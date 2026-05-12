package com.donorconnect.safetyservice.service;

import com.donorconnect.safetyservice.dto.request.*;
import com.donorconnect.safetyservice.dto.response.*;
import com.donorconnect.safetyservice.entity.*;
import com.donorconnect.safetyservice.enums.*;
import com.donorconnect.safetyservice.exception.*;
import com.donorconnect.safetyservice.feign.*;
import com.donorconnect.safetyservice.repository.*;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyService {

    private final ReactionRepository reactionRepository;
    private final LookbackTraceRepository lookbackTraceRepository;
    private final BloodIssueClient bloodIssueClient;
    private final BloodComponentClient bloodComponentClient;
    private final DonationClient donationClient;

    // --- REACTIONS ---
    public Reaction create(ReactionRequest req) {

       ApiResponse<?> issueResponse= bloodIssueClient.getIssueById(req.getIssueId());
       if(!issueResponse.isSuccess()){
           throw new ServiceUnavailableException("Transfusion service is currently unavailable. Please try again later.");

       }

        Reaction r = Reaction.builder()
                .issueId(req.getIssueId())
                .patientId(req.getPatientId())
                .reactionType(req.getReactionType())
                .severity(req.getSeverity())
                .reactionDate(req.getReactionDate() != null ? req.getReactionDate() : LocalDate.now())
                .notes(req.getNotes())
                .status(ReactionStatus.PENDING)
                .build();
        return reactionRepository.save(r);
    }

    public Page<Reaction> getAll(Pageable pageable) {
        return reactionRepository.findAll(pageable);
    }

    public Reaction getById(Long id) {
        return reactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reaction", id));
    }

    public List<Reaction> getReactionsByPatient(Long patientId) {
        return reactionRepository.findByPatientId(patientId);
    }

    public List<Reaction> getBySeverity(Severity severity) {
        return reactionRepository.findBySeverity(severity);
    }

    public Reaction update(Long id, ReactionRequest req) {
        Reaction r = getById(id);
        if (req.getReactionType() != null) r.setReactionType(req.getReactionType());
        if (req.getSeverity() != null) r.setSeverity(req.getSeverity());
        if (req.getNotes() != null) r.setNotes(req.getNotes());
        return reactionRepository.save(r);
    }

    public Reaction updateStatus(Long id, ReactionStatus status) {
        Reaction r = getById(id);
        r.setStatus(status);
        return reactionRepository.save(r);
    }

    // --- LOOKBACK ---
    public LookbackTrace createTrace(LookbackRequest req) {
       ApiResponse<?> donationResponse= donationClient.getById(req.getDonationId());
       if(!donationResponse.isSuccess()){
              throw new ServiceUnavailableException("Blood supply service is currently unavailable. Please try again later.");
       }
        ApiResponse<?> componentResponse=  bloodComponentClient.getById(req.getComponentId());
        if(!componentResponse.isSuccess()){
            throw new ServiceUnavailableException("Blood supply service is currently unavailable. Please try again later.");
        }
        LookbackTrace t = LookbackTrace.builder()
                .donationId(req.getDonationId())
                .componentId(req.getComponentId())
                .patientId(req.getPatientId())
                .traceDate(LocalDate.now())
                .status("ACTIVE")
                .build();
        return lookbackTraceRepository.save(t);
    }

    public List<LookbackTrace> getByDonation(Long donationId) {
        return lookbackTraceRepository.findByDonationId(donationId);
    }

    public List<LookbackTrace> getLookbackByPatient(Long patientId) {
        return lookbackTraceRepository.findByPatientId(patientId);
    }

    public List<LookbackTrace> getByComponent(Long componentId) {
        return lookbackTraceRepository.findByComponentId(componentId);
    }

    // NEW — get componentId from issueId
    // BloodIssueClient calls: /transfusion/api/v1/issue/{issueId}
    // IssueRecord fields: issueId, componentId, patientId, ...
    // FIX: Jackson deserializes numbers as Integer, use Number.longValue() not Long.valueOf()
    public Long getComponentIdByIssue(Long issueId) {
        ApiResponse<?> response = bloodIssueClient.getIssueById(issueId);
        if (!response.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Transfusion service is currently unavailable. Please try again later.");
        }
        Long componentId = extractLongField(response.getData(), "componentId");
        if (componentId == null) {
            log.error("componentId not found in issue response for issueId={}: {}", issueId, response.getData());
            throw new ResourceNotFoundException("ComponentId for Issue", issueId);
        }
        log.info("Fetched componentId={} for issueId={}", componentId, issueId);
        return componentId;
    }

    // NEW — get donationId from componentId
    // BloodComponentClient calls: /api/v1/components/{componentId}
    // BloodComponent fields: componentId, donationId, componentType, ...
    public Long getDonationIdByComponent(Long componentId) {
        ApiResponse<?> response = bloodComponentClient.getById(componentId);
        if (!response.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Blood supply service is currently unavailable. Please try again later.");
        }
        Long donationId = extractLongField(response.getData(), "donationId");
        if (donationId == null) {
            log.error("donationId not found in component response for componentId={}: {}", componentId, response.getData());
            throw new ResourceNotFoundException("DonationId for Component", componentId);
        }
        log.info("Fetched donationId={} for componentId={}", donationId, componentId);
        return donationId;
    }

    // Helper — safely extracts a Long from a Map returned by Feign
    // Jackson deserializes JSON numbers as Integer (if value fits) or Long
    // Using Number.longValue() handles both cases safely
    @SuppressWarnings("unchecked")
    private Long extractLongField(Object data, String fieldName) {
        if (data instanceof Map<?, ?> dataMap) {
            Object value = ((Map<String, Object>) dataMap).get(fieldName);
            if (value instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }
}