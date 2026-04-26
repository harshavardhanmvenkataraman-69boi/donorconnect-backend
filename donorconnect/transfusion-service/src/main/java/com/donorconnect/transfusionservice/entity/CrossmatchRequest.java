package com.donorconnect.transfusionservice.entity;
import com.donorconnect.transfusionservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "crossmatch_requests") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CrossmatchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @Column(nullable = false)
    private Long patientId;

    private String orderBy;

    private String bloodGroup;

    private String rhFactor;

    private Integer requiredUnits;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CrossmatchPriority priority = CrossmatchPriority.ROUTINE;

    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CrossmatchStatus status = CrossmatchStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String availableComponentIds;
}
