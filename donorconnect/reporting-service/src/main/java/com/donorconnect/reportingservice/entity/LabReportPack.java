package com.donorconnect.reportingservice.entity;
import com.donorconnect.reportingservice.enums.ReportScope;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "lab_report_packs") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LabReportPack {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long packId;
    @Enumerated(EnumType.STRING) private ReportScope scope;
    @Column(columnDefinition = "TEXT") private String metricsJson;
    private LocalDateTime generatedDate;
    @PrePersist public void prePersist() { generatedDate = LocalDateTime.now(); }
}
