package com.donorconnect.billingservice.entity;
import com.donorconnect.billingservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "billing_refs") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BillingRef {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long billingId;
    @Column(nullable = false) private Long issueId;
    private BigDecimal chargeAmount;
    @Enumerated(EnumType.STRING) private ChargeType chargeType;
    private LocalDate billingDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BillingStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = BillingStatus.DRAFT; }
}
