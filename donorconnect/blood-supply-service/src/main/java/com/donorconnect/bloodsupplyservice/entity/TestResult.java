package com.donorconnect.bloodsupplyservice.entity;
import com.donorconnect.bloodsupplyservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "test_results") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TestResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long testResultId;
    @Column(nullable = false) private Long donationId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TestType testType;
    private String result;
    private LocalDate resultDate;
    private Long enteredBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TestResultStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = TestResultStatus.PENDING; }
}
