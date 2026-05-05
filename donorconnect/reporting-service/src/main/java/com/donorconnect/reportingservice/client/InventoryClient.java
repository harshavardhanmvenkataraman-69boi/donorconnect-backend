package com.donorconnect.reportingservice.client;
import com.donorconnect.reportingservice.dto.BloodComponentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {
    @GetMapping("/api/v1/components")
    List<BloodComponentDto> getAllComponents();
}
