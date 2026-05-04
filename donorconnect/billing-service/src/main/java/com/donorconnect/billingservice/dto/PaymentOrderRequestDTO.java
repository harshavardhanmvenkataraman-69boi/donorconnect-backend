package com.project.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderRequestDTO {

    @NotNull(message = "Billing ID is required")
    private Integer billingId;
}
