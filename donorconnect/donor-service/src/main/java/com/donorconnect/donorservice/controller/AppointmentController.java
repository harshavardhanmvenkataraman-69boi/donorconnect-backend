package com.donorconnect.donorservice.controller;

import com.donorconnect.donorservice.dto.request.AppointmentRequest;
import com.donorconnect.donorservice.dto.response.ApiResponse;
import com.donorconnect.donorservice.enums.AppointmentStatus;
import com.donorconnect.donorservice.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Donation appointment booking and management")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN','ROLE_DONOR')")
    @Operation(summary = "Book appointment")
    public ResponseEntity<ApiResponse<?>> book(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment booked", appointmentService.book(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "All appointments (paginated)")
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getById(appointmentId)));
    }

    @GetMapping("/donor/{donorId}")
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "Appointments for a donor")
    public ResponseEntity<ApiResponse<?>> getByDonor(@PathVariable Long donorId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getByDonor(donorId)));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "Today's appointments")
    public ResponseEntity<ApiResponse<?>> getToday() {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getToday()));
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN')")
    @Operation(summary = "Update appointment")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long appointmentId,
                                                  @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment updated", appointmentService.update(appointmentId, request)));
    }

    @PatchMapping("/{appointmentId}/check-in")
    @PreAuthorize("hasRole('ROLE_RECEPTION')")
    @Operation(summary = "Mark as checked-in")
    public ResponseEntity<ApiResponse<?>> checkIn(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Checked in", appointmentService.updateStatus(appointmentId, AppointmentStatus.CHECKED_IN)));
    }

    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_ADMIN','ROLE_DONOR')")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<ApiResponse<?>> cancel(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", appointmentService.updateStatus(appointmentId, AppointmentStatus.CANCELLED)));
    }

    @PatchMapping("/{appointmentId}/no-show")
    @PreAuthorize("hasRole('ROLE_RECEPTION')")
    @Operation(summary = "Mark as no-show")
    public ResponseEntity<ApiResponse<?>> noShow(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Marked no-show", appointmentService.updateStatus(appointmentId, AppointmentStatus.NO_SHOW)));
    }

    @PatchMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('ROLE_RECEPTION')")
    @Operation(summary = "Mark as completed")
    public ResponseEntity<ApiResponse<?>> complete(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success("Appointment completed", appointmentService.updateStatus(appointmentId, AppointmentStatus.COMPLETED)));
    }
}
