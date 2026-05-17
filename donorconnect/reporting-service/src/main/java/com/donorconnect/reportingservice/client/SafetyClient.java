package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.ReactionDto;
import com.donorconnect.reportingservice.dto.ServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "safety-service", fallback = SafetyClientFallback.class)
public interface SafetyClient {
    @GetMapping("/api/v1/reactions")
    ServiceResponse<List<ReactionDto>> getAllReactions(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "1000") int size);
}
