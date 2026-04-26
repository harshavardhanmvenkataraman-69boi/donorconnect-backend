package com.donorconnect.safetyservice.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lookback_traces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookbackTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long traceId;

    @Column(nullable = false)
    private Long donationId;

    @Column(nullable = false)
    private Long componentId;

    @Column(nullable = false)
    private Long patientId;

    private LocalDate traceDate;

    @Builder.Default
    private String status = "ACTIVE";
}

