package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "donor-service", contextId = "DonorClient",
        fallback  = DonorFallback.class)
public interface DonorClient {
    @GetMapping("/api/v1/donors/{donorId}")
    ApiResponse<?> getDonorById(@PathVariable Long donorId);
}