package com.donorconnect.safetyservice.controller;

import com.donorconnect.safetyservice.dto.request.LookbackRequest;
import com.donorconnect.safetyservice.dto.request.ReactionRequest;
import com.donorconnect.safetyservice.dto.response.ApiResponse;
import com.donorconnect.safetyservice.enums.LookbackStatus;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/safety")
@RequiredArgsConstructor
@Tag(name = "Reactions & Lookback", description = "Adverse reactions and donor traceability")
public class SafetyController {

    private final SafetyService reactionService;

    // --- REACTIONS ---

    @PostMapping("/reactions")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Log adverse reaction — status starts PENDING")
    public ResponseEntity<ApiResponse<?>> createReaction(@RequestBody ReactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reaction logged", reactionService.create(request)));
    }

    @GetMapping("/reactions")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "All reactions (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllReactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/reactions/{reactionId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get reaction by ID")
    public ResponseEntity<ApiResponse<?>> getReactionById(@PathVariable Long reactionId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getById(reactionId)));
    }

    @GetMapping("/reactions/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Reactions for a patient")
    public ResponseEntity<ApiResponse<?>> getReactionsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getReactionsByPatient(patientId)));
    }

    @GetMapping("/reactions/severity/{severity}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Filter reactions by severity")
    public ResponseEntity<ApiResponse<?>> getReactionsBySeverity(@PathVariable Severity severity) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getBySeverity(severity)));
    }

    @PutMapping("/reactions/{reactionId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Update reaction record")
    public ResponseEntity<ApiResponse<?>> updateReaction(@PathVariable Long reactionId,
                                                         @RequestBody ReactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reaction updated", reactionService.update(reactionId, request)));
    }

    // Manual status update — Admin closes investigation
    // PENDING → INVESTIGATING (auto on lookback) → CLOSED (manual by admin)
    @PatchMapping("/reactions/{reactionId}/status")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Update reaction status — PENDING / INVESTIGATING / CLOSED")
    public ResponseEntity<ApiResponse<?>> updateReactionStatus(@PathVariable Long reactionId,
                                                               @RequestParam ReactionStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", reactionService.updateStatus(reactionId, status)));
    }

    // --- LOOKBACK ---

    // Initiate → saves trace with TRACED status + auto-updates reaction to INVESTIGATING
    @PostMapping("/lookback")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Initiate lookback trace — LookbackStatus=TRACED, Reaction→INVESTIGATING")
    public ResponseEntity<ApiResponse<?>> createLookbackTrace(@RequestBody LookbackRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trace created", reactionService.createTrace(request)));
    }

    @GetMapping("/lookback/donation/{donationId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Trace all components from a donation")
    public ResponseEntity<ApiResponse<?>> getLookbackByDonation(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getByDonation(donationId)));
    }

    @GetMapping("/lookback/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "All donations received by a patient")
    public ResponseEntity<ApiResponse<?>> getLookbackByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getLookbackByPatient(patientId)));
    }

    @GetMapping("/lookback/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Trace a specific component")
    public ResponseEntity<ApiResponse<?>> getLookbackByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getByComponent(componentId)));
    }

    @GetMapping("/lookback/exists/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Check if lookback exists for a patient")
    public ResponseEntity<ApiResponse<?>> lookbackExistsForPatient(@PathVariable Long patientId) {
        var traces = reactionService.getTracesByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "exists", !traces.isEmpty(),
                "traces", traces
        )));
    }

    // Admin closes lookback trace — TRACED → CLOSED
    @PatchMapping("/lookback/{traceId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update lookback trace status — Admin only (OPEN / TRACED / CLOSED)")
    public ResponseEntity<ApiResponse<?>> updateLookbackStatus(@PathVariable Long traceId,
                                                               @RequestParam LookbackStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Lookback status updated",
                reactionService.updateLookbackStatus(traceId, status)));
    }

    // Full investigation — ADMIN ONLY
    @GetMapping("/lookback-details/{donationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Full lookback investigation — donor + all components (Admin only)")
    public ResponseEntity<ApiResponse<?>> getLookbackDetails(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getLookbackDetails(donationId)));
    }

    // --- LOOKUP HELPERS ---

    @GetMapping("/issue-component/{issueId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get component ID for a given issue record")
    public ResponseEntity<ApiResponse<?>> getComponentIdByIssue(@PathVariable Long issueId) {
        Long componentId = reactionService.getComponentIdByIssue(issueId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("componentId", componentId)));
    }

    @GetMapping("/component-donation/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get donation ID for a given component")
    public ResponseEntity<ApiResponse<?>> getDonationIdByComponent(@PathVariable Long componentId) {
        Long donationId = reactionService.getDonationIdByComponent(componentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("donationId", donationId)));
    }
}