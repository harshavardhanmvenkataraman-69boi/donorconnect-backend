package com.donorconnect.reportingservice.controller;

import com.donorconnect.reportingservice.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports/download")
@RequiredArgsConstructor
@Tag(name = "Report Downloads", description = "Download reports as CSV or Excel")
public class ReportDownloadController {

    private final ReportExportService exportService;

    private static final String TODAY = LocalDate.now().toString();

    // ── CSV downloads ─────────────────────────────────────────────────────────

    @GetMapping("/inventory-snapshot/csv")
    @Operation(summary = "Download inventory snapshot as CSV")
    public ResponseEntity<byte[]> inventorySnapshotCsv() {
        return csvResponse(exportService.exportInventorySnapshotCsv(),
                "inventory-snapshot-" + TODAY + ".csv");
    }

    @GetMapping("/donor-activity/csv")
    @Operation(summary = "Download donor activity report as CSV")
    public ResponseEntity<byte[]> donorActivityCsv() {
        return csvResponse(exportService.exportDonorActivityCsv(),
                "donor-activity-" + TODAY + ".csv");
    }

    @GetMapping("/component-wastage/csv")
    @Operation(summary = "Download component wastage report as CSV")
    public ResponseEntity<byte[]> componentWastageCsv() {
        return csvResponse(exportService.exportComponentWastageCsv(),
                "component-wastage-" + TODAY + ".csv");
    }

    @GetMapping("/utilization/csv")
    @Operation(summary = "Download utilization report as CSV")
    public ResponseEntity<byte[]> utilizationCsv() {
        return csvResponse(exportService.exportUtilizationCsv(),
                "utilization-" + TODAY + ".csv");
    }

    @GetMapping("/deferrals/csv")
    @Operation(summary = "Download deferral trends report as CSV")
    public ResponseEntity<byte[]> deferralsCsv() {
        return csvResponse(exportService.exportDeferralsCsv(),
                "deferrals-" + TODAY + ".csv");
    }

    @GetMapping("/reactive-count/csv")
    @Operation(summary = "Download reactive test count report as CSV")
    public ResponseEntity<byte[]> reactiveCountCsv() {
        return csvResponse(exportService.exportReactiveCountCsv(),
                "reactive-count-" + TODAY + ".csv");
    }

    @GetMapping("/expiry-risk/csv")
    @Operation(summary = "Download expiry risk report as CSV")
    public ResponseEntity<byte[]> expiryRiskCsv() {
        return csvResponse(exportService.exportExpiryRiskCsv(),
                "expiry-risk-" + TODAY + ".csv");
    }

    @GetMapping("/full-report/csv")
    @Operation(summary = "Download full analytics report as CSV (all sections)")
    public ResponseEntity<byte[]> fullReportCsv() {
        return csvResponse(exportService.exportFullReportCsv(),
                "donorconnect-full-report-" + TODAY + ".csv");
    }

    // ── Excel downloads ───────────────────────────────────────────────────────

    @GetMapping("/full-report/excel")
    @Operation(summary = "Download full analytics report as Excel (all sheets)")
    public ResponseEntity<byte[]> fullReportExcel() throws IOException {
        byte[] data = exportService.exportFullReportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"donorconnect-full-report-" + TODAY + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(data);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> csvResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(data);
    }
}
