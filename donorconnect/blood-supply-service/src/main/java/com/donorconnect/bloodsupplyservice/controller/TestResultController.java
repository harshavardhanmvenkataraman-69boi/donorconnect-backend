package com.donorconnect.bloodsupplyservice.controller;

import com.donorconnect.bloodsupplyservice.dto.request.TestResultRequest;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import com.donorconnect.bloodsupplyservice.service.TestResultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test-results")
@RequiredArgsConstructor
@Tag(name = "Test Results", description = "Infectious marker and blood group test results")
public class TestResultController {

    private final TestResultService testResultService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "Enter test result")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody TestResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Test result saved", testResultService.create(request)));
    }

    @GetMapping("/{testResultId}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "Get test result by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long testResultId) {
        return ResponseEntity.ok(ApiResponse.success(testResultService.getById(testResultId)));
    }

    @GetMapping("/donation/{donationId}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "All results for a donation")
    public ResponseEntity<ApiResponse<?>> getByDonation(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(testResultService.getByDonation(donationId)));
    }

    @PutMapping("/{testResultId}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "Update test result")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long testResultId,
                                                 @RequestBody TestResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Result updated", testResultService.update(testResultId, request)));
    }

    @GetMapping("/reactive")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "All reactive results")
    public ResponseEntity<ApiResponse<?>> getReactive() {
        return ResponseEntity.ok(ApiResponse.success(testResultService.getReactive()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "All pending results")
    public ResponseEntity<ApiResponse<?>> getPending() {
        return ResponseEntity.ok(ApiResponse.success(testResultService.getPending()));
    }
}
