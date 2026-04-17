package com.donorconnect.transfusionservice.controller;

import com.donorconnect.transfusionservice.dto.request.CrossmatchRequestDto;
import com.donorconnect.transfusionservice.dto.request.CrossmatchResultRequest;
import com.donorconnect.transfusionservice.dto.request.IssueRequestDto;
import com.donorconnect.transfusionservice.dto.response.ApiResponse;
import com.donorconnect.transfusionservice.entity.*;
import com.donorconnect.transfusionservice.enums.CrossmatchStatus;
import com.donorconnect.transfusionservice.service.TransfusionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/transfusion")
@RequiredArgsConstructor
@Tag(name = "Transfusion", description = "Crossmatch, reservation and blood issue to patients")
public class TransfusionController {
    private final TransfusionService transfusionService;

    // --- CROSSMATCH REQUESTS ---

    @PostMapping("/api/v1/crossmatch/requests")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Create crossmatch request")
    public ResponseEntity<ApiResponse<?>> createRequest(@RequestBody CrossmatchRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Crossmatch request created", transfusionService.createRequest(request)));
    }

    @GetMapping("/api/v1/crossmatch/requests")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "All crossmatch requests (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getAllRequests(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/crossmatch/requests/{requestId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get crossmatch request by ID")
    public ResponseEntity<ApiResponse<?>> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getRequestById(requestId)));
    }

    @GetMapping("/api/v1/crossmatch/requests/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Crossmatch requests for a patient")
    public ResponseEntity<ApiResponse<?>> getRequestsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getRequestsByPatient(patientId)));
    }

    @GetMapping("/api/v1/crossmatch/requests/pending")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "All pending crossmatch requests")
    public ResponseEntity<ApiResponse<?>> getPendingRequests() {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getPendingRequests()));
    }

    @PatchMapping("/api/v1/crossmatch/requests/{requestId}/status")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Update crossmatch request status")
    public ResponseEntity<ApiResponse<?>> updateRequestStatus(@PathVariable Long requestId,
                                                              @RequestParam CrossmatchStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", transfusionService.updateRequestStatus(requestId, status)));
    }

    // --- CROSSMATCH RESULTS ---

    @PostMapping("/api/v1/crossmatch/results")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Record crossmatch result")
    public ResponseEntity<ApiResponse<?>> createResult(@RequestBody CrossmatchResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Result recorded", transfusionService.createResult(request)));
    }

    @GetMapping("/api/v1/crossmatch/results/{crossmatchId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get crossmatch result by ID")
    public ResponseEntity<ApiResponse<?>> getResultById(@PathVariable Long crossmatchId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getResultById(crossmatchId)));
    }

    @GetMapping("/api/v1/crossmatch/results/request/{requestId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Results for a crossmatch request")
    public ResponseEntity<ApiResponse<?>> getResultsByRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getResultsByRequest(requestId)));
    }

    // --- ISSUE ---

    @PostMapping("/api/v1/issue")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Issue blood to patient")
    public ResponseEntity<ApiResponse<?>> issue(@RequestBody IssueRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Blood issued to patient", transfusionService.issue(request)));
    }

    @GetMapping("/api/v1/issue")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "All issue records (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllIssues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getAllIssues(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/issue/{issueId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Get issue record by ID")
    public ResponseEntity<ApiResponse<?>> getIssueById(@PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getIssueById(issueId)));
    }

    @GetMapping("/api/v1/issue/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Issues for a patient")
    public ResponseEntity<ApiResponse<?>> getIssuesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getIssuesByPatient(patientId)));
    }

    @GetMapping("/api/v1/issue/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Issues for a component")
    public ResponseEntity<ApiResponse<?>> getIssuesByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(transfusionService.getIssuesByComponent(componentId)));
    }

    @PatchMapping("/api/v1/issue/{issueId}/return")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER','ROLE_ADMIN')")
    @Operation(summary = "Mark unit as returned")
    public ResponseEntity<ApiResponse<?>> returnUnit(@PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.success("Unit returned", transfusionService.returnUnit(issueId)));
    }
}
