package com.donorconnect.billingservice.service;

import com.donorconnect.billingservice.dto.BillingRequestDTO;
import com.donorconnect.billingservice.dto.BillingResponseDTO;
import com.donorconnect.billingservice.dto.BillingStatusUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {

    BillingResponseDTO createBilling(BillingRequestDTO request);

    Page<BillingResponseDTO> getAllBillings(Pageable pageable);

    BillingResponseDTO getBillingById(Integer billingId);

    BillingResponseDTO getBillingByIssueId(Integer issueId);

    List<BillingResponseDTO> exportBillings(LocalDate from, LocalDate to);

    BillingResponseDTO updateBillingStatus(Integer billingId, BillingStatusUpdateDTO statusUpdate);
}
