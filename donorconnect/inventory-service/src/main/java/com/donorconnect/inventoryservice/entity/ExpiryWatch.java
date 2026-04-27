package com.donorconnect.inventoryservice.entity;

import com.donorconnect.inventoryservice.enums.ExpiryWatchStatus;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Alert record raised when a component's expiry is within the warning window.
 * Created automatically by ExpiryWatchScheduler.
 */
@Entity
@Table(name = "expiry_watch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expiryId;

    /** FK to InventoryBalance (and indirectly to Component in blood-supply-service) */
    @Column(nullable = false)
    private Long componentId;

    @Column(nullable = false)
    private Integer daysToExpire;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private LocalDate flagDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpiryWatchStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = ExpiryWatchStatus.OPEN;
        if (flagDate == null) flagDate = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}