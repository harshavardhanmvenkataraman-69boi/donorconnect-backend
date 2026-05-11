package com.donorconnect.reportingservice.client;
import com.donorconnect.reportingservice.dto.DeferralDto;
import com.donorconnect.reportingservice.dto.DonationDto;
import com.donorconnect.reportingservice.dto.DonorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
@Component @Slf4j
public class DonorClientFallback implements DonorClient {
    @Override public List<DonorDto> getAllDonors() { log.warn("DonorClient fallback"); return Collections.emptyList(); }
    @Override public List<DonationDto> getAllDonations() { log.warn("DonorClient fallback"); return Collections.emptyList(); }
    @Override public List<DeferralDto> getAllDeferrals() { log.warn("DonorClient fallback"); return Collections.emptyList(); }
}
