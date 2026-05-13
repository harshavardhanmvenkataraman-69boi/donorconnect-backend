package com.project.billing.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDTO {

    private Long transactionId;
    private Integer billingId;
    private BigDecimal amount;
    private String currency;
    private String gateway;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
