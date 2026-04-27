package com.donorconnect.safetyservice.feign;

import com.donorconnect.safetyservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="transfusion-service",fallback = BloodIssueFallback.class)
public interface BloodIssueClient {
    @GetMapping("/transfusion/api/v1/issue/{issueId}")
    ApiResponse<?> getIssueById(@PathVariable Long issueId);
}
