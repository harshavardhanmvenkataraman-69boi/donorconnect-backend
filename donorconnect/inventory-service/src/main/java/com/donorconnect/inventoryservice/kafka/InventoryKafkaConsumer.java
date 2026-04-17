package com.donorconnect.inventoryservice.kafka;

import com.donorconnect.inventoryservice.dto.request.InventoryStatusUpdateRequest;
import com.donorconnect.inventoryservice.enums.InventoryStatus;
import com.donorconnect.inventoryservice.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class InventoryKafkaConsumer {

    private final InventoryService inventoryService;

    /**
     * Consumes blood.test.reactive from blood-supply-service.
     * When a test is reactive, quarantine the component in inventory.
     */
    @KafkaListener(topics = "blood.test.reactive", groupId = "inventory-service-group")
    @Transactional
    public void onTestReactive(Map<String, Object> event) {
        log.info("Inventory consuming blood.test.reactive: {}", event);
        // blood.test.reactive carries donationId — quarantine all components from that donation
        // This is handled by blood-supply-service itself (self-consume).
        // inventory-service listens for the resulting status change events.
    }

    /**
     * Consumes transfusion.component.issued from transfusion-service.
     * Decrements inventory when blood is issued to a patient.
     */
    @KafkaListener(topics = "transfusion.component.issued", groupId = "inventory-service-group")
    @Transactional
    public void onComponentIssued(Map<String, Object> event) {
        log.info("Inventory consuming transfusion.component.issued: {}", event);
        if (event.get("componentId") == null) return;
        Long componentId = Long.valueOf(event.get("componentId").toString());
        try {
            inventoryService.updateStatus(componentId,
                    InventoryStatusUpdateRequest.builder()
                            .status(InventoryStatus.ISSUED)
                            .reason("Issued to patient via transfusion")
                            .build());
        } catch (Exception e) {
            log.error("Failed to update inventory for componentId={}: {}", componentId, e.getMessage());
        }
    }
}
