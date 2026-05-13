package com.donorconnect.bloodsupplyservice.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DisposalRequest {
    @NotNull
    private Long componentId;
    private LocalDate disposalDate;
    private String disposalReason;
    private String witness;
    private String notes;
}
