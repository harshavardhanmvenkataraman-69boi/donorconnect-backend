package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.dto.request.DonorRequest;
import com.donorconnect.donorservice.dto.response.ApiResponse;
import com.donorconnect.donorservice.entity.*;
import com.donorconnect.donorservice.enums.DonorStatus;
import com.donorconnect.donorservice.enums.DonorType;
import com.donorconnect.donorservice.service.DonorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
@Tag(name = "Donors", description = "Donor registration and management")
public class DonorController {

    private final DonorService donorService;
//    private final DonationService donationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ADMIN')")
    @Operation(summary = "Register new donor")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody DonorRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Donor registered", donorService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ADMIN')")
    @Operation(summary = "Get all donors (paginated)")
    public ResponseEntity<ApiResponse<?>> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "donorId") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getAll(pageable)));
    }

    @GetMapping("/{donorId}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN','PHLEBOTOMIST')")
    @Operation(summary = "Get donor by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getById(donorId)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Search donors by name or blood group")
    public ResponseEntity<ApiResponse<?>> search(
            @RequestParam(required = false) String name,
            @RequestParam(name = "blood-group", required = false) String bloodGroup) {
        return ResponseEntity.ok(ApiResponse.success(donorService.search(name, bloodGroup)));
    }

    @PutMapping("/{donorId}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Update donor info")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long donorId,
                                                 @RequestBody DonorRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Donor updated", donorService.update(donorId, request)));
    }

    @PutMapping("/{donorId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change donor status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long donorId,
                                                       @RequestParam DonorStatus status) {
        if (status == DonorStatus.ACTIVE || status == DonorStatus.DEFERRED) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("ACTIVE and DEFERRED statuses are managed automatically by the system.")
            );
        }
        return ResponseEntity.ok(ApiResponse.success("Status updated", donorService.updateStatus(donorId, status)));
    }

//    @GetMapping("/{donorId}/donation-history")
//    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN')")
//    @Operation(summary = "Donor's donation history")
//    public ResponseEntity<ApiResponse<?>> getDonationHistory(@PathVariable Long donorId) {
//        return ResponseEntity.ok(ApiResponse.success(donationService.getByDonor(donorId)));
//    }

    @GetMapping("/blood-group/{bloodGroup}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Filter donors by blood group")
    public ResponseEntity<ApiResponse<?>> getByBloodGroup(@PathVariable String bloodGroup) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getByBloodGroup(bloodGroup)));
    }

    @GetMapping("/type/{donorType}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Filter donors by type")
    public ResponseEntity<ApiResponse<?>> getByType(@PathVariable DonorType donorType) {
        return ResponseEntity.ok(ApiResponse.success(donorService.getByType(donorType)));
    }
}
