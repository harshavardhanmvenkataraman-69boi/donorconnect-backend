package com.donorconnect.donorservice.entity;
import com.donorconnect.donorservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "donors") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Donor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long donorId;
    @Column(nullable = false) private String name;
    private LocalDate dob;
    private String gender;
    @Enumerated(EnumType.STRING) private BloodGroup bloodGroup;
    @Enumerated(EnumType.STRING) private RhFactor rhFactor;
    private String contactInfo;
    @Column(columnDefinition = "TEXT") private String addressJson;
    @Enumerated(EnumType.STRING) private DonorType donorType;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DonorStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = DonorStatus.ACTIVE; }
}
