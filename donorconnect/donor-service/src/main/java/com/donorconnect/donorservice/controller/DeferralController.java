package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.dto.request.DeferralRequest;
import com.donorconnect.donorservice.dto.response.ApiResponse;
import com.donorconnect.donorservice.service.DeferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deferrals")
@RequiredArgsConstructor
@Tag(name = "Deferrals", description = "Donor deferral management")
public class DeferralController {

    private final DeferralService deferralService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create deferral")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody DeferralRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deferral created", deferralService.create(request)));
    }

    @GetMapping("/{deferralId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "Get deferral by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long deferralId) {
        return ResponseEntity.ok(ApiResponse.success(deferralService.getById(deferralId)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "All deferrals for a donor")
    public ResponseEntity<ApiResponse<?>> getByDonor(@PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.success(deferralService.getByDonor(donorId)));
    }

    @PutMapping("/{deferralId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update deferral")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long deferralId,
                                                 @RequestBody DeferralRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deferral updated", deferralService.update(deferralId, request)));
    }

    /**
     * Lifts a TEMPORARY deferral manually before its endDate.
     * Also restores donor status to ACTIVE if no other active deferrals remain.
     * PERMANENT deferrals cannot be lifted.
     */
    @PatchMapping("/{deferralId}/lift")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lift a temporary deferral")
    public ResponseEntity<ApiResponse<?>> lift(@PathVariable Long deferralId) {
        return ResponseEntity.ok(ApiResponse.success("Deferral lifted", deferralService.lift(deferralId)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "All currently active deferrals")
    public ResponseEntity<ApiResponse<?>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(deferralService.getActive()));
    }

    /**
     * Admin visibility into deferrals that were expired by the nightly scheduler.
     * Useful for audits and reporting.
     */
    @GetMapping("/expired")
    @PreAuthorize("hasAnyAuthority('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "All expired temporary deferrals (processed by nightly scheduler)")
    public ResponseEntity<ApiResponse<?>> getExpired() {
        return ResponseEntity.ok(ApiResponse.success(deferralService.getExpiredDeferrals()));
    }
}
