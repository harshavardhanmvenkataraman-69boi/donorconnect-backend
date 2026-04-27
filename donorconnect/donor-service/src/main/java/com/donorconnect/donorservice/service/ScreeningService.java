package com.donorconnect.donorservice.service;

import com.donorconnect.donorservice.dto.request.ScreeningRequest;
import com.donorconnect.donorservice.entity.ScreeningRecord;
import com.donorconnect.donorservice.exception.ResourceNotFoundException;
import com.donorconnect.donorservice.repository.ScreeningRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRecordRepository screeningRepo;
    private final DeferralService deferralService;

    /**
     * Creates a screening record.
     * If the donor is NOT cleared (clearedFlag = false), a deferral MUST be provided
     * in the request. The deferral is created automatically and the donor status is
     * flipped to DEFERRED atomically within the same transaction.
     */

    @Transactional
    public ScreeningRecord create(ScreeningRequest req) {
        ScreeningRecord record = ScreeningRecord.builder()
                .donorId(req.getDonorId()).screeningDate(LocalDate.now())
                .vitalsJson(req.getVitalsJson()).questionnaireJson(req.getQuestionnaireJson())
                .clearedFlag(req.getClearedFlag()).clearedBy(req.getClearedBy()).notes(req.getNotes())
                .build();
        ScreeningRecord saved = screeningRepo.save(record);

        // If donor fails screening, auto-create deferral and flip donor status
        if (Boolean.FALSE.equals(req.getClearedFlag())) {
            if (req.getDeferralRequest() == null) {
                throw new IllegalArgumentException(
                        "A deferral request is required when clearedFlag is false.");
            }
            req.getDeferralRequest().setDonorId(req.getDonorId());
            deferralService.create(req.getDeferralRequest()); // also updates donor status
        }

        return saved;
    }

    public ScreeningRecord getById(Long id) {
        return screeningRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScreeningRecord", id));
    }

    public List<ScreeningRecord> getByDonor(Long donorId) { return screeningRepo.findByDonorId(donorId); }

    /**
     * Updating a screening record's clearedFlag to false also triggers a deferral.
     * Updating it back to true does NOT automatically lift the deferral —
     * that must be done explicitly via DeferralService.lift() to maintain an audit trail.
     */
    @Transactional
    public ScreeningRecord update(Long id, ScreeningRequest req) {
        ScreeningRecord r = getById(id);
        boolean wasCleared = Boolean.TRUE.equals(r.getClearedFlag());

        if (req.getVitalsJson() != null) r.setVitalsJson(req.getVitalsJson());
        if (req.getQuestionnaireJson() != null) r.setQuestionnaireJson(req.getQuestionnaireJson());
        if (req.getClearedFlag() != null) r.setClearedFlag(req.getClearedFlag());
        if (req.getClearedBy() != null) r.setClearedBy(req.getClearedBy());
        if (req.getNotes() != null) r.setNotes(req.getNotes());
        ScreeningRecord saved = screeningRepo.save(r);

        // If screening is being flipped from cleared → not cleared, auto-create deferral
        if (wasCleared && Boolean.FALSE.equals(req.getClearedFlag())) {
            if (req.getDeferralRequest() == null) {
                throw new IllegalArgumentException(
                        "A deferral request is required when clearedFlag is changed to false.");
            }
            req.getDeferralRequest().setDonorId(r.getDonorId());
            deferralService.create(req.getDeferralRequest());
        }

        return saved;
    }
}

