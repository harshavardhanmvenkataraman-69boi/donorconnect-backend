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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/safety")   // ← FIX: was missing, methods had full paths hardcoded
@RequiredArgsConstructor
@Tag(name = "Reactions & Lookback", description = "Adverse reactions and donor traceability")
public class SafetyController {

    private final SafetyService reactionService;

    // --- REACTIONS ---

    @PostMapping("/reactions")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Log adverse reaction")
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

    @PatchMapping("/reactions/{reactionId}/status")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Update investigation status")
    public ResponseEntity<ApiResponse<?>> updateReactionStatus(@PathVariable Long reactionId,
                                                               @RequestParam ReactionStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", reactionService.updateStatus(reactionId, status)));
    }

    // --- LOOKBACK ---

    @PostMapping("/lookback")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create lookback trace")
    public ResponseEntity<ApiResponse<?>> createLookbackTrace(@RequestBody LookbackRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Trace created", reactionService.createTrace(request)));
    }

    @GetMapping("/lookback/donation/{donationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Trace all components from a donation")
    public ResponseEntity<ApiResponse<?>> getLookbackByDonation(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getByDonation(donationId)));
    }

    @GetMapping("/lookback/patient/{patientId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "All donations received by a patient")
    public ResponseEntity<ApiResponse<?>> getLookbackByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getLookbackByPatient(patientId)));
    }

    @GetMapping("/lookback/component/{componentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Trace a specific component")
    public ResponseEntity<ApiResponse<?>> getLookbackByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(reactionService.getByComponent(componentId)));
    }

    // NEW — get componentId from issueId via BloodIssueClient Feign
    // Called by frontend to build the lookback chain from a reaction row
    // GET /api/safety/issue-component/{issueId} → { componentId: 17 }
    @GetMapping("/issue-component/{issueId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get component ID for a given issue record")
    public ResponseEntity<ApiResponse<?>> getComponentIdByIssue(@PathVariable Long issueId) {
        Long componentId = reactionService.getComponentIdByIssue(issueId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("componentId", componentId)));
    }

    // NEW — get donationId from componentId via BloodComponentClient Feign
    // GET /api/safety/component-donation/{componentId} → { donationId: 3 }
    @GetMapping("/component-donation/{componentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get donation ID for a given component")
    public ResponseEntity<ApiResponse<?>> getDonationIdByComponent(@PathVariable Long componentId) {
        Long donationId = reactionService.getDonationIdByComponent(componentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("donationId", donationId)));
    }
}