package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class DonationFallback implements DonationClient {

    @Override
    public ApiResponse<?> getById(Long donationId) {
        return ApiResponse.error("Blood supply service is currently unavailable. Please try again later.");
    }
    
}