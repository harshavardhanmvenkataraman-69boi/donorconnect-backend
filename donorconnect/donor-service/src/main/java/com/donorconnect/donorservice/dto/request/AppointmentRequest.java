package com.donorconnect.donorservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotNull
    private Long donorId;
    private LocalDateTime dateTime;
    private Long centerId;
    private Long driveId;
}
