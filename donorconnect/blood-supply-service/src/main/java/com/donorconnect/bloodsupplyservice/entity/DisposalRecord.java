package com.donorconnect.bloodsupplyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "disposal_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisposalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long disposalId;

    @Column(nullable = false)
    private Long componentId;

    private LocalDate disposalDate;

    private String disposalReason;

    private String witness;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private String status = "COMPLETED";
}
