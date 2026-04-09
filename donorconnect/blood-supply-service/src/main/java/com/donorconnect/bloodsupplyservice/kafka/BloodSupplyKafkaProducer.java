package com.donorconnect.bloodsupplyservice.kafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class BloodSupplyKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String TEST_REACTIVE_TOPIC = "blood.test.reactive";
    public static final String COMPONENT_ISSUED_TOPIC = "blood.component.issued";
    public static final String LOW_STOCK_TOPIC = "blood.stock.low";

    public void publishTestReactive(TestResultReactiveEvent event) {
        log.info("Publishing TestResultReactiveEvent donationId={}", event.getDonationId());
        kafkaTemplate.send(TEST_REACTIVE_TOPIC, String.valueOf(event.getDonationId()), event);
    }

    public void publishComponentIssued(ComponentIssuedEvent event) {
        log.info("Publishing ComponentIssuedEvent componentId={}", event.getComponentId());
        kafkaTemplate.send(COMPONENT_ISSUED_TOPIC, String.valueOf(event.getComponentId()), event);
    }
}
