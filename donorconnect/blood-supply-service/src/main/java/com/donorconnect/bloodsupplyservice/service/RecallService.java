package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.dto.request.DisposalRequest;
import com.donorconnect.bloodsupplyservice.dto.request.QuarantineRequest;
import com.donorconnect.bloodsupplyservice.dto.request.RecallRequest;
import com.donorconnect.bloodsupplyservice.entity.DisposalRecord;
import com.donorconnect.bloodsupplyservice.entity.QuarantineAction;
import com.donorconnect.bloodsupplyservice.entity.RecallNotice;
import com.donorconnect.bloodsupplyservice.enums.QuarantineStatus;
import com.donorconnect.bloodsupplyservice.enums.RecallStatus;
import com.donorconnect.bloodsupplyservice.repository.DisposalRecordRepository;
import com.donorconnect.bloodsupplyservice.repository.QuarantineActionRepository;
import com.donorconnect.bloodsupplyservice.repository.RecallNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecallService {

    private final RecallNoticeRepository recallNoticeRepository;
    private final QuarantineActionRepository quarantineActionRepository;
    private final DisposalRecordRepository disposalRecordRepository;

    // ===================== RECALL =====================

    public RecallNotice createRecall(RecallRequest req) {
        RecallNotice recall = RecallNotice.builder()
                .donationId(req.getDonationId())
                .componentId(req.getComponentId())
                .reason(req.getReason())
                .noticeDate(LocalDate.now())
                .status(RecallStatus.OPEN)
                .build();

        return recallNoticeRepository.save(recall);
    }

    public Page<RecallNotice> getAllRecalls(Pageable pageable) {
        return recallNoticeRepository.findAll(pageable);
    }

    public RecallNotice getRecallById(Long id) {
        return recallNoticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecallNotice", String.valueOf(id)));
    }

    public List<RecallNotice> getRecallsByDonation(Long donationId) {
        return recallNoticeRepository.findByDonationId(donationId);
    }

    public List<RecallNotice> getOpenRecalls() {
        return recallNoticeRepository.findByStatus(RecallStatus.OPEN);
    }

    public RecallNotice closeRecall(Long id) {
        RecallNotice recall = getRecallById(id);
        recall.setStatus(RecallStatus.CLOSED);
        return recallNoticeRepository.save(recall);
    }

    // ===================== QUARANTINE =====================

    public QuarantineAction quarantine(QuarantineRequest req) {
        QuarantineAction action = QuarantineAction.builder()
                .componentId(req.getComponentId())
                .startDate(LocalDate.now())
                .reason(req.getReason())
                .status(QuarantineStatus.QUARANTINED)
                .build();

        return quarantineActionRepository.save(action);
    }

    public Page<QuarantineAction> getAllQuarantine(Pageable pageable) {
        return quarantineActionRepository.findAll(pageable);
    }

    public QuarantineAction getQuarantineById(Long id) {
        return quarantineActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuarantineAction", String.valueOf(id)));
    }

    public List<QuarantineAction> getByComponent(Long componentId) {
        return quarantineActionRepository.findByComponentId(componentId);
    }

    public List<QuarantineAction> getActiveQuarantine() {
        return quarantineActionRepository.findByStatus(QuarantineStatus.QUARANTINED);
    }

    public QuarantineAction release(Long id) {
        QuarantineAction action = getQuarantineById(id);
        action.setStatus(QuarantineStatus.RELEASED);
        action.setReleasedDate(LocalDate.now());
        return quarantineActionRepository.save(action);
    }

    // ===================== DISPOSAL =====================


    public DisposalRecord createDisposal(DisposalRequest req) {
        DisposalRecord record = DisposalRecord.builder()
                .componentId(req.getComponentId())
                .disposalDate(LocalDate.now())
                .disposalReason(req.getDisposalReason())
                .witness(req.getWitness())
                .notes(req.getNotes())
                .status("COMPLETED")
                .build();

        return disposalRecordRepository.save(record);
    }

    public Page<DisposalRecord> getAllDisposals(Pageable pageable) {
        return disposalRecordRepository.findAll(pageable);
    }

    public DisposalRecord getDisposalById(Long id) {
        return disposalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisposalRecord", String.valueOf(id)));
    }

    public List<DisposalRecord> getDisposalsByComponent(Long componentId) {
        return disposalRecordRepository.findByComponentId(componentId);
    }
}