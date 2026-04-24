package com.donorconnect.inventoryservice.dto.request;

import com.donorconnect.inventoryservice.enums.InventoryStatus;

import jakarta.validation.constraints.NotNull;

import lombok.*;

/** Received from blood-supply-service when component status changes (QUARANTINE / ISSUED / EXPIRED) */
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class InventoryStatusUpdateRequest {
    @NotNull private InventoryStatus status;
    private String reason;
}
