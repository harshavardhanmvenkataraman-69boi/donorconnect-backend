package com.donorconnect.inventoryservice.entity;

import com.donorconnect.inventoryservice.enums.*;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One row per blood component in stock.
 * Denormalized fields (bloodGroup, rhFactor, componentType, expiryDate)
 * are copied from blood-supply-service at creation time so inventory-service
 * never needs to Feign-call blood-supply-service for reads.
 */
@Entity
@Table(name = "inventory_balances",
       uniqueConstraints = @UniqueConstraint(columnNames = "component_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long balanceId;

    /** FK to Component in blood-supply-service (cross-service reference, no DB FK constraint) */
    @Column(nullable = false, unique = true)
    private Long componentId;

    // --- Denormalized from Component (copied at creation, never changes) ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RhFactor rhFactor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentType componentType;

    private LocalDate expiryDate;

    private String bagNumber;

    // --- Mutable fields updated via Kafka events from blood-supply-service ---
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
        if (quantity == null) quantity = 1;
        if (status == null) status = InventoryStatus.AVAILABLE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
