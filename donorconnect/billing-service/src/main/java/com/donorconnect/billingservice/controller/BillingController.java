package com.donorconnect.billingservice.controller;


import com.donorconnect.billingservice.service.BillingService;
import com.donorconnect.billingservice.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ADMIN')") // Only admins can access billing endpoints
public class BillingController {

    private final BillingService billingService;

    // POST /billing — Create billing record
    @PostMapping
    public ResponseEntity<com.donorconnect.billingservice.dto.BillingResponseDTO> createBilling(
            @Valid @RequestBody com.donorconnect.billingservice.dto.BillingRequestDTO request) {
        com.donorconnect.billingservice.dto.BillingResponseDTO response = billingService.createBilling(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /billing — All billing records (paginated)
    @GetMapping
    public ResponseEntity<Page<com.donorconnect.billingservice.dto.BillingResponseDTO>> getAllBillings(
            @PageableDefault(size = 10, sort = "billingId") Pageable pageable) {
        return ResponseEntity.ok(billingService.getAllBillings(pageable));
    }

    // GET /billing/{billingId} — Get billing record by ID
    @GetMapping("/{billingId}")
    public ResponseEntity<com.donorconnect.billingservice.dto.BillingResponseDTO> getBillingById(
            @PathVariable Integer billingId) {
        return ResponseEntity.ok(billingService.getBillingById(billingId));
    }

    // GET /billing/issue/{issueId} — Billing record for an issue
    @GetMapping("/issue/{issueId}")
    public ResponseEntity<com.donorconnect.billingservice.dto.BillingResponseDTO> getBillingByIssueId(
            @PathVariable Integer issueId) {
        return ResponseEntity.ok(billingService.getBillingByIssueId(issueId));
    }

    // GET /billing/export?from=&to= — Export billing data as JSON
    @GetMapping("/export")
    public ResponseEntity<List<com.donorconnect.billingservice.dto.BillingResponseDTO>> exportBillings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(billingService.exportBillings(from, to));
    }

    // PATCH /billing/{billingId}/status — Update billing status
    @PatchMapping("/{billingId}/status")
    public ResponseEntity<com.donorconnect.billingservice.dto.BillingResponseDTO> updateBillingStatus(
            @PathVariable Integer billingId,
            @Valid @RequestBody com.donorconnect.billingservice.dto.BillingStatusUpdateDTO statusUpdate) {
        return ResponseEntity.ok(billingService.updateBillingStatus(billingId, statusUpdate));
    }
}
