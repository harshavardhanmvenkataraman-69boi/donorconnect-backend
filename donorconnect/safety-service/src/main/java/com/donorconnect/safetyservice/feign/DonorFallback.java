package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class DonorFallback implements DonorClient {

    @Override
    public ApiResponse<?> getDonorById(Long donorId) {
        return ApiResponse.error("Donor service is currently unavailable. Please try again later.");
    }
}