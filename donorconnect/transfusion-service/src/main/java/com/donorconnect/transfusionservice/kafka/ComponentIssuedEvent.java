package com.donorconnect.transfusionservice.kafka;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ComponentIssuedEvent {
    private Long componentId;
    private Long patientId;
    private Long issueId;
    private String bloodGroup;
    private String rhFactor;
    private Double chargeAmount;
    private String timestamp;
}
