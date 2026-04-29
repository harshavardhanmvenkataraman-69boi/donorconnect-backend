package com.donorconnect.billingservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "billing_refs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_id")
    private Integer billingId;

    @Column(name = "issue_id")
    private Integer issueId;

    @Column(name = "charge_amount", precision = 10, scale = 2)
    private BigDecimal chargeAmount;

    @Column(name = "charge_type", length = 50)
    private String chargeType;

    @Column(name = "billing_date")
    private LocalDate billingDate;

    @Column(name = "status", length = 20)
    private String status;
}
