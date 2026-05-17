package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class BloodSupplyClientFallback implements BloodSupplyClient {

    @Override
    public ServiceResponse<PageResponse<DonationDto>> getAllDonations(int page, int size) {
        log.warn("BloodSupplyClient fallback");
        ServiceResponse<PageResponse<DonationDto>> r = new ServiceResponse<>();
        PageResponse<DonationDto> p = new PageResponse<>();
        p.setContent(Collections.emptyList());
        r.setData(p);
        return r;
    }

    @Override
    public ServiceResponse<List<TestResultDto>> getReactiveTestResults() {
        log.warn("BloodSupplyClient fallback");
        ServiceResponse<List<TestResultDto>> r = new ServiceResponse<>();
        r.setData(Collections.emptyList());
        return r;
    }

    @Override
    public ServiceResponse<List<TestResultDto>> getPendingTestResults() {
        log.warn("BloodSupplyClient fallback");
        ServiceResponse<List<TestResultDto>> r = new ServiceResponse<>();
        r.setData(Collections.emptyList());
        return r;
    }
}
