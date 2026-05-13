package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.InventoryBalanceDto;
import com.donorconnect.reportingservice.dto.ServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {
    @GetMapping("/api/v1/inventory")
    ServiceResponse<List<InventoryBalanceDto>> getAllInventory();
}
