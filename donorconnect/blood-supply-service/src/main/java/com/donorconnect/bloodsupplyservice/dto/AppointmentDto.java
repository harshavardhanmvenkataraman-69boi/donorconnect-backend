package com.donorconnect.bloodsupplyservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDto {
    private Long appointmentId;
    private Long donorId;
    private LocalDateTime dateTime;
    private Long centerId;
    private Long driveId;
    private String status;
}
