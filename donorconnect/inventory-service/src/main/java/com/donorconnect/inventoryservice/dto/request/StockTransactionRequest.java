package com.donorconnect.inventoryservice.dto.request;

import com.donorconnect.inventoryservice.enums.TransactionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.LocalDate;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class StockTransactionRequest {
    @NotNull private Long componentId;
    @NotNull private TransactionType txnType;
    @NotNull @Min(1) private Integer quantity;
    private LocalDate txnDate;
    private String referenceId;
    private String notes;
}