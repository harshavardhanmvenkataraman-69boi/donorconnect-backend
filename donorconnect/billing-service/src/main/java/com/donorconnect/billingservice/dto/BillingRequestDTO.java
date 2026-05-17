package com.donorconnect.billingservice.dto;

import com.donorconnect.billingservice.enums.BillingStatus;
import com.donorconnect.billingservice.enums.ChargeType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inbound payload for creating a billing record.
 *
 * `status` is optional — when omitted the service defaults to PENDING,
 * which matches the spec's workflow (a newly issued unit produces a
 * pending billing entry that is later marked EXPORTED or CANCELLED).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRequestDTO {

    @NotNull(message = "Issue ID is required")
    @Positive(message = "Issue ID must be positive")
    private Integer issueId;

    @NotNull(message = "Charge amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Charge amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Charge amount must have at most 10 digits and 2 decimal places")
    private BigDecimal chargeAmount;

    @NotNull(message = "Charge type is required")
    private ChargeType chargeType;

    @NotNull(message = "Billing date is required")
    @PastOrPresent(message = "Billing date cannot be in the future")
    private LocalDate billingDate;

    /** Optional. Defaults to PENDING when not supplied. */
    private BillingStatus status;
}
