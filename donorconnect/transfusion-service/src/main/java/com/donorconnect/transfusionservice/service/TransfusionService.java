package com.donorconnect.transfusionservice.service;

import com.donorconnect.transfusionservice.entity.*;
import com.donorconnect.transfusionservice.enums.*;
import com.donorconnect.transfusionservice.feign.BloodSupplyFeignClient;
import com.donorconnect.transfusionservice.kafka.*;
import com.donorconnect.transfusionservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class TransfusionService {
    private final CrossmatchRequestRepository requestRepo;
    private final CrossmatchResultRepository resultRepo;
    private final IssueRecordRepository issueRepo;
    private final BloodSupplyFeignClient bloodSupplyClient;
    private final TransfusionKafkaProducer kafkaProducer;

    public CrossmatchRequest createRequest(CrossmatchRequest req) { return requestRepo.save(req); }
    public List<CrossmatchRequest> getPendingRequests() { return requestRepo.findByStatus(CrossmatchRequestStatus.PENDING); }

    /**
     * Confirm a crossmatch result using a synchronous Feign call to blood-supply-service
     * to verify the component is still AVAILABLE before recording the match.
     */
    @Transactional
    public CrossmatchResult confirmCrossmatch(CrossmatchResult result) {
        // Synchronous check via Feign
        Map<String, Object> component = bloodSupplyClient.getComponentById(result.getComponentId());
        String status = (String) component.get("status");
        if (!"AVAILABLE".equals(status)) {
            throw new RuntimeException("Component " + result.getComponentId() + " is not available (status=" + status + ")");
        }
        result.setTestedDate(LocalDate.now());
        CrossmatchResult saved = resultRepo.save(result);

        // Update request status
        requestRepo.findById(result.getRequestId()).ifPresent(req -> {
            req.setStatus(result.getCompatibility() == Compatibility.COMPATIBLE
                    ? CrossmatchRequestStatus.MATCHED : CrossmatchRequestStatus.REJECTED);
            requestRepo.save(req);
        });
        return saved;
    }

    /**
     * Issue a blood component to a patient. Publishes an event to Kafka so that
     * blood-supply-service decrements inventory and billing-service creates a billing ref.
     */
    @Transactional
    public IssueRecord issueComponent(IssueRecord issueRecord) {
        issueRecord.setIssueDate(LocalDate.now());
        issueRecord.setStatus(IssueStatus.ISSUED);
        IssueRecord saved = issueRepo.save(issueRecord);

        // Publish event for blood-supply and billing services
        kafkaProducer.publishComponentIssued(ComponentIssuedEvent.builder()
                .componentId(saved.getComponentId())
                .patientId(saved.getPatientId())
                .issueId(saved.getIssueId())
                .chargeAmount(500.0) // Base charge; billing service calculates actual
                .timestamp(LocalDateTime.now().toString())
                .build());
        log.info("Component {} issued to patient {}", saved.getComponentId(), saved.getPatientId());
        return saved;
    }

    public List<IssueRecord> getIssuesByPatient(Long patientId) { return issueRepo.findByPatientId(patientId); }
}
