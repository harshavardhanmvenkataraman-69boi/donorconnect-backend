package com.donorconnect.bloodsupplyservice.entity;


import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    @Column(nullable = false)
    private Long donorId;

    private LocalDate collectionDate;

    @Column(unique = true)
    private String bagId;

    private Integer volumeMl;

    private String collectedBy;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CollectionStatus collectionStatus = CollectionStatus.COLLECTED;
}
