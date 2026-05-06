package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;

import org.springframework.stereotype.Component;

@Component
public class BloodIssueFallback  implements BloodIssueClient{

    @Override
    public ApiResponse<?> getIssueById(Long issueId) {
        return ApiResponse.error("Transfusion service is currently unavailable. Please try again later.");
    }
    
}