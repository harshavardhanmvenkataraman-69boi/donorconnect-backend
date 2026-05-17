package com.donorconnect.billingservice.controller;

import com.donorconnect.billingservice.dto.BillingRequestDTO;
import com.donorconnect.billingservice.dto.BillingResponseDTO;
import com.donorconnect.billingservice.dto.BillingStatusUpdateDTO;
import com.donorconnect.billingservice.dto.PagedResponseDTO;
import com.donorconnect.billingservice.enums.BillingStatus;
import com.donorconnect.billingservice.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Billing REST API.
 *
 * Mounted at /api/v1/billing — the API gateway rewrites /api/billing/(.*)
 * onto this path before forwarding the request.
 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    // ── Commands ────────────────────────────────────────────────────────────────

    /** POST /api/v1/billing — create a billing record. */
    @PostMapping
    public ResponseEntity<BillingResponseDTO> createBilling(@Valid @RequestBody BillingRequestDTO request) {
        BillingResponseDTO created = billingService.createBilling(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PATCH /api/v1/billing/{billingId}/status — update status. */
    @PatchMapping("/{billingId}/status")
    public ResponseEntity<BillingResponseDTO> updateBillingStatus(
            @PathVariable Integer billingId,
            @Valid @RequestBody BillingStatusUpdateDTO statusUpdate) {
        return ResponseEntity.ok(billingService.updateBillingStatus(billingId, statusUpdate));
    }

    /** POST /api/v1/billing/export/mark — mark all PENDING records in range as EXPORTED. */
    @PostMapping("/export/mark")
    public ResponseEntity<List<BillingResponseDTO>> markExported(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(billingService.markExported(from, to));
    }

    // ── Queries ─────────────────────────────────────────────────────────────────

    /** GET /api/v1/billing — paginated list, optional status filter. */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<BillingResponseDTO>> getAllBillings(
            @RequestParam(required = false) BillingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "billingId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = "ASC".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        Page<BillingResponseDTO> result = billingService.getAllBillings(status, pageable);
        return ResponseEntity.ok(PagedResponseDTO.from(result));
    }

    /** GET /api/v1/billing/stats — counts per status, plus TOTAL. */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(billingService.getStatusCounts());
    }

    /** GET /api/v1/billing/{billingId} — single record. */
    @GetMapping("/{billingId}")
    public ResponseEntity<BillingResponseDTO> getBillingById(@PathVariable Integer billingId) {
        return ResponseEntity.ok(billingService.getBillingById(billingId));
    }

    /** GET /api/v1/billing/issue/{issueId} — record for an issue. */
    @GetMapping("/issue/{issueId}")
    public ResponseEntity<BillingResponseDTO> getBillingByIssueId(@PathVariable Integer issueId) {
        return ResponseEntity.ok(billingService.getBillingByIssueId(issueId));
    }

    /** GET /api/v1/billing/export — read-only export (JSON), filtered by date and optionally status. */
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BillingResponseDTO>> exportBillingsJson(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BillingStatus status) {
        return ResponseEntity.ok(billingService.exportBillings(from, to, status));
    }

    /** GET /api/v1/billing/export.csv — read-only CSV export. */
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportBillingsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BillingStatus status) {

        List<BillingResponseDTO> rows = billingService.exportBillings(from, to, status);

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.println("billingId,issueId,chargeAmount,chargeType,billingDate,status,createdAt,updatedAt");
            for (BillingResponseDTO r : rows) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                        r.getBillingId(),
                        r.getIssueId(),
                        r.getChargeAmount(),
                        r.getChargeType(),
                        r.getBillingDate(),
                        r.getStatus(),
                        formatTs(r.getCreatedAt()),
                        formatTs(r.getUpdatedAt())
                );
            }
        }

        String filename = String.format("billing-export-%s-to-%s.csv", from, to);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        return new ResponseEntity<>(sw.toString(), headers, HttpStatus.OK);
    }

    private static String formatTs(LocalDateTime ts) {
        return ts == null ? "" : ts.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
