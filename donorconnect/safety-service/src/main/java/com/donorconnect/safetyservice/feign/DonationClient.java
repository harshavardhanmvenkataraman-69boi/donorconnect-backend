package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="blood-supply-service", contextId = "DonationClient",fallback = DonationFallback.class)
public interface DonationClient {
    @GetMapping("/api/v1/donations//{donationId}")
    ApiResponse<?> getById(@PathVariable Long donationId);

}
