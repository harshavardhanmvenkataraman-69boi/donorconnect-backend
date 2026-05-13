package com.donorconnect.reportingservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReactionDto {
    private Long reactionId;
    private Long issueId;
    private Long patientId;
    private String reactionType;
    private String severity;
    private LocalDate reactionDate;
    private String status;
}
