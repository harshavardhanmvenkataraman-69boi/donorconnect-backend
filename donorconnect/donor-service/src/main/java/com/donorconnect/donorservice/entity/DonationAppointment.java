package com.donorconnect.donorservice.entity;

import com.donorconnect.donorservice.enums.AppointmentStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "donation_appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @Column(nullable = false)
    private Long donorId;

    private LocalDateTime dateTime;

    private Long centerId;

    private Long driveId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.BOOKED;
}
