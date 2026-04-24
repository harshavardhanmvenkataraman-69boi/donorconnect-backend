package com.donorconnect.billingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerifyResponseDTO {

    private Long transactionId;
    private String status;
    private String message;
    private LocalDateTime verifiedAt;
}
