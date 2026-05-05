package com.donorconnect.reportingservice.client;
import com.donorconnect.reportingservice.dto.TestResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
@Component @Slf4j
public class SafetyClientFallback implements SafetyClient {
    @Override public List<TestResultDto> getAllTestResults() { log.warn("SafetyClient fallback"); return Collections.emptyList(); }
}
