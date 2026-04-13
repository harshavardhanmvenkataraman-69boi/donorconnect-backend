package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.dto.request.ScreeningRequest;
import com.donorconnect.donorservice.dto.response.ApiResponse;
import com.donorconnect.donorservice.service.ScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/screenings")
@RequiredArgsConstructor
@Tag(name = "Screening", description = "Donor screening records")
public class ScreeningController {

    private final ScreeningService screeningService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Create screening record")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody ScreeningRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Screening created", screeningService.create(request)));
    }

    @GetMapping("/{screeningId}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Get screening by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long screeningId) {
        return ResponseEntity.ok(ApiResponse.success(screeningService.getById(screeningId)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "All screenings for a donor")
    public ResponseEntity<ApiResponse<?>> getByDonor(@PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.success(screeningService.getByDonor(donorId)));
    }

    @PutMapping("/{screeningId}")
    @PreAuthorize("hasAnyRole('RECEPTION','ADMIN')")
    @Operation(summary = "Update screening record")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long screeningId,
                                                 @RequestBody ScreeningRequest request) {
        if (Boolean.FALSE.equals(request.getClearedFlag()) && request.getDeferralRequest() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("deferralRequest is required when clearedFlag is false.")
            );
        }
        return ResponseEntity.ok(ApiResponse.success("Screening updated", screeningService.update(screeningId, request)));
    }
}

