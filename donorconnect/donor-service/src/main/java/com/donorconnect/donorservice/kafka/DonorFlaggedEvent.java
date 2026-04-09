package com.donorconnect.donorservice.kafka;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DonorFlaggedEvent {
    private Long donorId;
    private String reason;
    private String flaggedBy;
    private String timestamp;
}
