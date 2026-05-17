package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.DeferralDto;
import com.donorconnect.reportingservice.dto.DonorDto;
import com.donorconnect.reportingservice.dto.ServiceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "donor-service", fallback = DonorClientFallback.class)
public interface DonorClient {

    // /search with no params returns all donors as plain List (not paginated)
    @GetMapping("/api/v1/donors/search")
    ServiceResponse<List<DonorDto>> getAllDonors(
            @RequestParam(required = false) String name,
            @RequestParam(name = "blood-group", required = false) String bloodGroup);

    // /active returns plain List
    @GetMapping("/api/v1/deferrals/active")
    ServiceResponse<List<DeferralDto>> getActiveDeferrals();
}
