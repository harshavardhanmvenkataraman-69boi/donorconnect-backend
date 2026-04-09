package com.donorconnect.bloodsupplyservice.entity;
import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "donations") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Donation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long donationId;
    @Column(nullable = false) private Long donorId;
    private LocalDate collectionDate;
    private String bagId;
    private Double volumeMl;
    private Long collectedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CollectionStatus collectionStatus;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (collectionStatus == null) collectionStatus = CollectionStatus.COLLECTED; }
}
