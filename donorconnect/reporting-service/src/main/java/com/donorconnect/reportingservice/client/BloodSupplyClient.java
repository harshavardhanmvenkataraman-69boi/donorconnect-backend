package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "blood-supply-service", fallback = BloodSupplyClientFallback.class)
public interface BloodSupplyClient {

    // paginated - returns Page wrapped in ApiResponse
    @GetMapping("/api/v1/donations")
    ServiceResponse<PageResponse<DonationDto>> getAllDonations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size);

    // plain lists
    @GetMapping("/api/v1/test-results/reactive")
    ServiceResponse<List<TestResultDto>> getReactiveTestResults();

    @GetMapping("/api/v1/test-results/pending")
    ServiceResponse<List<TestResultDto>> getPendingTestResults();
}
