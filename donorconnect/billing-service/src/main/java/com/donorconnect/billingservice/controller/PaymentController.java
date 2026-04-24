package com.donorconnect.billingservice.controller;

import com.donorconnect.billingservice.dto.*;
import com.donorconnect.billingservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
//@PreAuthorize("hasRole('ADMIN')")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /payment/create-order
     * Creates a Razorpay order for the given billing record.
     * Returns the order details needed to open Razorpay Checkout on the frontend.
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponseDTO> createOrder(
            @Valid @RequestBody PaymentOrderRequestDTO req) throws Exception {
        return ResponseEntity.ok(paymentService.createOrder(req));
    }

    /**
     * POST /payment/verify
     * Called after the user completes payment on Razorpay Checkout.
     * Verifies the HMAC signature and marks the billing as PAID on success.
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentVerifyResponseDTO> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequestDTO req) {
        return ResponseEntity.ok(paymentService.verifyPayment(req));
    }

    /**
     * GET /payment/transactions/{billingId}
     * Returns all payment transactions for a given billing record.
     */
    @GetMapping("/transactions/{billingId}")
    public ResponseEntity<List<PaymentTransactionDTO>> getTransactions(
            @PathVariable Integer billingId) {
        return ResponseEntity.ok(paymentService.getTransactionsByBillingId(billingId));
    }
}
