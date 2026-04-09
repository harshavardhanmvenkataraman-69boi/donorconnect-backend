package com.donorconnect.safetyservice.entity;
import com.donorconnect.safetyservice.enums.LookbackStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "lookback_traces") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LookbackTrace {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long traceId;
    private Long donationId;
    private Long componentId;
    private Long patientId;
    private LocalDate traceDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LookbackStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = LookbackStatus.OPEN; }
}
