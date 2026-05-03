package com.donorconnect.safetyservice.dto.request;

import com.donorconnect.safetyservice.enums.Severity;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
public class ReactionRequest {
    @NotNull
    private Long issueId;
    @NotNull private Long patientId;
    private String reactionType;
    private Severity severity;
    private LocalDate reactionDate;
    private String notes;
}