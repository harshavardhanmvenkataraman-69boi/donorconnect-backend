package com.donorconnect.bloodsupplyservice.entity;

import com.donorconnect.bloodsupplyservice.enums.RecallStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "recall_notices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecallNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recallId;

    private Long donationId;

    private Long componentId;

    private String reason;

    private LocalDate noticeDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RecallStatus status = RecallStatus.OPEN;
}
