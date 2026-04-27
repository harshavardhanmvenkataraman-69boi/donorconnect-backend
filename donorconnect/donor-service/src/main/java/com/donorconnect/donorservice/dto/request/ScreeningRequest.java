package com.donorconnect.donorservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScreeningRequest {
    @NotNull
    private Long donorId;
    private String vitalsJson;
    private String questionnaireJson;
    private Boolean clearedFlag;
    private String clearedBy;
    private String notes;

    /**
     * Required when clearedFlag = false.
     * The embedded deferral details are used to auto-create a deferral
     * atomically with the screening record.
     */
    private DeferralRequest deferralRequest;
}
