package com.donorconnect.reportingservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryBalanceDto {
    private Long balanceId;
    private Long componentId;
    private String bloodGroup;
    private String rhFactor;
    private String componentType;
    private String bagNumber;
    private LocalDate expiryDate;
    private Integer quantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
