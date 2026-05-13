package com.donorconnect.billingservice.feign;

import com.donorconnect.billingservice.dto.ApiResponse;
import com.donorconnect.billingservice.dto.IssueDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="transfusion-service")
public interface TransfusionServiceClient {

    @GetMapping("/api/v1/issue/{issueId}")
    public ResponseEntity<ApiResponse<IssueDto>> getIssueById(@PathVariable Long issueId);
}
