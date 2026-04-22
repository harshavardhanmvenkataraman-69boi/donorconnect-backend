package com.donorconnect.bloodsupplyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class QuarantineRequest {
    @NotNull
    private Long componentId;
    private LocalDate startDate;
    @NotBlank
    private String reason;
}
