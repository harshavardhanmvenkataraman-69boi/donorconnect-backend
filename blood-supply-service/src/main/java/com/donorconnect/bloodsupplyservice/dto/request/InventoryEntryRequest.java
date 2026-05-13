package com.donorconnect.bloodsupplyservice.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.LocalDate;

/** Received from blood-supply-service (via Feign) when a new Component is created */
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class InventoryEntryRequest {
    private Long componentId;
    private String bloodGroup;
    private String rhFactor;
    private String componentType;
    private LocalDate expiryDate;
    private String bagNumber;
}
