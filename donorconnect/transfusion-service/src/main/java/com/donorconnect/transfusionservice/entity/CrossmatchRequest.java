package com.donorconnect.transfusionservice.entity;
import com.donorconnect.transfusionservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "crossmatch_requests") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CrossmatchRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long requestId;
    @Column(nullable = false) private Long patientId;
    private Long orderBy;
    private String bloodGroup;
    private String rhFactor;
    private Integer requiredUnits;
    @Enumerated(EnumType.STRING) private Priority priority;
    private LocalDate requestDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CrossmatchRequestStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = CrossmatchRequestStatus.PENDING; }
}
