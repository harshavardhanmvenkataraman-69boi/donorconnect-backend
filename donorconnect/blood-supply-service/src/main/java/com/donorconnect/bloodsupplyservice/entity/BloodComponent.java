package com.donorconnect.bloodsupplyservice.entity;
import com.donorconnect.bloodsupplyservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "components") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Component {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long componentId;
    @Column(nullable = false) private Long donationId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ComponentType componentType;
    private String bagNumber;
    private Double volume;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ComponentStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = ComponentStatus.AVAILABLE; }
}
