package com.donorconnect.donorservice.entity;

import com.donorconnect.donorservice.enums.DriveStatus;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "drives")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driveId;

    @Column(nullable = false)
    private String name;

    private String location;

    private LocalDate scheduledDate;

    private Integer capacity;

    private String organizer;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DriveStatus status = DriveStatus.PLANNED;
}