package com.donorconnect.safetyservice.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
public class LookbackRequest {
    @NotNull private Long donationId;
    @NotNull
    private Long componentId;
    @NotNull private Long patientId;
    private LocalDate traceDate;
}