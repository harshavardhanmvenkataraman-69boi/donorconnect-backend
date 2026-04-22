package com.donorconnect.safetyservice.service;

import com.donorconnect.safetyservice.dto.request.LookbackRequest;
import com.donorconnect.safetyservice.dto.request.ReactionRequest;
import com.donorconnect.safetyservice.dto.response.ApiResponse;
import com.donorconnect.safetyservice.entity.LookbackTrace;
import com.donorconnect.safetyservice.entity.Reaction;
import com.donorconnect.safetyservice.enums.ReactionStatus;
import com.donorconnect.safetyservice.enums.Severity;
import com.donorconnect.safetyservice.exception.ResourceNotFoundException;
import com.donorconnect.safetyservice.exception.ServiceUnavailableException;
import com.donorconnect.safetyservice.feign.BloodComponentClient;
import com.donorconnect.safetyservice.feign.BloodIssueClient;
import com.donorconnect.safetyservice.feign.DonationClient;
import com.donorconnect.safetyservice.repository.LookbackTraceRepository;
import com.donorconnect.safetyservice.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
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
}