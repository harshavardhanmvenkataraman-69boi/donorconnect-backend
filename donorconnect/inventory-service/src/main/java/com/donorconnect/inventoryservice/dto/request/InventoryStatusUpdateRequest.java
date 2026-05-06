package com.donorconnect.inventoryservice.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class InventoryStatusUpdateRequest {
    @NotNull private String status;
    private String reason;
}
