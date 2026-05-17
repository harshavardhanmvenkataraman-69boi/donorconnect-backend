package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class BloodComponentFallback implements BloodComponentClient {
    @Override
    public ApiResponse<?> getById(Long componentId) {
        return ApiResponse.error("Blood Component Service is currently unavailable. Please try again later.");
    }

    @Override
    public ApiResponse<?> getByDonationId(Long donationId) {
        return ApiResponse.error("Blood supply service is currently unavailable. Please try again later.");
    }
}