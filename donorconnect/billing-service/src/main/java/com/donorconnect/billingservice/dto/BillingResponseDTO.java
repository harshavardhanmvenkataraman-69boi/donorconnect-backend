package com.donorconnect.billingservice.dto;

import com.donorconnect.billingservice.enums.BillingStatus;
import com.donorconnect.billingservice.enums.ChargeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Outbound representation of a billing record.
 * Timestamps are included so the UI can show audit info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingResponseDTO {

    private Integer billingId;
    private Integer issueId;
    private BigDecimal chargeAmount;
    private ChargeType chargeType;
    private LocalDate billingDate;
    private BillingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
