package com.donorconnect.transfusionservice.entity;
import com.donorconnect.transfusionservice.enums.Compatibility;
import com.donorconnect.transfusionservice.enums.CrossmatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Entity @Table(name = "crossmatch_results") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CrossmatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long crossmatchId;

    @Column(nullable = false)
    private Long requestId;

    @Column(nullable = false)
    private Long componentId;

    @Enumerated(EnumType.STRING)
    private Compatibility compatibility;

    private String testedBy;

    private LocalDate testedDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CrossmatchStatus status = CrossmatchStatus.PENDING;
}
