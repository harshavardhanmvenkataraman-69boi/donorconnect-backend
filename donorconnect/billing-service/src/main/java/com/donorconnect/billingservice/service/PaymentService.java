package com.donorconnect.billingservice.service;

import com.donorconnect.billingservice.dto.*;
import com.donorconnect.billingservice.model.PaymentTransaction;
import com.donorconnect.billingservice.repository.BillingRepository;
import com.donorconnect.billingservice.repository.PaymentTransactionRepository;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final BillingRepository billingRepository;
    private final PaymentTransactionRepository paymentRepo;

    @Value("${razorpay.key.id}")
    @SuppressWarnings("FieldMayBeFinal")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    @SuppressWarnings("FieldMayBeFinal")
    private String razorpaySecret;

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 1: Create a Razorpay order and persist a local transaction record
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public com.project.billing.dto.PaymentOrderResponseDTO createOrder(com.project.billing.dto.PaymentOrderRequestDTO req) throws RazorpayException {

        // Load and validate the billing record
        com.donorconnect.billingservice.model.BillingRef billing = billingRepository.findById(req.getBillingId())
                .orElseThrow(() -> new RuntimeException("Billing not found: " + req.getBillingId()));

        String currentStatus = billing.getStatus().toUpperCase();
        if (!"PENDING".equals(currentStatus) && !"OVERDUE".equals(currentStatus)) {
            throw new RuntimeException(
                    "Only PENDING or OVERDUE bills can be paid. Current status: " + currentStatus);
        }

        // Razorpay expects amount in the smallest currency unit (paise for INR)
        int amountInPaise = billing.getChargeAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        // Build the Razorpay order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "billing_" + billing.getBillingId());
        orderRequest.put("payment_capture", 1); // auto-capture

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String rzpOrderId = razorpayOrder.get("id");

        log.info("Razorpay order created: {} for billing: {}", rzpOrderId, billing.getBillingId());

        // Persist a local transaction record
        PaymentTransaction txn = PaymentTransaction.builder()
                .billingId(billing.getBillingId())
                .amount(billing.getChargeAmount())
                .razorpayOrderId(rzpOrderId)
                .status("CREATED")
                .build();
        paymentRepo.save(txn);

        return com.project.billing.dto.PaymentOrderResponseDTO.builder()
                .transactionId(txn.getTransactionId())
                .razorpayOrderId(rzpOrderId)
                .amount(billing.getChargeAmount())
                .currency("INR")
                .razorpayKeyId(razorpayKeyId)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 2: Verify the Razorpay signature and update billing status to PAID
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public com.project.billing.dto.PaymentVerifyResponseDTO verifyPayment(com.project.billing.dto.PaymentVerifyRequestDTO req) {

        PaymentTransaction txn = paymentRepo.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found for order: " + req.getRazorpayOrderId()));

        boolean signatureValid = verifySignature(
                req.getRazorpayOrderId(),
                req.getRazorpayPaymentId(),
                req.getRazorpaySignature());

        if (signatureValid) {
            // Update transaction
            txn.setRazorpayPaymentId(req.getRazorpayPaymentId());
            txn.setRazorpaySignature(req.getRazorpaySignature());
            txn.setStatus("SUCCESS");
            paymentRepo.save(txn);

            // Mark the billing record as PAID via the existing status-transition logic
            billingRepository.findById(txn.getBillingId()).ifPresent(b -> {
                b.setStatus("PAID");
                billingRepository.save(b);
                log.info("Billing {} marked as PAID after successful payment", b.getBillingId());
            });

            return com.project.billing.dto.PaymentVerifyResponseDTO.builder()
                    .transactionId(txn.getTransactionId())
                    .status("SUCCESS")
                    .message("Payment verified and billing marked as PAID")
                    .verifiedAt(LocalDateTime.now())
                    .build();

        } else {
            // Signature mismatch — possible tampering
            txn.setStatus("FAILED");
            txn.setFailureReason("HMAC signature verification failed");
            paymentRepo.save(txn);

            log.warn("Signature verification FAILED for order: {}", req.getRazorpayOrderId());

            return com.project.billing.dto.PaymentVerifyResponseDTO.builder()
                    .transactionId(txn.getTransactionId())
                    .status("FAILED")
                    .message("Signature verification failed — payment rejected")
                    .verifiedAt(LocalDateTime.now())
                    .build();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET: All transactions for a billing record
    // ─────────────────────────────────────────────────────────────────────────
    public List<com.project.billing.dto.PaymentTransactionDTO> getTransactionsByBillingId(Integer billingId) {
        List<PaymentTransaction> transactions = paymentRepo.findByBillingId(billingId);
        List<com.project.billing.dto.PaymentTransactionDTO> dtoList = new ArrayList<>();

        for (PaymentTransaction txn : transactions) {
            dtoList.add(mapToDTO(txn));
        }

        return dtoList;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HMAC-SHA256 signature verification
    //   Payload = razorpay_order_id + "|" + razorpay_payment_id
    //   Key     = Razorpay key secret
    // ─────────────────────────────────────────────────────────────────────────
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            String generated = HexFormat.of().formatHex(hash);
            return generated.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }

    private com.project.billing.dto.PaymentTransactionDTO mapToDTO(PaymentTransaction txn) {
        return com.project.billing.dto.PaymentTransactionDTO.builder()
                .transactionId(txn.getTransactionId())
                .billingId(txn.getBillingId())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .gateway(txn.getGateway())
                .razorpayOrderId(txn.getRazorpayOrderId())
                .razorpayPaymentId(txn.getRazorpayPaymentId())
                .status(txn.getStatus())
                .failureReason(txn.getFailureReason())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }
}
