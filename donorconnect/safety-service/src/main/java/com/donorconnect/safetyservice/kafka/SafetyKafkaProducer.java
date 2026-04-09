package com.donorconnect.safetyservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class SafetyKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String SAFETY_ALERT_TOPIC = "safety.alert";

    public void publishSafetyAlert(Map<String, Object> alert) {
        log.info("Publishing safety alert: {}", alert);
        kafkaTemplate.send(SAFETY_ALERT_TOPIC, String.valueOf(alert.get("reactionId")), alert);
    }
}
