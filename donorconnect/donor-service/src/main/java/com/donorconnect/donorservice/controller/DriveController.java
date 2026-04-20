package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.dto.request.DriveRequest;
import com.donorconnect.donorservice.dto.response.ApiResponse;
import com.donorconnect.donorservice.enums.DriveStatus;
import com.donorconnect.donorservice.service.DriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drives")
@RequiredArgsConstructor
@Tag(name = "Drives", description = "Blood donation drives")
public class DriveController {

    private final DriveService driveService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create donation drive")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody DriveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Drive created", driveService.create(request)));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_RECEPTION')")
    @Operation(summary = "All drives")
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(driveService.getAll()));
    }

    @GetMapping("/{driveId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_RECEPTION')")
    @Operation(summary = "Get drive by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long driveId) {
        return ResponseEntity.ok(ApiResponse.success(driveService.getById(driveId)));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_RECEPTION')")
    @Operation(summary = "Upcoming drives")
    public ResponseEntity<ApiResponse<?>> getUpcoming() {
        return ResponseEntity.ok(ApiResponse.success(driveService.getUpcoming()));
    }

    @PutMapping("/{driveId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update drive")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long driveId,
                                                  @RequestBody DriveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Drive updated", driveService.update(driveId, request)));
    }

    @PatchMapping("/{driveId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Change drive status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long driveId,
                                                        @RequestParam DriveStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", driveService.updateStatus(driveId, status)));
    }

    @GetMapping("/{driveId}/appointments")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_RECEPTION')")
    @Operation(summary = "All appointments for a drive")
    public ResponseEntity<ApiResponse<?>> getAppointments(@PathVariable Long driveId) {
        return ResponseEntity.ok(ApiResponse.success(driveService.getAppointmentsByDrive(driveId)));
    }

}
