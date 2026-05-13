package com.donorconnect.safetyservice.entity;


import com.donorconnect.safetyservice.enums.ReactionStatus;
import com.donorconnect.safetyservice.enums.Severity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "reactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reactionId;

    @Column(nullable = false)
    private Long issueId;

    @Column(nullable = false)
    private Long patientId;

    private String reactionType;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private LocalDate reactionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReactionStatus status = ReactionStatus.PENDING;
}