package com.donorconnect.transfusionservice.dto.request;

import com.donorconnect.transfusionservice.enums.CrossmatchPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
@Data
public class CrossmatchRequestDto {
    @NotNull
    private Long patientId;
    private String orderBy;
    private String bloodGroup;
    private String rhFactor;
    private Integer requiredUnits;
    private CrossmatchPriority priority;
    private LocalDate requestDate;
    private String notes;
    private String componentType;
}
