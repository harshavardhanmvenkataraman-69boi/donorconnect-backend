package com.donorconnect.inventoryservice.dto.request;

import com.donorconnect.inventoryservice.enums.*;

import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.LocalDate;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class InventoryEntryRequest {
    @NotNull private Long componentId;
    @NotNull private BloodGroup bloodGroup;
    @NotNull private RhFactor rhFactor;
    @NotNull private ComponentType componentType;
    @NotNull private LocalDate expiryDate;
    @NotNull private String bagNumber;
}