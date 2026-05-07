package com.donorconnect.transfusionservice.service;

import com.donorconnect.transfusionservice.dto.request.CrossmatchRequestDto;
import com.donorconnect.transfusionservice.dto.request.CrossmatchResultRequest;
import com.donorconnect.transfusionservice.dto.request.InventoryStatusUpdateRequest;
import com.donorconnect.transfusionservice.dto.request.IssueRequestDto;
import com.donorconnect.transfusionservice.entity.*;
import com.donorconnect.transfusionservice.enums.*;
import com.donorconnect.transfusionservice.exception.ComponentAlreadyIssuedException;
import com.donorconnect.transfusionservice.exception.ResourceNotFoundException;
import com.donorconnect.transfusionservice.feign.InventoryFeignClient;
import com.donorconnect.transfusionservice.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service 
@RequiredArgsConstructor 
@Slf4j
public class TransfusionService {
    private final CrossmatchRequestRepository crossmatchRequestRepository;
    private final CrossmatchResultRepository crossmatchResultRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final InventoryFeignClient inventoryFeignClient;
    private final BloodCompatibilityChecker compatibilityChecker;

    // --- CROSSMATCH REQUESTS ---
    public CrossmatchRequest createRequest(CrossmatchRequestDto req) {

        // Step 1: Check inventory availability across all compatible blood groups
        int totalAvailable = 0;
        List<Long> componentIds = new ArrayList<>();
        String componentType = req.getComponentType() != null
                ? req.getComponentType() : "PRBC";

        if (req.getBloodGroup() != null && req.getRhFactor() != null
                && req.getRequiredUnits() != null) {

            List<String[]> compatibleGroups = compatibilityChecker
                    .getCompatibleGroups(req.getBloodGroup(), req.getRhFactor(), componentType);

            for (String[] group : compatibleGroups) {
                try {
                    Map<String, Object> response = inventoryFeignClient
                            .getAvailableUnits(group[0], group[1], componentType);
                    // Extract list from "data" field of ApiResponse
                    Object data = response.get("data");
                    if (data instanceof List<?> list) {
                        totalAvailable += list.size();

                        // ADD — extract componentIds from each item
                        for (Object item : list) {
                            if (item instanceof Map<?, ?> itemMap) {
                                Object compId = itemMap.get("componentId");
                                if (compId != null) {
                                    componentIds.add(Long.valueOf(compId.toString()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch inventory for group={} rh={}: {}",
                            group[0], group[1], e.getMessage());
                }
            }
        }

        // Step 2: Determine status based on availability
        CrossmatchStatus initialStatus;
        String notes;

        if (req.getRequiredUnits() == null || totalAvailable == 0) {
            initialStatus = CrossmatchStatus.INSUFFICIENT_STOCK;
            notes = "No compatible units available. Required: " + req.getRequiredUnits();
        } else if (totalAvailable < req.getRequiredUnits()) {
            initialStatus = CrossmatchStatus.PARTIALLY_AVAILABLE;
            notes = "Only " + totalAvailable + " compatible units available."
                    + " Required: " + req.getRequiredUnits();
        } else {
            initialStatus = CrossmatchStatus.PENDING;
            notes = totalAvailable + " compatible units available."
                    + " Required: " + req.getRequiredUnits();
        }

        // Convert componentIds list to comma separated string
        String componentIdsStr = componentIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        CrossmatchRequest r = CrossmatchRequest.builder()
                .patientId(req.getPatientId())
                .orderBy(req.getOrderBy())
                .bloodGroup(req.getBloodGroup())
                .rhFactor(req.getRhFactor())
                .requiredUnits(req.getRequiredUnits())
                .priority(req.getPriority() != null ? req.getPriority() : CrossmatchPriority.ROUTINE)
                .requestDate(LocalDate.now())
                .status(initialStatus)
                .notes(notes)
                .availableComponentIds(componentIdsStr)
                .build();
        return crossmatchRequestRepository.save(r);
    }

    public Page<CrossmatchRequest> getAllRequests(Pageable pageable) {
        return crossmatchRequestRepository.findAll(pageable);
    }

    public CrossmatchRequest getRequestById(Long id) {
        return crossmatchRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrossmatchRequest", id));
    }

    public List<CrossmatchRequest> getRequestsByPatient(Long patientId) {
        return crossmatchRequestRepository.findByPatientId(patientId);
    }

    public List<CrossmatchRequest> getPendingRequests() {
        return crossmatchRequestRepository.findByStatus(CrossmatchStatus.PENDING);
    }

    public CrossmatchRequest updateRequestStatus(Long id, CrossmatchStatus status) {
        CrossmatchRequest r = getRequestById(id);
        r.setStatus(status);
        return crossmatchRequestRepository.save(r);
    }

    // --- CROSSMATCH RESULTS ---
    public CrossmatchResult createResult(CrossmatchResultRequest req) {

        // Safety check using inventory-service
        Map<String, Object> inventoryResponse =
                inventoryFeignClient.getInventoryByComponentId(req.getComponentId());

        // Status is inside "data" because response is wrapped in ApiResponse
        String inventoryStatus = extractStatusFromApiResponse(inventoryResponse);

        if (!"AVAILABLE".equalsIgnoreCase(inventoryStatus)) {
            throw new IllegalStateException(
                    "Component not available for crossmatch. Status: " + inventoryStatus);
        }

        CrossmatchResult r = CrossmatchResult.builder()
                .requestId(req.getRequestId())
                .componentId(req.getComponentId())
                .compatibility(req.getCompatibility())
                .testedBy(req.getTestedBy())
                .testedDate(LocalDate.now())
                .status(req.getCompatibility() == Compatibility.COMPATIBLE
                        ? CrossmatchStatus.MATCHED : CrossmatchStatus.REJECTED)
                .build();
        CrossmatchResult saved = crossmatchResultRepository.save(r);

        if (req.getCompatibility() == Compatibility.COMPATIBLE) {

            long compatibleCount = crossmatchResultRepository
                    .countByRequestIdAndCompatibility(req.getRequestId(), Compatibility.COMPATIBLE);
            CrossmatchRequest request = getRequestById(req.getRequestId());

            if (compatibleCount >= request.getRequiredUnits()) {
                // All required units have been crossmatched — mark request as MATCHED
                request.setStatus(CrossmatchStatus.MATCHED);
                log.info("CrossmatchRequest {} fully matched — {}/{} units compatible",
                        req.getRequestId(), compatibleCount, request.getRequiredUnits());
            } else {
                // Still need more units to be tested — keep PENDING
                log.info("CrossmatchRequest {} partially matched — {}/{} units compatible so far",
                        req.getRequestId(), compatibleCount, request.getRequiredUnits());
            }

            crossmatchRequestRepository.save(request);

            inventoryFeignClient.updateInventoryStatus(
                    req.getComponentId(),
                    InventoryStatusUpdateRequest.builder()
                            .status("RESERVED")
                            .reason("Reserved after crossmatch")
                            .build()
            );
        }
        return saved;
    }

    public CrossmatchResult getResultById(Long id) {
        return crossmatchResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrossmatchResult", id));
    }

    public List<CrossmatchResult> getResultsByRequest(Long requestId) {
        return crossmatchResultRepository.findByRequestId(requestId);
    }

    // --- ISSUE ---
    public IssueRecord issue(IssueRequestDto req) {
        // Check if component is RESERVED before issuing
        Map<String, Object> inventoryResponse =
                inventoryFeignClient.getInventoryByComponentId(req.getComponentId());

        String inventoryStatus = extractStatusFromApiResponse(inventoryResponse);

        // Only RESERVED components should be issued
        // (RESERVED means crossmatch was done and unit is allocated for this patient)
        if (!"RESERVED".equalsIgnoreCase(inventoryStatus)) {
            throw new ComponentAlreadyIssuedException(req.getComponentId(), inventoryStatus);
        }

        IssueRecord r = IssueRecord.builder()
                .componentId(req.getComponentId())
                .patientId(req.getPatientId())
                .issueDate(LocalDate.now())
                .issuedBy(req.getIssuedBy())
                .indication(req.getIndication())
                .status(IssueStatus.ISSUED)
                .build();
        IssueRecord saved = issueRecordRepository.save(r);

        inventoryFeignClient.updateInventoryStatus(
                req.getComponentId(),
                InventoryStatusUpdateRequest.builder()
                        .status("ISSUED")
                        .reason("Issued to patient")
                        .build()
        );
        return saved;
    }

    public Page<IssueRecord> getAllIssues(Pageable pageable) {
        return issueRecordRepository.findAll(pageable);
    }

    public IssueRecord getIssueById(Long id) {
        return issueRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IssueRecord", id));
    }

    public List<IssueRecord> getIssuesByPatient(Long patientId) {
        return issueRecordRepository.findByPatientId(patientId);
    }

    public List<IssueRecord> getIssuesByComponent(Long componentId) {
        return issueRecordRepository.findByComponentId(componentId);
    }

    public IssueRecord returnUnit(Long id) {
        IssueRecord r = getIssueById(id);
        r.setStatus(IssueStatus.RETURNED);
        return issueRecordRepository.save(r);
    }

    @SuppressWarnings("unchecked")
    private String extractStatusFromApiResponse(Map<String, Object> response) {
        // Try root level first
        Object directStatus = response.get("status");
        if (directStatus != null) {
            return String.valueOf(directStatus);
        }

        // Try inside "data" — ApiResponse wrapper
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object nestedStatus = ((Map<String, Object>) dataMap).get("status");
            if (nestedStatus != null) {
                return String.valueOf(nestedStatus);
            }
        }

        throw new IllegalStateException(
                "Unable to read status from inventory response: " + response);
    }
}