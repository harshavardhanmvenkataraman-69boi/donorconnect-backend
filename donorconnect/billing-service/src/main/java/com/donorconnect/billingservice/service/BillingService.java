package com.donorconnect.billingservice.service;

import com.donorconnect.billingservice.dto.BillingRequestDTO;
import com.donorconnect.billingservice.dto.BillingResponseDTO;
import com.donorconnect.billingservice.dto.BillingStatusUpdateDTO;
import com.donorconnect.billingservice.enums.BillingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BillingService {

    BillingResponseDTO createBilling(BillingRequestDTO request);

    Page<BillingResponseDTO> getAllBillings(BillingStatus status, Pageable pageable);

    BillingResponseDTO getBillingById(Integer billingId);

    BillingResponseDTO getBillingByIssueId(Integer issueId);

    BillingResponseDTO updateBillingStatus(Integer billingId, BillingStatusUpdateDTO statusUpdate);

    /**
     * Returns billing records in the given date range, optionally filtered by status.
     * Does NOT mutate state — safe for repeated reads / CSV downloads.
     */
    List<BillingResponseDTO> exportBillings(LocalDate from, LocalDate to, BillingStatus status);

    /**
     * Marks all PENDING records in the given date range as EXPORTED and returns them.
     * Use when the export is being shipped to the hospital billing system for real.
     */
    List<BillingResponseDTO> markExported(LocalDate from, LocalDate to);

    /** Aggregate counts keyed by status name — used by the UI stats bar. */
    Map<String, Long> getStatusCounts();
}
