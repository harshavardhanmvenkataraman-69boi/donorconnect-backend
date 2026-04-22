package com.donorconnect.donorservice.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "screening_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long screeningId;

    @Column(nullable = false)
    private Long donorId;

    private LocalDate screeningDate;

    @Column(columnDefinition = "TEXT")
    private String vitalsJson;

    @Column(columnDefinition = "TEXT")
    private String questionnaireJson;

    private Boolean clearedFlag;

    private String clearedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

