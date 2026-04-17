package com.donorconnect.bloodsupplyservice.controller;

import com.donorconnect.bloodsupplyservice.dto.request.DonationRequest;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import com.donorconnect.bloodsupplyservice.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
@Tag(name = "Donations", description = "Donation collection workflow")
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_ADMIN')")
    @Operation(summary = "Start donation collection")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody DonationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Donation recorded", donationService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_ADMIN')")
    @Operation(summary = "All donations (paginated)")
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(donationService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{donationId}")
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_ADMIN','ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Get donation by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(donationService.getById(donationId)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_ADMIN')")
    @Operation(summary = "Donations by donor")
    public ResponseEntity<ApiResponse<?>> getByDonor(@PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.success(donationService.getByDonor(donorId)));
    }

    @PutMapping("/{donationId}")
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_ADMIN')")
    @Operation(summary = "Update donation")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long donationId,
                                                 @RequestBody DonationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Donation updated", donationService.update(donationId, request)));
    }

    @PatchMapping("/{donationId}/status")
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_ADMIN')")
    @Operation(summary = "Update collection status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long donationId,
                                                       @RequestParam CollectionStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", donationService.updateStatus(donationId, status)));
    }

    @GetMapping("/bag/{bagId}")
    @PreAuthorize("hasAnyRole('ROLE_PHLEBOTOMIST','ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "Find donation by bag ID")
    public ResponseEntity<ApiResponse<?>> getByBagId(@PathVariable String bagId) {
        return ResponseEntity.ok(ApiResponse.success(donationService.getByBagId(bagId)));
    }
}
