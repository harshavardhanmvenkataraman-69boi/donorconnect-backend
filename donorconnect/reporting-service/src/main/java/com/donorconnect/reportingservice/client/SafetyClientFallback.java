package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.ReactionDto;
import com.donorconnect.reportingservice.dto.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class SafetyClientFallback implements SafetyClient {
    @Override
    public ServiceResponse<List<ReactionDto>> getAllReactions(int page, int size) {
        log.warn("SafetyClient fallback: safety-service unavailable");
        ServiceResponse<List<ReactionDto>> r = new ServiceResponse<>();
        r.setData(Collections.emptyList());
        return r;
    }
}
