package com.donorconnect.transfusionservice.feign;
import com.donorconnect.transfusionservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryFeignClient {

    @GetMapping("/api/v1/inventory/component/{componentId}")
    Map<String, Object> getInventoryByComponentId(@PathVariable Long componentId);

    //Check Availability
    @GetMapping("/api/v1/inventory/available")
    List<Map<String, Object>> getAvailableUnits(
            @RequestParam String bloodGroup,
            @RequestParam String rhFactor,
            @RequestParam String componentType);

    // Reserve unit
    @PatchMapping("/api/v1/inventory/{componentId}/status")
    Map<String, Object> updateInventoryStatus(
            @PathVariable Long componentId,
            @RequestBody Map<String, String> request
    );
}

