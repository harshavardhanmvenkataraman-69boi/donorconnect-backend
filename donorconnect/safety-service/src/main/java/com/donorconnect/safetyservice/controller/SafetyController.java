package com.donorconnect.safetyservice.controller;

import com.donorconnect.safetyservice.dto.request.LookbackRequest;
import com.donorconnect.safetyservice.dto.request.ReactionRequest;
import com.donorconnect.safetyservice.dto.response.ApiResponse;

import com.donorconnect.safetyservice.enums.ReactionStatus;
import com.donorconnect.safetyservice.enums.Severity;
import com.donorconnect.safetyservice.service.SafetyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reactions & Lookback", description = "Adverse reactions and donor traceability")
public class SafetyController {

    private final SafetyService reactionService;

    // --- REACTIONS ---
    @PostMapping("/api/v1/reactions")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Log adverse reaction")
    public ResponseEntity<ApiResponse<?>> createReaction(@RequestBody ReactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reaction logged", reactionService.create(request)));
    }

    @GetMapping("/api/v1/reactions")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "All reactions (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllReactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/reactions/{reactionId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get reaction by ID")
    public ResponseEntity<ApiResponse<?>> getReactionById(@PathVariable Long reactionId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getById(reactionId)));
    }

    @GetMapping("/api/v1/reactions/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Reactions for a patient")
    public ResponseEntity<ApiResponse<?>> getReactionsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getReactionsByPatient(patientId)));
    }

    @GetMapping("/api/v1/reactions/severity/{severity}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Filter reactions by severity")
    public ResponseEntity<ApiResponse<?>> getReactionsBySeverity(@PathVariable Severity severity) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getBySeverity(severity)));
    }

    @PutMapping("/api/v1/reactions/{reactionId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Update reaction record")
    public ResponseEntity<ApiResponse<?>> updateReaction(@PathVariable Long reactionId,
                                                         @RequestBody ReactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reaction updated", reactionService.update(reactionId, request)));
    }

    @PatchMapping("/api/v1/reactions/{reactionId}/status")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Update investigation status")
    public ResponseEntity<ApiResponse<?>> updateReactionStatus(@PathVariable Long reactionId,
                                                               @RequestParam ReactionStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", reactionService.updateStatus(reactionId, status)));
    }

    // --- LOOKBACK ---
    @PostMapping("/api/v1/lookback")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create lookback trace")
    public ResponseEntity<ApiResponse<?>> createLookbackTrace(@RequestBody LookbackRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trace created", reactionService.createTrace(request)));
    }

    @GetMapping("/api/v1/lookback/donation/{donationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Trace all components from a donation")
    public ResponseEntity<ApiResponse<?>> getLookbackByDonation(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getByDonation(donationId)));
    }

    @GetMapping("/api/v1/lookback/patient/{patientId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "All donations received by a patient")
    public ResponseEntity<ApiResponse<?>> getLookbackByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getLookbackByPatient(patientId)));
    }

    @GetMapping("/api/v1/lookback/component/{componentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Trace a specific component")
    public ResponseEntity<ApiResponse<?>> getLookbackByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getByComponent(componentId)));
    }
}