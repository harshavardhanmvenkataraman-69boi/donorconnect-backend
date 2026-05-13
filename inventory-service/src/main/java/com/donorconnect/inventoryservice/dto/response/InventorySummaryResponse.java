package com.donorconnect.inventoryservice.dto.response;

import com.donorconnect.inventoryservice.enums.*;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class InventorySummaryResponse {
    private BloodGroup bloodGroup;
    private ComponentType componentType;
    private Long totalQuantity;
}