package com.donorconnect.bloodsupplyservice.entity;

import com.donorconnect.bloodsupplyservice.enums.TestStatus;
import com.donorconnect.bloodsupplyservice.enums.TestType;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testResultId;

    @Column(nullable = false)
    private Long donationId;

    @Enumerated(EnumType.STRING)
    private TestType testType;

    private String result;

    private LocalDate resultDate;

    private String enteredBy;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TestStatus status = TestStatus.PENDING;
}
