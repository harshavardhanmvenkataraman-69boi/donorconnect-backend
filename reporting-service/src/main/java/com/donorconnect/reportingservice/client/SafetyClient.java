package com.donorconnect.reportingservice.client;
import com.donorconnect.reportingservice.dto.TestResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
@FeignClient(name = "safety-service", fallback = SafetyClientFallback.class)
public interface SafetyClient {
    @GetMapping("/api/v1/test-results")
    List<TestResultDto> getAllTestResults();
}
