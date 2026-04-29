package com.donorconnect.billingservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingResponseDTO {

    private Integer billingId;
    private Integer issueId;
    private BigDecimal chargeAmount;
    private String chargeType;
    private LocalDate billingDate;
    private String status;
}
