package com.donorconnect.bloodsupplyservice.feign;

import com.donorconnect.bloodsupplyservice.config.FeignConfig;
import com.donorconnect.bloodsupplyservice.dto.request.DeferralRequestDto;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client to donor-service for creating deferrals.
 * Called by TestResultService when a result is REACTIVE.
 * Deferral type (PERMANENT vs TEMPORARY) is decided by DeferralPolicy
 * based on which infectious-disease test was reactive.
 */
@FeignClient(
        name = "donor-service",
        contextId = "deferralFeignClient",
        path = "/api/v1/deferrals",
        configuration = FeignConfig.class
)
public interface DeferralFeignClient {

    @PostMapping
    ApiResponse<?> createDeferral(@RequestBody DeferralRequestDto request);
}
