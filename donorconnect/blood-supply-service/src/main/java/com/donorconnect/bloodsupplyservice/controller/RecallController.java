package com.donorconnect.bloodsupplyservice.controller;

import com.donorconnect.bloodsupplyservice.dto.request.DisposalRequest;
import com.donorconnect.bloodsupplyservice.dto.request.QuarantineRequest;
import com.donorconnect.bloodsupplyservice.dto.request.RecallRequest;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import com.donorconnect.bloodsupplyservice.service.RecallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Recalls, Quarantine & Disposal", description = "Blood unit recall, quarantine and disposal workflow")
public class RecallController {

    private final RecallService recallService;

    // --- RECALLS ---

    @PostMapping("/api/v1/recalls")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create recall notice")
    public ResponseEntity<ApiResponse<?>> createRecall(@RequestBody RecallRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Recall created", recallService.createRecall(request)));
    }

    @GetMapping("/api/v1/recalls")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "All recalls (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllRecalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getAllRecalls(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/recalls/{recallId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get recall by ID")
    public ResponseEntity<ApiResponse<?>> getRecallById(@PathVariable Long recallId) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getRecallById(recallId)));
    }

    @GetMapping("/api/v1/recalls/donation/{donationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Recalls for a donation")
    public ResponseEntity<ApiResponse<?>> getRecallsByDonation(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getRecallsByDonation(donationId)));
    }

    @GetMapping("/api/v1/recalls/open")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "All open recalls")
    public ResponseEntity<ApiResponse<?>> getOpenRecalls() {
        return ResponseEntity.ok(ApiResponse.success(recallService.getOpenRecalls()));
    }

    @PatchMapping("/api/v1/recalls/{recallId}/close")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Close a recall")
    public ResponseEntity<ApiResponse<?>> closeRecall(@PathVariable Long recallId) {
        return ResponseEntity.ok(ApiResponse.success("Recall closed", recallService.closeRecall(recallId)));
    }

    // --- QUARANTINE ---

    @PostMapping("/api/v1/quarantine")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Quarantine a component")
    public ResponseEntity<ApiResponse<?>> quarantine(@RequestBody QuarantineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Component quarantined", recallService.quarantine(request)));
    }

    @GetMapping("/api/v1/quarantine")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "All quarantine actions")
    public ResponseEntity<ApiResponse<?>> getAllQuarantine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getAllQuarantine(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/quarantine/{qaId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get quarantine action by ID")
    public ResponseEntity<ApiResponse<?>> getQuarantineById(@PathVariable Long qaId) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getQuarantineById(qaId)));
    }

    @GetMapping("/api/v1/quarantine/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Quarantine history for component")
    public ResponseEntity<ApiResponse<?>> getByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getByComponent(componentId)));
    }

    @GetMapping("/api/v1/quarantine/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Currently quarantined components")
    public ResponseEntity<ApiResponse<?>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(recallService.getActiveQuarantine()));
    }

    @PatchMapping("/api/v1/quarantine/{qaId}/release")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Release from quarantine")
    public ResponseEntity<ApiResponse<?>> release(@PathVariable Long qaId) {
        return ResponseEntity.ok(ApiResponse.success("Released from quarantine", recallService.release(qaId)));
    }

    // --- DISPOSAL ---

    @PostMapping("/api/v1/disposal")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Record disposal")
    public ResponseEntity<ApiResponse<?>> createDisposal(@RequestBody DisposalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Disposal recorded", recallService.createDisposal(request)));
    }

    @GetMapping("/api/v1/disposal")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "All disposal records (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllDisposals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getAllDisposals(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/disposal/{disposalId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get disposal record by ID")
    public ResponseEntity<ApiResponse<?>> getDisposalById(@PathVariable Long disposalId) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getDisposalById(disposalId)));
    }

    @GetMapping("/api/v1/disposal/component/{componentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Disposal records for a component")
    public ResponseEntity<ApiResponse<?>> getDisposalsByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(recallService.getDisposalsByComponent(componentId)));
    }
}
