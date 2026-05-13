package com.project.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

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
