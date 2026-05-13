package com.donorconnect.transfusionservice.feign;

import com.donorconnect.transfusionservice.dto.request.InventoryStatusUpdateRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class InventoryFeignClientFallback implements InventoryFeignClient {

    @Override
    public Map<String, Object> getInventoryByComponentId(Long componentId) {
        log.error(">>> CIRCUIT BREAKER: inventory-service down. " +
                "Cannot check status for componentId = {}", componentId);
        // Return UNKNOWN status — TransfusionService will throw IllegalStateException
        return Map.of(
                "data", Map.of("status", "UNKNOWN"),
                "success", false,
                "message", "Inventory service unavailable"
        );
    }

    @Override
    public Map<String, Object> getAvailableUnits(
            String bloodGroup, String rhFactor, String componentType) {
        log.error(">>> CIRCUIT BREAKER: inventory-service down. " +
                        "Cannot fetch available units for '{}' '{}' '{}'",
                bloodGroup, rhFactor, componentType);
        // Return empty data — createRequest() will mark as INSUFFICIENT_STOCK
        return Map.of(
                "data", java.util.List.of(),
                "success", false,
                "message", "Inventory service unavailable"
        );
    }

    @Override
    public Map<String, Object> updateInventoryStatus(
            Long componentId, InventoryStatusUpdateRequest request) {
        log.error(">>> CIRCUIT BREAKER: inventory-service down. " +
                        "Cannot update status for componentId = {} to {}",
                componentId, request.getStatus());
        return Map.of(
                "success", false,
                "message", "Inventory service unavailable. Status update failed."
        );
    }
}