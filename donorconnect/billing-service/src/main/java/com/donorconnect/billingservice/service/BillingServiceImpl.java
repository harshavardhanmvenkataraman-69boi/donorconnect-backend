package com.donorconnect.billingservice.service;

import com.donorconnect.billingservice.dto.BillingRequestDTO;
import com.donorconnect.billingservice.dto.BillingResponseDTO;
import com.donorconnect.billingservice.dto.BillingStatusUpdateDTO;
import com.donorconnect.billingservice.enums.BillingStatus;
import com.donorconnect.billingservice.exception.DuplicateBillingException;
import com.donorconnect.billingservice.exception.InvalidBillingStatusException;
import com.donorconnect.billingservice.exception.InvalidDateRangeException;
import com.donorconnect.billingservice.exception.ResourceNotFoundException;
import com.donorconnect.billingservice.model.BillingRef;
import com.donorconnect.billingservice.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;

    /**
     * Allowed status transitions per the spec workflow:
     *   PENDING   → EXPORTED | CANCELLED
     *   EXPORTED  → terminal
     *   CANCELLED → terminal
     */
    private static final Map<BillingStatus, Set<BillingStatus>> TRANSITIONS = Map.of(
            BillingStatus.PENDING,   Set.of(BillingStatus.EXPORTED, BillingStatus.CANCELLED),
            BillingStatus.EXPORTED,  Set.of(),
            BillingStatus.CANCELLED, Set.of()
    );

    // ── Commands ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BillingResponseDTO createBilling(BillingRequestDTO request) {
        billingRepository.findByIssueId(request.getIssueId()).ifPresent(existing -> {
            throw new DuplicateBillingException(request.getIssueId());
        });

        BillingStatus status = request.getStatus() != null
                ? request.getStatus()
                : BillingStatus.PENDING;

        BillingRef saved = billingRepository.save(BillingRef.builder()
                .issueId(request.getIssueId())
                .chargeAmount(request.getChargeAmount())
                .chargeType(request.getChargeType())
                .billingDate(request.getBillingDate())
                .status(status)
                .build());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BillingResponseDTO updateBillingStatus(Integer billingId, BillingStatusUpdateDTO statusUpdate) {
        BillingRef billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing", "billingId", billingId));

        BillingStatus current = billing.getStatus();
        BillingStatus target  = statusUpdate.getStatus();

        if (current == target) {
            // idempotent — no transition needed, just echo current state
            return mapToResponse(billing);
        }

        Set<BillingStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidBillingStatusException(current.name(), target.name());
        }

        billing.setStatus(target);
        return mapToResponse(billingRepository.save(billing));
    }

    @Override
    @Transactional
    public List<BillingResponseDTO> markExported(LocalDate from, LocalDate to) {
        validateRange(from, to);

        List<BillingRef> rows = billingRepository.findForExport(from, to, BillingStatus.PENDING);
        List<BillingResponseDTO> result = new ArrayList<>(rows.size());
        for (BillingRef b : rows) {
            b.setStatus(BillingStatus.EXPORTED);
            result.add(mapToResponse(billingRepository.save(b)));
        }
        return result;
    }

    // ── Queries ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<BillingResponseDTO> getAllBillings(BillingStatus status, Pageable pageable) {
        Page<BillingRef> page = (status == null)
                ? billingRepository.findAll(pageable)
                : billingRepository.findByStatus(status, pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO getBillingById(Integer billingId) {
        return billingRepository.findById(billingId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Billing", "billingId", billingId));
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO getBillingByIssueId(Integer issueId) {
        return billingRepository.findByIssueId(issueId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Billing", "issueId", issueId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> exportBillings(LocalDate from, LocalDate to, BillingStatus status) {
        validateRange(from, to);

        List<BillingResponseDTO> out = new ArrayList<>();
        for (BillingRef b : billingRepository.findForExport(from, to, status)) {
            out.add(mapToResponse(b));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BillingStatus s : BillingStatus.values()) {
            counts.put(s.name(), billingRepository.countByStatus(s));
        }
        counts.put("TOTAL", billingRepository.count());
        return counts;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new InvalidDateRangeException(from, to);
        }
    }

    private BillingResponseDTO mapToResponse(BillingRef b) {
        return BillingResponseDTO.builder()
                .billingId(b.getBillingId())
                .issueId(b.getIssueId())
                .chargeAmount(b.getChargeAmount())
                .chargeType(b.getChargeType())
                .billingDate(b.getBillingDate())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
