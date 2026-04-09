package com.donorconnect.reportingservice.kafka;

import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.ReportScope;
import com.donorconnect.reportingservice.repository.LabReportPackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class ReportingKafkaConsumer {
    private final LabReportPackRepository reportRepo;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "blood.test.reactive", groupId = "reporting-service-group")
    public void onTestReactive(Map<String, Object> event) {
        log.info("Reporting: indexing reactive test event donationId={}", event.get("donationId"));
        saveReport(ReportScope.GLOBAL, event, "REACTIVE_TEST");
    }

    @KafkaListener(topics = "transfusion.component.issued", groupId = "reporting-service-group")
    public void onComponentIssued(Map<String, Object> event) {
        log.info("Reporting: indexing component issued event issueId={}", event.get("issueId"));
        saveReport(ReportScope.PERIOD, event, "COMPONENT_ISSUED");
    }

    @KafkaListener(topics = "safety.alert", groupId = "reporting-service-group")
    public void onSafetyAlert(Map<String, Object> event) {
        log.info("Reporting: indexing safety alert reactionId={}", event.get("reactionId"));
        saveReport(ReportScope.GLOBAL, event, "REACTION_ALERT");
    }

    private void saveReport(ReportScope scope, Object data, String type) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", type, "data", data));
            reportRepo.save(LabReportPack.builder().scope(scope).metricsJson(json).build());
        } catch (Exception e) {
            log.error("Failed to save report: {}", e.getMessage());
        }
    }
}
