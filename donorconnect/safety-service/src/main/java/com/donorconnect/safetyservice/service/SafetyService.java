package com.donorconnect.safetyservice.service;

import com.donorconnect.safetyservice.entity.*;
import com.donorconnect.safetyservice.enums.ReactionStatus;
import com.donorconnect.safetyservice.kafka.SafetyKafkaProducer;
import com.donorconnect.safetyservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class SafetyService {
    private final ReactionRepository reactionRepo;
    private final LookbackTraceRepository lookbackRepo;
    private final SafetyKafkaProducer kafkaProducer;

    public List<Reaction> getAllReactions() { return reactionRepo.findAll(); }
    public List<Reaction> getReactionsByPatient(Long patientId) { return reactionRepo.findByPatientId(patientId); }

    @Transactional
    public Reaction logReaction(Reaction reaction) {
        Reaction saved = reactionRepo.save(reaction);
        kafkaProducer.publishSafetyAlert(Map.of(
            "reactionId", saved.getReactionId(),
            "patientId", saved.getPatientId(),
            "severity", saved.getSeverity().name()
        ));
        return saved;
    }

    @Transactional
    public Reaction updateReactionStatus(Long id, ReactionStatus status) {
        Reaction r = reactionRepo.findById(id).orElseThrow();
        r.setStatus(status);
        return reactionRepo.save(r);
    }

    public List<LookbackTrace> getTracesByDonation(Long donationId) { return lookbackRepo.findByDonationId(donationId); }
    public LookbackTrace saveTrace(LookbackTrace trace) { return lookbackRepo.save(trace); }
}
