package com.donorconnect.transfusionservice.feign;

import com.donorconnect.transfusionservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OpenFeign client for synchronous calls to blood-supply-service.
 */
@FeignClient(name = "blood-supply-service", path = "/api/v1", configuration = FeignConfig.class)
public interface BloodSupplyFeignClient {

    @GetMapping("/components/{id}")
    Map<String, Object> getComponentById(@PathVariable("id") Long id);

    @GetMapping("/components/available")
    List<Map<String, Object>> getAvailableComponents();

//    @GetMapping("/inventory")
//    List<Map<String, Object>> getInventory();

    @PutMapping("/components/{componentId}/status")
    Map<String, Object> updateComponentStatus(
            @PathVariable("componentId") Long componentId,
            @RequestParam("status") String status
    );
}
