package com.donorconnect.billingservice.model;

import com.donorconnect.billingservice.enums.BillingStatus;
import com.donorconnect.billingservice.enums.ChargeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reference billing record, per the design spec (section 4).
 *
 * One record is generated per blood-component issue and acts as a hand-off
 * to an external hospital billing system via the /export endpoints.
 */
@Entity
@Table(
    name = "billing_refs",
    indexes = {
        @Index(name = "idx_billing_issue_id",     columnList = "issue_id"),
        @Index(name = "idx_billing_status",       columnList = "status"),
        @Index(name = "idx_billing_billing_date", columnList = "billing_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_id")
    private Integer billingId;

    @Column(name = "issue_id", nullable = false)
    private Integer issueId;

    @Column(name = "charge_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal chargeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", length = 20, nullable = false)
    private ChargeType chargeType;

    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BillingStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = BillingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
