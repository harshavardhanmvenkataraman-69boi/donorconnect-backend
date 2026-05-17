package com.donorconnect.billingservice.dto;

import com.donorconnect.billingservice.enums.BillingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Payload for PATCH /billing/{id}/status.
 *
 * Use Spring's enum-binding so an unknown status value is rejected
 * by Jackson with a clear 400 before reaching the service layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private BillingStatus status;
}
