package com.donorconnect.inventoryservice.dto.response;

import com.donorconnect.inventoryservice.enums.TransactionType;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class StockTransactionResponse {
    private Long txnId;
    private Long componentId;
    private Long locationId;
    private TransactionType txnType;
    private Integer quantity;
    private LocalDate txnDate;
    private String referenceId;
    private String notes;
    private Long performedBy;
    private LocalDateTime createdAt;
}
