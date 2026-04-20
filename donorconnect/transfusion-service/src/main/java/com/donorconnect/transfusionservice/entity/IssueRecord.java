package com.donorconnect.transfusionservice.entity;
import com.donorconnect.transfusionservice.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "issue_records") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IssueRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long issueId;

    @Column(nullable = false)
    private Long componentId;

    @Column(nullable = false)
    private Long patientId;

    private LocalDate issueDate;

    private String issuedBy;

    private String indication;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IssueStatus status = IssueStatus.ISSUED;
}
