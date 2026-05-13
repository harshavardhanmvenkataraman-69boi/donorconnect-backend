package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.Exception.ResourceNotFoundException;
import com.donorconnect.bloodsupplyservice.dto.request.DisposalRequest;
import com.donorconnect.bloodsupplyservice.dto.request.QuarantineRequest;
import com.donorconnect.bloodsupplyservice.entity.BloodComponent;
import com.donorconnect.bloodsupplyservice.entity.DisposalRecord;
import com.donorconnect.bloodsupplyservice.entity.QuarantineAction;
import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
import com.donorconnect.bloodsupplyservice.enums.QuarantineStatus;
import com.donorconnect.bloodsupplyservice.repository.BloodComponentRepository;
import com.donorconnect.bloodsupplyservice.repository.DisposalRecordRepository;
import com.donorconnect.bloodsupplyservice.repository.QuarantineActionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Quarantine + Disposal pipeline. Replaces the old RecallService.
 *
 * KEY BUG FIX from the previous implementation: the old code created
 * QuarantineAction / DisposalRecord rows but never updated the underlying
 * BloodComponent's status. So the UI showed "success" but the component
 * still appeared AVAILABLE. This service updates both in the same
 * transaction so the state is consistent.
 *
 * Lifecycle states:
 *   AVAILABLE -> QUARANTINE (manual or auto on reactive test)
 *   QUARANTINE -> AVAILABLE (release after re-test cleared)
 *   QUARANTINE -> DISPOSED (supervisor confirms unsafe)
 *   AVAILABLE -> DISPOSED (direct disposal, e.g. expired)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuarantineDisposalService {

    private final QuarantineActionRepository quarantineActionRepository;
    private final DisposalRecordRepository disposalRecordRepository;
    private final BloodComponentRepository bloodComponentRepository;

    // ============ QUARANTINE ============

    @Transactional
    public QuarantineAction quarantine(QuarantineRequest req) {
        BloodComponent component = bloodComponentRepository.findById(req.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", req.getComponentId()));

        if (component.getStatus() == ComponentStatus.DISPOSED) {
            throw new IllegalStateException("Component already disposed — cannot quarantine.");
        }
        if (component.getStatus() == ComponentStatus.QUARANTINE) {
            throw new IllegalStateException("Component is already in quarantine.");
        }

        QuarantineAction action = QuarantineAction.builder()
                .componentId(req.getComponentId())
                .startDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now())
                .reason(req.getReason())
                .status(QuarantineStatus.QUARANTINED)
                .build();
        QuarantineAction saved = quarantineActionRepository.save(action);

        // THE FIX: actually flip the component status.
        component.setStatus(ComponentStatus.QUARANTINE);
        bloodComponentRepository.save(component);

        log.info("Component {} moved to QUARANTINE. qaId={}, reason='{}'",
                component.getComponentId(), saved.getQaId(), req.getReason());
        return saved;
    }

    public Page<QuarantineAction> getAllQuarantine(Pageable pageable) {
        return quarantineActionRepository.findAll(pageable);
    }

    public QuarantineAction getQuarantineById(Long id) {
        return quarantineActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuarantineAction", id));
    }

    public List<QuarantineAction> getByComponent(Long componentId) {
        return quarantineActionRepository.findByComponentId(componentId);
    }

    public List<QuarantineAction> getActiveQuarantine() {
        return quarantineActionRepository.findByStatus(QuarantineStatus.QUARANTINED);
    }

    /**
     * Release component from quarantine -> AVAILABLE.
     * Only valid if the component is currently QUARANTINE.
     */
    @Transactional
    public QuarantineAction release(Long qaId) {
        QuarantineAction action = getQuarantineById(qaId);
        if (action.getStatus() != QuarantineStatus.QUARANTINED) {
            throw new IllegalStateException("Quarantine action is not active — cannot release.");
        }

        BloodComponent component = bloodComponentRepository.findById(action.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", action.getComponentId()));

        action.setStatus(QuarantineStatus.RELEASED);
        action.setReleasedDate(LocalDate.now());
        QuarantineAction saved = quarantineActionRepository.save(action);

        // Component is cleared — back to AVAILABLE.
        component.setStatus(ComponentStatus.AVAILABLE);
        bloodComponentRepository.save(component);

        log.info("Component {} released from quarantine -> AVAILABLE. qaId={}",
                component.getComponentId(), qaId);
        return saved;
    }

    // ============ DISPOSAL ============

    @Transactional
    public DisposalRecord createDisposal(DisposalRequest req) {
        BloodComponent component = bloodComponentRepository.findById(req.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", req.getComponentId()));

        if (component.getStatus() == ComponentStatus.DISPOSED) {
            throw new IllegalStateException("Component already disposed.");
        }
        if (component.getStatus() == ComponentStatus.ISSUED) {
            throw new IllegalStateException("Component is already issued — cannot dispose.");
        }

        DisposalRecord record = DisposalRecord.builder()
                .componentId(req.getComponentId())
                .disposalDate(req.getDisposalDate() != null ? req.getDisposalDate() : LocalDate.now())
                .disposalReason(req.getDisposalReason())
                .witness(req.getWitness())
                .notes(req.getNotes())
                .status("COMPLETED")
                .build();
        DisposalRecord saved = disposalRecordRepository.save(record);

        // If component was in quarantine, close the active quarantine action.
        if (component.getStatus() == ComponentStatus.QUARANTINE) {
            quarantineActionRepository
                    .findByComponentIdAndStatus(component.getComponentId(), QuarantineStatus.QUARANTINED)
                    .forEach(qa -> {
                        qa.setStatus(QuarantineStatus.RELEASED); // we don't have a "DISPOSED" QuarantineStatus, mark released to close it
                        qa.setReleasedDate(LocalDate.now());
                        quarantineActionRepository.save(qa);
                    });
        }

        // THE FIX: flip component status to DISPOSED.
        component.setStatus(ComponentStatus.DISPOSED);
        bloodComponentRepository.save(component);

        log.info("Component {} DISPOSED. disposalId={}, reason='{}'",
                component.getComponentId(), saved.getDisposalId(), req.getDisposalReason());
        return saved;
    }

    public Page<DisposalRecord> getAllDisposals(Pageable pageable) {
        return disposalRecordRepository.findAll(pageable);
    }

    public DisposalRecord getDisposalById(Long id) {
        return disposalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DisposalRecord", id));
    }

    public List<DisposalRecord> getDisposalsByComponent(Long componentId) {
        return disposalRecordRepository.findByComponentId(componentId);
    }
}
