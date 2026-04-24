package com.donorconnect.billingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerifyRequestDTO {

    @NotNull(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotNull(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotNull(message = "Razorpay Signature is required")
    private String razorpaySignature;
}
