package com.project.billing.dto;

import lombok.*;
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
