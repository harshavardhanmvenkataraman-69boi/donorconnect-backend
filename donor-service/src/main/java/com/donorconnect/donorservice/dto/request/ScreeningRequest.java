package com.donorconnect.donorservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScreeningRequest {
    @NotNull
    private Long donorId;
    private String vitalsJson;
    private String questionnaireJson;
    private Boolean clearedFlag;
    private String clearedBy;
    private String notes;
    private DeferralRequest deferralRequest;
}