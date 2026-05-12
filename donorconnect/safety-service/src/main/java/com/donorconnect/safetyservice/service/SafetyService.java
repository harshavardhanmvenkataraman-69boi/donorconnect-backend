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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyService {

    private final ReactionRepository reactionRepository;
    private final LookbackTraceRepository lookbackTraceRepository;
    private final BloodIssueClient bloodIssueClient;
    private final BloodComponentClient bloodComponentClient;
    private final DonationClient donationClient;
    private final DonorClient donorClient;

    // --- REACTIONS ---

    // Reaction created → status = PENDING
    public Reaction create(ReactionRequest req) {
        ApiResponse<?> issueResponse = bloodIssueClient.getIssueById(req.getIssueId());
        if (!issueResponse.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Transfusion service is currently unavailable. Please try again later.");
        }
        Reaction r = Reaction.builder()
                .issueId(req.getIssueId())
                .patientId(req.getPatientId())
                .reactionType(req.getReactionType())
                .severity(req.getSeverity())
                .reactionDate(req.getReactionDate() != null ? req.getReactionDate() : LocalDate.now())
                .notes(req.getNotes())
                .status(ReactionStatus.PENDING)   // ← always starts PENDING
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

    // Manual status update — Admin can move to CLOSED
    public Reaction updateStatus(Long id, ReactionStatus status) {
        Reaction r = getById(id);
        r.setStatus(status);
        return reactionRepository.save(r);
    }

    // --- LOOKBACK ---

    // Status flow:
    // Lookback initiated → LookbackTrace.status = TRACED
    //                    → Reaction.status = INVESTIGATING (auto)
    // Admin closes       → Reaction.status = CLOSED (manual via updateStatus)
    //                    → LookbackTrace.status = CLOSED (manual via updateLookbackStatus)
    public LookbackTrace createTrace(LookbackRequest req) {

        // Validate donation exists
        ApiResponse<?> donationResponse = donationClient.getById(req.getDonationId());
        if (!donationResponse.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Blood supply service is currently unavailable. Please try again later.");
        }

        // Validate component exists
        ApiResponse<?> componentResponse = bloodComponentClient.getById(req.getComponentId());
        if (!componentResponse.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Blood supply service is currently unavailable. Please try again later.");
        }

        // Save trace with TRACED status — chain has been successfully traced
        LookbackTrace t = LookbackTrace.builder()
                .donationId(req.getDonationId())
                .componentId(req.getComponentId())
                .patientId(req.getPatientId())
                .traceDate(LocalDate.now())
                .status(LookbackStatus.TRACED)   // ← donation→component→patient chain traced
                .build();
        LookbackTrace saved = lookbackTraceRepository.save(t);

        // Auto-update linked reaction: PENDING → INVESTIGATING
        // Only updates if reactionId is provided and reaction is still PENDING
        if (req.getReactionId() != null) {
            reactionRepository.findById(req.getReactionId()).ifPresent(reaction -> {
                if (reaction.getStatus() == ReactionStatus.PENDING) {
                    reaction.setStatus(ReactionStatus.INVESTIGATING);
                    reactionRepository.save(reaction);
                    log.info("Auto-updated reaction {} → INVESTIGATING after lookback initiated", req.getReactionId());
                }
            });
        }

        return saved;
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

    public List<LookbackTrace> getTracesByPatient(Long patientId) {
        return lookbackTraceRepository.findByPatientId(patientId);
    }

    // Admin closes the lookback trace — TRACED → CLOSED
    public LookbackTrace updateLookbackStatus(Long traceId, LookbackStatus status) {
        LookbackTrace trace = lookbackTraceRepository.findById(traceId)
                .orElseThrow(() -> new ResourceNotFoundException("LookbackTrace", traceId));
        trace.setStatus(status);
        return lookbackTraceRepository.save(trace);
    }

    // --- LOOKUP HELPERS ---

    public Long getComponentIdByIssue(Long issueId) {
        ApiResponse<?> response = bloodIssueClient.getIssueById(issueId);
        if (!response.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Transfusion service is currently unavailable. Please try again later.");
        }
        Long componentId = extractLongField(response.getData(), "componentId");
        if (componentId == null) {
            log.error("componentId not found for issueId={}", issueId);
            throw new ResourceNotFoundException("ComponentId for Issue", issueId);
        }
        log.info("Fetched componentId={} for issueId={}", componentId, issueId);
        return componentId;
    }

    public Long getDonationIdByComponent(Long componentId) {
        ApiResponse<?> response = bloodComponentClient.getById(componentId);
        if (!response.isSuccess()) {
            throw new ServiceUnavailableException(
                    "Blood supply service is currently unavailable. Please try again later.");
        }
        Long donationId = extractLongField(response.getData(), "donationId");
        if (donationId == null) {
            log.error("donationId not found for componentId={}", componentId);
            throw new ResourceNotFoundException("DonationId for Component", componentId);
        }
        log.info("Fetched donationId={} for componentId={}", donationId, componentId);
        return donationId;
    }

    // --- LOOKBACK DETAILS (Admin only) ---

    @SuppressWarnings("unchecked")
    public Map<String, Object> getLookbackDetails(Long donationId) {
        Map<String, Object> result = new LinkedHashMap<>();

        ApiResponse<?> donationRes = donationClient.getById(donationId);
        if (!donationRes.isSuccess()) {
            throw new ServiceUnavailableException("Blood supply service unavailable.");
        }
        Map<String, Object> donation = (Map<String, Object>) donationRes.getData();
        result.put("donation", donation);

        Long donorId = extractLongField(donation, "donorId");
        if (donorId != null) {
            ApiResponse<?> donorRes = donorClient.getDonorById(donorId);
            if (donorRes.isSuccess()) {
                result.put("donor", donorRes.getData());
            } else {
                log.warn("Could not fetch donor for donorId={}", donorId);
                result.put("donor", null);
            }
        }

        ApiResponse<?> componentsRes = bloodComponentClient.getByDonationId(donationId);
        if (componentsRes.isSuccess()) {
            result.put("components", componentsRes.getData());
        } else {
            log.warn("Could not fetch components for donationId={}", donationId);
            result.put("components", Collections.emptyList());
        }

        return result;
    }

    // --- HELPER ---

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