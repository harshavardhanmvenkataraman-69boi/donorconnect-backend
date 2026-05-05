package com.donorconnect.reportingservice.controller;

import com.donorconnect.reportingservice.dto.ApiResponse;
import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.ReportScope;
import com.donorconnect.reportingservice.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics", description = "KPIs, analytics and regulatory reports")
public class ReportingController {

    private final ReportingService service;

    @GetMapping("/inventory-snapshot")
    @Operation(summary = "Current stock by blood group x component")
    public ResponseEntity<ApiResponse<?>> inventorySnapshot() {
        return ResponseEntity.ok(ApiResponse.success(service.getInventorySnapshot()));
    }

    @GetMapping("/expiry-risk")
    @Operation(summary = "Components at expiry risk (next 7 days)")
    public ResponseEntity<ApiResponse<?>> expiryRisk() {
        return ResponseEntity.ok(ApiResponse.success(service.getExpiryRisk()));
    }

    @GetMapping("/donor-activity")
    @Operation(summary = "Active / lapsed donor counts")
    public ResponseEntity<ApiResponse<?>> donorActivity() {
        return ResponseEntity.ok(ApiResponse.success(service.getDonorActivity()));
    }

    @GetMapping("/donation-frequency")
    @Operation(summary = "Donations per period")
    public ResponseEntity<ApiResponse<?>> donationFrequency() {
        return ResponseEntity.ok(ApiResponse.success(service.getDonationFrequency()));
    }

    @GetMapping("/component-wastage")
    @Operation(summary = "Expired / disposed component stats")
    public ResponseEntity<ApiResponse<?>> componentWastage() {
        return ResponseEntity.ok(ApiResponse.success(service.getComponentWastage()));
    }

    @GetMapping("/reactive-count")
    @Operation(summary = "Reactive test result counts by type")
    public ResponseEntity<ApiResponse<?>> reactiveCount() {
        return ResponseEntity.ok(ApiResponse.success(service.getReactiveCount()));
    }

    @GetMapping("/deferrals")
    @Operation(summary = "Deferral trends")
    public ResponseEntity<ApiResponse<?>> deferrals() {
        return ResponseEntity.ok(ApiResponse.success(service.getDeferralTrends()));
    }

    @GetMapping("/tat")
    @Operation(summary = "Turnaround time: collection to release")
    public ResponseEntity<ApiResponse<?>> tat() {
        return ResponseEntity.ok(ApiResponse.success(service.getTAT()));
    }

    @GetMapping("/utilization")
    @Operation(summary = "Blood utilization report")
    public ResponseEntity<ApiResponse<?>> utilization() {
        return ResponseEntity.ok(ApiResponse.success(service.getUtilization()));
    }

    @PostMapping("/generate")
    @Operation(summary = "Manually trigger report generation")
    public ResponseEntity<ApiResponse<?>> generate(
            @RequestParam(defaultValue = "SITE") ReportScope scope) {
        return ResponseEntity.ok(ApiResponse.success("Report generated", service.generateReport(scope)));
    }

    @GetMapping("/{packId}")
    @Operation(summary = "Get a saved report pack")
    public ResponseEntity<ApiResponse<?>> getPackById(@PathVariable Long packId) {
        return ResponseEntity.ok(ApiResponse.success(service.getPackById(packId)));
    }

    @GetMapping
    @Operation(summary = "All generated report packs (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllPacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPacks(PageRequest.of(page, size))));
    }

    // ── Legacy endpoints (kept for backward compatibility) ────────────────────

    @GetMapping("/scope/{scope}")
    @Operation(summary = "Get report packs by scope")
    public ResponseEntity<ApiResponse<List<LabReportPack>>> byScope(@PathVariable ReportScope scope) {
        return ResponseEntity.ok(ApiResponse.success(service.getByScope(scope)));
    }
}
