package com.donorconnect.inventoryservice.dto.response;

import com.donorconnect.inventoryservice.enums.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class InventoryBalanceResponse {
    private Long balanceId;
    private Long componentId;
    private BloodGroup bloodGroup;
    private RhFactor rhFactor;
    private ComponentType componentType;
    private String bagNumber;
    private LocalDate expiryDate;
    private Integer quantity;
    private InventoryStatus status;
    private Long locationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}