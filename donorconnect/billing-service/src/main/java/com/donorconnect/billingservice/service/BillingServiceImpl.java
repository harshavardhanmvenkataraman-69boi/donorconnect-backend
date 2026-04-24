package com.donorconnect.billingservice.service;

import com.donorconnect.billingservice.dto.BillingRequestDTO;
import com.donorconnect.billingservice.dto.BillingResponseDTO;
import com.donorconnect.billingservice.dto.BillingStatusUpdateDTO;
import com.donorconnect.billingservice.exception.DuplicateBillingException;
import com.donorconnect.billingservice.exception.InvalidBillingStatusException;
import com.donorconnect.billingservice.exception.InvalidDateRangeException;
import com.donorconnect.billingservice.exception.ResourceNotFoundException;
import com.donorconnect.billingservice.model.BillingRef;
import com.donorconnect.billingservice.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    public BillingResponseDTO createBilling(BillingRequestDTO request) {
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
        Page<BillingRef> billingPage = billingRepository.findAll(pageable);

        // Convert each BillingRef to BillingResponseDTO using traditional for-loop
        List<BillingResponseDTO> responseList = new ArrayList<>();
        for (BillingRef billing : billingPage.getContent()) {
            responseList.add(mapToResponse(billing));
        }

        // Create a new Page with converted DTOs
        return new PageImpl<>(responseList, pageable, billingPage.getTotalElements());
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

        // Fetch all billings in the date range
        List<BillingRef> billings = billingRepository.findByBillingDateBetween(from, to);

        // Convert to DTOs using traditional for-loop
        List<BillingResponseDTO> responseList = new ArrayList<>();
        for (BillingRef billing : billings) {
            responseList.add(mapToResponse(billing));
        }

        return responseList;
    }

    @Override
    @Transactional
    public BillingResponseDTO updateBillingStatus(Integer billingId, BillingStatusUpdateDTO statusUpdate) {
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
        return BillingResponseDTO.builder()
                .billingId(billing.getBillingId())
                .issueId(billing.getIssueId())
                .chargeAmount(billing.getChargeAmount())
                .chargeType(billing.getChargeType())
                .billingDate(billing.getBillingDate())
                .status(billing.getStatus())
                .build();
    }
}
