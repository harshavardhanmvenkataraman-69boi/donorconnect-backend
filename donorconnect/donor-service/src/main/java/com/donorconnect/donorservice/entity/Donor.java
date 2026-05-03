package com.donorconnect.donorservice.entity;

import com.donorconnect.donorservice.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "donors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donorId;

    @Column(nullable = false)
    private String name;

    private String dob;

    private String gender;

    private String bloodGroup;

    private String rhFactor;

    private String contactInfo;

    @Column(columnDefinition = "TEXT")
    private String addressJson;

    @Enumerated(EnumType.STRING)
    private DonorType donorType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DonorStatus status = DonorStatus.ACTIVE;
}