package com.donorconnect.billingservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {

    com.donorconnect.billingservice.dto.BillingResponseDTO createBilling(com.donorconnect.billingservice.dto.BillingRequestDTO request);

    Page<com.donorconnect.billingservice.dto.BillingResponseDTO> getAllBillings(Pageable pageable);

    com.donorconnect.billingservice.dto.BillingResponseDTO getBillingById(Integer billingId);

    com.donorconnect.billingservice.dto.BillingResponseDTO getBillingByIssueId(Integer issueId);

    List<com.donorconnect.billingservice.dto.BillingResponseDTO> exportBillings(LocalDate from, LocalDate to);

    com.donorconnect.billingservice.dto.BillingResponseDTO updateBillingStatus(Integer billingId, com.donorconnect.billingservice.dto.BillingStatusUpdateDTO statusUpdate);
}
