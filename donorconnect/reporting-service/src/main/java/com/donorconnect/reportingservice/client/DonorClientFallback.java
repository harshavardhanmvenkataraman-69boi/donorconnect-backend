package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.DeferralDto;
import com.donorconnect.reportingservice.dto.DonorDto;
import com.donorconnect.reportingservice.dto.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class DonorClientFallback implements DonorClient {
    private <T> ServiceResponse<List<T>> empty() {
        ServiceResponse<List<T>> r = new ServiceResponse<>();
        r.setData(Collections.emptyList());
        return r;
    }

    @Override
    public ServiceResponse<List<DonorDto>> getAllDonors(String name, String bloodGroup) {
        log.warn("DonorClient fallback: donor-service unavailable");
        return empty();
    }

    @Override
    public ServiceResponse<List<DeferralDto>> getActiveDeferrals() {
        log.warn("DonorClient fallback: donor-service unavailable");
        return empty();
    }
}
