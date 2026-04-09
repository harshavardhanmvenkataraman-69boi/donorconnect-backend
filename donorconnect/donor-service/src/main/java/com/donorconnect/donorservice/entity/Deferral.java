package com.donorconnect.donorservice.entity;
import com.donorconnect.donorservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "deferrals") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Deferral {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long deferralId;
    @Column(nullable = false) private Long donorId;
    @Enumerated(EnumType.STRING) private DeferralType deferralType;
    @Column(columnDefinition = "TEXT") private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DeferralStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = DeferralStatus.ACTIVE; }
}
