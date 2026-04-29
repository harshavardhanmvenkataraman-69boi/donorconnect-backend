package com.donorconnect.billingservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestDTO {

    @NotNull(message = "Issue ID is required")
    private Integer issueId;

    @NotNull(message = "Charge amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Charge amount must be positive")
    private BigDecimal chargeAmount;

    @NotBlank(message = "Charge type is required")
    private String chargeType;

    @NotNull(message = "Billing date is required")
    @PastOrPresent(message = "Billing date cannot be in the future")
    private LocalDate billingDate;

    @NotBlank(message = "Status is required")
    private String status;
}
