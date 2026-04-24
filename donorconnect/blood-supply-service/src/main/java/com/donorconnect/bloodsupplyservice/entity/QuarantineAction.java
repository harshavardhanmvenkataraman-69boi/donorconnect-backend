package com.donorconnect.bloodsupplyservice.entity;


import com.donorconnect.bloodsupplyservice.enums.QuarantineStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "quarantine_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuarantineAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qaId;

    @Column(nullable = false)
    private Long componentId;

    private LocalDate startDate;

    private String reason;

    private LocalDate releasedDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QuarantineStatus status = QuarantineStatus.QUARANTINED;
}
