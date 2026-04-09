package com.donorconnect.transfusionservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class TransfusionKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String COMPONENT_ISSUED_TOPIC = "transfusion.component.issued";
    public static final String REACTION_ALERT_TOPIC = "transfusion.reaction.alert";

    public void publishComponentIssued(ComponentIssuedEvent event) {
        log.info("Publishing ComponentIssuedEvent issueId={}", event.getIssueId());
        kafkaTemplate.send(COMPONENT_ISSUED_TOPIC, String.valueOf(event.getIssueId()), event);
    }
}
