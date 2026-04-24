package com.donorconnect.billingservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRequestDTO {

    @NotNull(message = "Issue ID is required")
    private Integer issueId;

    @NotNull(message = "Charge amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Charge amount must be positive")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal chargeAmount;

    @NotBlank(message = "Charge type is required")
    @Size(max = 50)
    private String chargeType;

    @NotNull(message = "Billing date is required")
    private LocalDate billingDate;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    private String status;
}
