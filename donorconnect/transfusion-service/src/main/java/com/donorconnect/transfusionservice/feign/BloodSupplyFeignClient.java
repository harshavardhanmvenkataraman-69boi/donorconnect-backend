package com.donorconnect.transfusionservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;

/**
 * OpenFeign client for synchronous reads from blood-supply-service.
 * Used to check real-time component availability before confirming crossmatch.
 */
@FeignClient(name = "blood-supply-service", path = "/blood")
public interface BloodSupplyFeignClient {
    @GetMapping("/components/{id}")
    Map<String, Object> getComponentById(@PathVariable Long id);

    @GetMapping("/components/available")
    List<Map<String, Object>> getAvailableComponents();

    @GetMapping("/inventory")
    List<Map<String, Object>> getInventory();
}
