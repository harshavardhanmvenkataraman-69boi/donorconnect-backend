package com.donorconnect.bloodsupplyservice.entity;
import com.donorconnect.bloodsupplyservice.enums.RecallStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "recall_notices") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RecallNotice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long recallId;
    private Long donationId;
    private Long componentId;
    @Column(columnDefinition = "TEXT") private String reason;
    private LocalDate noticeDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RecallStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = RecallStatus.OPEN; }
}
