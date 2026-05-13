package com.donorconnect.billingservice.service;

import com.donorconnect.billingservice.feign.TransfusionServiceClient;
import com.donorconnect.billingservice.exception.DuplicateBillingException;
import com.donorconnect.billingservice.exception.InvalidBillingStatusException;
import com.donorconnect.billingservice.exception.InvalidDateRangeException;
import com.donorconnect.billingservice.exception.ResourceNotFoundException;
import com.donorconnect.billingservice.model.BillingRef;
import com.donorconnect.billingservice.repository.BillingRepository;
import com.donorconnect.billingservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final TransfusionServiceClient transfusionServiceClient;

    // Allowed status values
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "PAID", "CANCELLED", "OVERDUE");

    // Allowed status transitions: current → set of valid next statuses
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
            "PENDING",   Set.of("PAID", "CANCELLED", "OVERDUE"),
            "OVERDUE",   Set.of("PAID", "CANCELLED"),
            "PAID",      Set.of(),        // terminal
            "CANCELLED", Set.of()         // terminal
    );

    @Override
    @Transactional
    public BillingResponseDTO createBilling(com.donorconnect.billingservice.dto.BillingRequestDTO request) {
        // Guard: no duplicate billing for the same issue
        billingRepository.findByIssueId(request.getIssueId()).ifPresent(existing -> {
            throw new DuplicateBillingException(request.getIssueId());
        });

        // Guard: status must be valid
        validateStatus(request.getStatus());

        BillingRef billing = BillingRef.builder()
                .issueId(request.getIssueId())
                .chargeAmount(request.getChargeAmount())
                .chargeType(request.getChargeType())
                .billingDate(request.getBillingDate())
                .status(request.getStatus())
                .build();

        return mapToResponse(billingRepository.save(billing));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillingResponseDTO> getAllBillings(Pageable pageable) {
        return billingRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO getBillingById(Integer billingId) {
        BillingRef billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing", "billingId", billingId));
        return mapToResponse(billing);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingResponseDTO getBillingByIssueId(Integer issueId) {
        BillingRef billing = billingRepository.findByIssueId(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing", "issueId", issueId));
        return mapToResponse(billing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingResponseDTO> exportBillings(LocalDate from, LocalDate to) {
        // Guard: from must not be after to
        if (from.isAfter(to)) {
            throw new InvalidDateRangeException(from, to);
        }
        List<BillingResponseDTO> result = new ArrayList<>();
        for (BillingRef billing : billingRepository.findByBillingDateBetween(from, to)) {
            result.add(mapToResponse(billing));
        }
        return result;
    }

    @Override
    @Transactional
    public BillingResponseDTO updateBillingStatus(Integer billingId, com.donorconnect.billingservice.dto.BillingStatusUpdateDTO statusUpdate) {
        BillingRef billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing", "billingId", billingId));

        String newStatus = statusUpdate.getStatus().toUpperCase();

        // Guard: new status must be a known value
        validateStatus(newStatus);

        // Guard: transition must be permitted
        String currentStatus = billing.getStatus().toUpperCase();
        Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidBillingStatusException(currentStatus, newStatus);
        }

        billing.setStatus(newStatus);
        return mapToResponse(billingRepository.save(billing));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private void validateStatus(String status) {
        if (status == null || !ALLOWED_STATUSES.contains(status.toUpperCase())) {
            throw new InvalidBillingStatusException(status);
        }
    }

    private BillingResponseDTO mapToResponse(BillingRef billing) {
        return com.donorconnect.billingservice.dto.BillingResponseDTO.builder()
                .billingId(billing.getBillingId())
                .issueId(billing.getIssueId())
                .chargeAmount(billing.getChargeAmount())
                .chargeType(billing.getChargeType())
                .billingDate(billing.getBillingDate())
                .status(billing.getStatus())
                .build();
    }
}
