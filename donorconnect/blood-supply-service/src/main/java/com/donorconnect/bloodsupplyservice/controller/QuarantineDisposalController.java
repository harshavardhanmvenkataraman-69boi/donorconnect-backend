package com.donorconnect.bloodsupplyservice.controller;

import com.donorconnect.bloodsupplyservice.dto.request.DisposalRequest;
import com.donorconnect.bloodsupplyservice.dto.request.QuarantineRequest;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import com.donorconnect.bloodsupplyservice.service.QuarantineDisposalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Quarantine & Disposal", description = "Component quarantine and disposal workflow")
public class QuarantineDisposalController {

    private final QuarantineDisposalService service;

    // --- QUARANTINE ---

    @PostMapping("/api/v1/quarantine")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Quarantine a component (also updates component.status -> QUARANTINE)")
    public ResponseEntity<ApiResponse<?>> quarantine(@RequestBody QuarantineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Component quarantined", service.quarantine(request)));
    }

    @GetMapping("/api/v1/quarantine")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "All quarantine actions (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllQuarantine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllQuarantine(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/quarantine/{qaId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Get quarantine action by ID")
    public ResponseEntity<ApiResponse<?>> getQuarantineById(@PathVariable Long qaId) {
        return ResponseEntity.ok(ApiResponse.success(service.getQuarantineById(qaId)));
    }

    @GetMapping("/api/v1/quarantine/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Quarantine history for component")
    public ResponseEntity<ApiResponse<?>> getByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByComponent(componentId)));
    }

    @GetMapping("/api/v1/quarantine/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Currently quarantined components")
    public ResponseEntity<ApiResponse<?>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(service.getActiveQuarantine()));
    }

    @PatchMapping("/api/v1/quarantine/{qaId}/release")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Release from quarantine (also updates component.status -> AVAILABLE)")
    public ResponseEntity<ApiResponse<?>> release(@PathVariable Long qaId) {
        return ResponseEntity.ok(ApiResponse.success("Released from quarantine", service.release(qaId)));
    }

    // --- DISPOSAL ---

    @PostMapping("/api/v1/disposal")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Record disposal (also updates component.status -> DISPOSED)")
    public ResponseEntity<ApiResponse<?>> createDisposal(@RequestBody DisposalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Disposal recorded", service.createDisposal(request)));
    }

    @GetMapping("/api/v1/disposal")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "All disposal records (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllDisposals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllDisposals(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/disposal/{disposalId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Get disposal record by ID")
    public ResponseEntity<ApiResponse<?>> getDisposalById(@PathVariable Long disposalId) {
        return ResponseEntity.ok(ApiResponse.success(service.getDisposalById(disposalId)));
    }

    @GetMapping("/api/v1/disposal/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Disposal records for a component")
    public ResponseEntity<ApiResponse<?>> getDisposalsByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(service.getDisposalsByComponent(componentId)));
    }
}
