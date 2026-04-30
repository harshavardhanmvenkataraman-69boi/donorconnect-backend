package com.donorconnect.inventoryservice.entity;

import com.donorconnect.inventoryservice.enums.TransactionType;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long txnId;

    @Column(nullable = false)
    private Long componentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType txnType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate txnDate;

    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Long performedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (txnDate == null) txnDate = LocalDate.now();
    }
}