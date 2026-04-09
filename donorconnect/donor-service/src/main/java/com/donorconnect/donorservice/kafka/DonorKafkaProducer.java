package com.donorconnect.donorservice.kafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class DonorKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String DONOR_FLAGGED_TOPIC = "donor.flagged";

    public void publishDonorFlagged(DonorFlaggedEvent event) {
        log.info("Publishing DonorFlaggedEvent for donorId={}", event.getDonorId());
        kafkaTemplate.send(DONOR_FLAGGED_TOPIC, String.valueOf(event.getDonorId()), event);
    }
}
