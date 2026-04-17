package com.donorconnect.inventoryservice.entity;

import com.donorconnect.inventoryservice.enums.TransactionType;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable audit record of every quantity change to a component in inventory.
 * Created on RECEIPT, ISSUE, RETURN, TRANSFER, ADJUST, QUARANTINE, RELEASE.
 */
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

    private Long locationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType txnType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate txnDate;

    /** Reference to issueId, recallId, transferId etc. depending on txnType */
    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Who performed the transaction (userId from JWT) */
    private Long performedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (txnDate == null) txnDate = LocalDate.now();
    }
}
