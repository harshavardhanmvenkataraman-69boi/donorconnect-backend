package com.donorconnect.bloodsupplyservice.feign;

import com.donorconnect.bloodsupplyservice.config.FeignConfig;
import com.donorconnect.bloodsupplyservice.dto.request.InventoryEntryRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryFeignClient {
    @PostMapping("api/v1/inventory/entry")
    Map <String, Object> createEntry(@RequestBody InventoryEntryRequest request);
}
