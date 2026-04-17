package com.donorconnect.inventoryservice.kafka;

import com.donorconnect.inventoryservice.entity.InventoryBalance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    public static final String LOW_STOCK_TOPIC = "inventory.low.stock";
    public static final String EXPIRY_ALERT_TOPIC = "inventory.expiry.alert";

    public void publishLowStockAlert(List<InventoryBalance> lowItems) {
        if (!kafkaEnabled) {
            log.info("Kafka disabled; skipping low-stock alert publish");
            return;
        }
        Map<String, Object> event = Map.of(
                "type", "LOW_STOCK",
                "count", lowItems.size(),
                "timestamp", LocalDateTime.now().toString()
        );
        log.info("Publishing low-stock alert: {} items below threshold", lowItems.size());
        kafkaTemplate.send(LOW_STOCK_TOPIC, "low-stock", event);
    }

    public void publishExpiryAlert(Long componentId, int daysToExpire) {
        if (!kafkaEnabled) {
            log.info("Kafka disabled; skipping expiry alert publish for componentId={}", componentId);
            return;
        }
        Map<String, Object> event = Map.of(
                "type", "EXPIRY_ALERT",
                "componentId", componentId,
                "daysToExpire", daysToExpire,
                "timestamp", LocalDateTime.now().toString()
        );
        log.info("Publishing expiry alert for componentId={}, daysToExpire={}", componentId, daysToExpire);
        kafkaTemplate.send(EXPIRY_ALERT_TOPIC, String.valueOf(componentId), event);
    }
}