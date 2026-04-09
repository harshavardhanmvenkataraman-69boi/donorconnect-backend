package com.donorconnect.transfusionservice.entity;
import com.donorconnect.transfusionservice.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "issue_records") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IssueRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long issueId;
    @Column(nullable = false) private Long componentId;
    @Column(nullable = false) private Long patientId;
    private LocalDate issueDate;
    private Long issuedBy;
    private String indication;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private IssueStatus status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); if (status == null) status = IssueStatus.ISSUED; }
}
