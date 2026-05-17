package com.donorconnect.inventoryservice.dto.response;

import com.donorconnect.inventoryservice.enums.ExpiryWatchStatus;

import lombok.*;

import java.time.LocalDate;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ExpiryWatchResponse {
    private Long expiryId;
    private Long componentId;
    private Integer daysToExpire;
    private LocalDate expiryDate;
    private LocalDate flagDate;
    private ExpiryWatchStatus status;
}