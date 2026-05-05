package com.donorconnect.reportingservice.client;
import com.donorconnect.reportingservice.dto.DonorDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
@FeignClient(name = "donor-service", fallback = DonorClientFallback.class)
public interface DonorClient {
    @GetMapping("/api/v1/donors")
    List<DonorDto> getAllDonors();
    @GetMapping("/api/v1/donations")
    List<com.donorconnect.reportingservice.dto.DonationDto> getAllDonations();
    @GetMapping("/api/v1/deferrals")
    List<com.donorconnect.reportingservice.dto.DeferralDto> getAllDeferrals();
}
