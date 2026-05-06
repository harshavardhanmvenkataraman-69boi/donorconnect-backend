package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "blood-supply-service",
    contextId = "BloodComponentClient", 
    fallback = BloodComponentFallback.class
)
public interface BloodComponentClient {
    @GetMapping("/api/v1/components/{componentId}")
    ApiResponse<?> getById(@PathVariable Long componentId);
}