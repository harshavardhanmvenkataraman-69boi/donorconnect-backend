package com.donorconnect.bloodsupplyservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecallRequest {
    private Long donationId;
    private Long componentId;
    @NotBlank
    private String reason;
    private LocalDate noticeDate;
}
