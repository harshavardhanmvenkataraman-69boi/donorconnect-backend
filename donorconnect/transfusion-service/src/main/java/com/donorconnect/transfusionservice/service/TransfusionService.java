package com.donorconnect.transfusionservice.service;

import com.donorconnect.transfusionservice.dto.request.CrossmatchRequestDto;
import com.donorconnect.transfusionservice.dto.request.CrossmatchResultRequest;
import com.donorconnect.transfusionservice.dto.request.IssueRequestDto;
import com.donorconnect.transfusionservice.entity.*;
import com.donorconnect.transfusionservice.enums.*;
import com.donorconnect.transfusionservice.exception.ResourceNotFoundException;
import com.donorconnect.transfusionservice.feign.BloodSupplyFeignClient;
import com.donorconnect.transfusionservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class TransfusionService {
    private final CrossmatchRequestRepository crossmatchRequestRepository;
    private final CrossmatchResultRepository crossmatchResultRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final BloodSupplyFeignClient bloodSupplyFeignClient;

    // --- CROSSMATCH REQUESTS ---

    public CrossmatchRequest createRequest(CrossmatchRequestDto req) {
        CrossmatchRequest r = CrossmatchRequest.builder()
                .patientId(req.getPatientId())
                .orderBy(req.getOrderBy())
                .bloodGroup(req.getBloodGroup())
                .rhFactor(req.getRhFactor())
                .requiredUnits(req.getRequiredUnits())
                .priority(req.getPriority() != null ? req.getPriority() : CrossmatchPriority.ROUTINE)
                .requestDate(LocalDate.now())
                .status(CrossmatchStatus.PENDING)
                .build();
        return crossmatchRequestRepository.save(r);
    }

    public Page<CrossmatchRequest> getAllRequests(Pageable pageable) {
        return crossmatchRequestRepository.findAll(pageable);
    }

    public CrossmatchRequest getRequestById(Long id) {
        return crossmatchRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrossmatchRequest", id));
    }

    public List<CrossmatchRequest> getRequestsByPatient(Long patientId) {
        return crossmatchRequestRepository.findByPatientId(patientId);
    }

    public List<CrossmatchRequest> getPendingRequests() {
        return crossmatchRequestRepository.findByStatus(CrossmatchStatus.PENDING);
    }

    public CrossmatchRequest updateRequestStatus(Long id, CrossmatchStatus status) {
        CrossmatchRequest r = getRequestById(id);
        r.setStatus(status);
        return crossmatchRequestRepository.save(r);
    }

    // --- CROSSMATCH RESULTS ---

    public CrossmatchResult createResult(CrossmatchResultRequest req) {
        // Synchronous safety check: crossmatch is allowed only when the unit is still AVAILABLE.
        Map<String, Object> componentResponse = bloodSupplyFeignClient.getComponentById(req.getComponentId());
        String status = extractComponentStatus(componentResponse);

        if (!"AVAILABLE".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Component not available for crossmatch. Status: " + status);
        }

        CrossmatchResult r = CrossmatchResult.builder()
                .requestId(req.getRequestId())
                .componentId(req.getComponentId())
                .compatibility(req.getCompatibility())
                .testedBy(req.getTestedBy())
                .testedDate(LocalDate.now())
                .status(req.getCompatibility() == Compatibility.COMPATIBLE
                        ? CrossmatchStatus.MATCHED : CrossmatchStatus.REJECTED)
                .build();
        CrossmatchResult saved = crossmatchResultRepository.save(r);

        // If compatible, reserve the unit immediately to prevent parallel allocation.
        if (req.getCompatibility() == Compatibility.COMPATIBLE) {
            CrossmatchRequest request = getRequestById(req.getRequestId());
            request.setStatus(CrossmatchStatus.MATCHED);
            crossmatchRequestRepository.save(request);

            bloodSupplyFeignClient.updateComponentStatus(req.getComponentId(), "RESERVED");
        }
        return saved;
    }

    public CrossmatchResult getResultById(Long id) {
        return crossmatchResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrossmatchResult", id));
    }

    public List<CrossmatchResult> getResultsByRequest(Long requestId) {
        return crossmatchResultRepository.findByRequestId(requestId);
    }

    // --- ISSUE ---

    public IssueRecord issue(IssueRequestDto req) {
        IssueRecord r = IssueRecord.builder()
                .componentId(req.getComponentId())
                .patientId(req.getPatientId())
                .issueDate(LocalDate.now())
                .issuedBy(req.getIssuedBy())
                .indication(req.getIndication())
                .status(IssueStatus.ISSUED)
                .build();
        IssueRecord saved = issueRecordRepository.save(r);

        bloodSupplyFeignClient.updateComponentStatus(req.getComponentId(), "ISSUED");
        return saved;
    }

    public Page<IssueRecord> getAllIssues(Pageable pageable) {
        return issueRecordRepository.findAll(pageable);
    }

    public IssueRecord getIssueById(Long id) {
        return issueRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IssueRecord", id));
    }

    public List<IssueRecord> getIssuesByPatient(Long patientId) {
        return issueRecordRepository.findByPatientId(patientId);
    }

    public List<IssueRecord> getIssuesByComponent(Long componentId) {
        return issueRecordRepository.findByComponentId(componentId);
    }

    public IssueRecord returnUnit(Long id) {
        IssueRecord r = getIssueById(id);
        r.setStatus(IssueStatus.RETURNED);
        return issueRecordRepository.save(r);
    }

    @SuppressWarnings("unchecked")
    private String extractComponentStatus(Map<String, Object> response) {
        Object directStatus = response.get("status");
        if (directStatus != null) {
            return String.valueOf(directStatus);
        }

        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object nestedStatus = ((Map<String, Object>) dataMap).get("status");
            if (nestedStatus != null) {
                return String.valueOf(nestedStatus);
            }
        }

        throw new IllegalStateException("Unable to read component status from blood-supply response");
    }
}
