package com.donorconnect.transfusionservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class IssueRequestDto {
    @NotNull
    private Long componentId;
    @NotNull private Long patientId;
    private LocalDate issueDate;
    private String issuedBy;
    private String indication;
}