package com.donorconnect.transfusionservice.controller;

import com.donorconnect.transfusionservice.config.SecurityConfig;
import com.donorconnect.transfusionservice.dto.request.CrossmatchRequestDto;
import com.donorconnect.transfusionservice.dto.request.CrossmatchResultRequest;
import com.donorconnect.transfusionservice.dto.request.IssueRequestDto;
import com.donorconnect.transfusionservice.entity.CrossmatchRequest;
import com.donorconnect.transfusionservice.entity.CrossmatchResult;
import com.donorconnect.transfusionservice.entity.IssueRecord;
import com.donorconnect.transfusionservice.enums.*;
import com.donorconnect.transfusionservice.security.JwtAuthenticationFilter;
import com.donorconnect.transfusionservice.service.TransfusionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransfusionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtAuthenticationFilter.class, SecurityConfig.class }
        )
)
@DisplayName("TransfusionController Tests")
class TransfusionControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean TransfusionService transfusionService;

    private CrossmatchRequest sampleRequest;
    private CrossmatchResult  sampleResult;
    private IssueRecord       sampleIssue;

    @BeforeEach
    void setUp() {
        // CrossmatchRequest entity: requestId, patientId, orderBy, bloodGroup,
        // rhFactor, requiredUnits, priority, requestDate, status, notes, availableComponentIds
        sampleRequest = CrossmatchRequest.builder()
                .requestId(1L)
                .patientId(100L)
                .bloodGroup("A")
                .rhFactor("POSITIVE")
                .requiredUnits(2)
                .priority(CrossmatchPriority.ROUTINE)
                .requestDate(LocalDate.now())
                .status(CrossmatchStatus.PENDING)
                .build();

        // CrossmatchResult entity: crossmatchId, requestId, componentId,
        // compatibility, testedBy, testedDate, status
        sampleResult = CrossmatchResult.builder()
                .crossmatchId(1L)
                .requestId(1L)
                .componentId(10L)
                .compatibility(Compatibility.COMPATIBLE)
                .testedBy("Lab Tech")
                .testedDate(LocalDate.now())
                .status(CrossmatchStatus.MATCHED)
                .build();

        // IssueRecord entity: issueId, componentId, patientId,
        // issueDate, issuedBy, indication, status
        sampleIssue = IssueRecord.builder()
                .issueId(1L)
                .componentId(10L)
                .patientId(100L)
                .issueDate(LocalDate.now())
                .issuedBy("Officer A")
                .indication("Anaemia")
                .status(IssueStatus.ISSUED)
                .build();
    }

    // ─────────────────────────────────────────────
    // CROSSMATCH REQUESTS
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /crossmatch/requests — creates request successfully")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void createRequest_success() throws Exception {
        CrossmatchRequestDto dto = new CrossmatchRequestDto();
        dto.setPatientId(100L);
        dto.setBloodGroup("A");
        dto.setRhFactor("POSITIVE");
        dto.setRequiredUnits(2);

        when(transfusionService.createRequest(any())).thenReturn(sampleRequest);

        mockMvc.perform(post("/transfusion/api/v1/crossmatch/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.patientId").value(100))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /crossmatch/requests — returns paginated list")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getAllRequests_success() throws Exception {
        when(transfusionService.getAllRequests(any()))
                .thenReturn(new PageImpl<>(List.of(sampleRequest), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/transfusion/api/v1/crossmatch/requests?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].requestId").value(1));
    }

    @Test
    @DisplayName("GET /crossmatch/requests/{id} — returns request by ID")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getRequestById_success() throws Exception {
        when(transfusionService.getRequestById(1L)).thenReturn(sampleRequest);

        mockMvc.perform(get("/transfusion/api/v1/crossmatch/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(1));
    }

    @Test
    @DisplayName("GET /crossmatch/requests/patient/{id} — returns requests for patient")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getRequestsByPatient_success() throws Exception {
        when(transfusionService.getRequestsByPatient(100L)).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get("/transfusion/api/v1/crossmatch/requests/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].patientId").value(100));
    }

    @Test
    @DisplayName("GET /crossmatch/requests/pending — returns pending requests")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getPendingRequests_success() throws Exception {
        when(transfusionService.getPendingRequests()).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get("/transfusion/api/v1/crossmatch/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("PATCH /crossmatch/requests/{id}/status — updates status to MATCHED")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void updateRequestStatus_success() throws Exception {
        sampleRequest.setStatus(CrossmatchStatus.MATCHED);
        when(transfusionService.updateRequestStatus(1L, CrossmatchStatus.MATCHED))
                .thenReturn(sampleRequest);

        mockMvc.perform(patch("/transfusion/api/v1/crossmatch/requests/1/status")
                        .with(csrf())
                        .param("status", "MATCHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MATCHED"));
    }

    // ─────────────────────────────────────────────
    // CROSSMATCH RESULTS
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /crossmatch/results — records COMPATIBLE result")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void createResult_compatible() throws Exception {
        CrossmatchResultRequest req = new CrossmatchResultRequest();
        req.setRequestId(1L);
        req.setComponentId(10L);
        req.setCompatibility(Compatibility.COMPATIBLE);
        req.setTestedBy("Lab Tech");

        when(transfusionService.createResult(any())).thenReturn(sampleResult);

        mockMvc.perform(post("/transfusion/api/v1/crossmatch/results")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.compatibility").value("COMPATIBLE"))
                .andExpect(jsonPath("$.data.componentId").value(10));
    }

    @Test
    @DisplayName("POST /crossmatch/results — records INCOMPATIBLE result")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void createResult_incompatible() throws Exception {
        CrossmatchResultRequest req = new CrossmatchResultRequest();
        req.setRequestId(1L);
        req.setComponentId(10L);
        req.setCompatibility(Compatibility.INCOMPATIBLE);
        req.setTestedBy("Lab Tech");

        CrossmatchResult incompatibleResult = CrossmatchResult.builder()
                .crossmatchId(2L).requestId(1L).componentId(10L)
                .compatibility(Compatibility.INCOMPATIBLE)
                .testedBy("Lab Tech").testedDate(LocalDate.now())
                .status(CrossmatchStatus.REJECTED)
                .build();

        when(transfusionService.createResult(any())).thenReturn(incompatibleResult);

        mockMvc.perform(post("/transfusion/api/v1/crossmatch/results")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.compatibility").value("INCOMPATIBLE"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /crossmatch/results/{id} — returns result by ID")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getResultById_success() throws Exception {
        when(transfusionService.getResultById(1L)).thenReturn(sampleResult);

        mockMvc.perform(get("/transfusion/api/v1/crossmatch/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.crossmatchId").value(1));
    }

    @Test
    @DisplayName("GET /crossmatch/results/request/{id} — returns results for request")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getResultsByRequest_success() throws Exception {
        when(transfusionService.getResultsByRequest(1L)).thenReturn(List.of(sampleResult));

        mockMvc.perform(get("/transfusion/api/v1/crossmatch/results/request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].componentId").value(10));
    }

    // ─────────────────────────────────────────────
    // ISSUE
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /issue — issues blood component to patient")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void issue_success() throws Exception {
        IssueRequestDto dto = new IssueRequestDto();
        dto.setComponentId(10L);
        dto.setPatientId(100L);
        dto.setIssuedBy("Officer A");
        dto.setIndication("Anaemia");

        when(transfusionService.issue(any())).thenReturn(sampleIssue);

        mockMvc.perform(post("/transfusion/api/v1/issue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.issueId").value(1))
                .andExpect(jsonPath("$.data.status").value("ISSUED"))
                .andExpect(jsonPath("$.data.patientId").value(100))
                .andExpect(jsonPath("$.data.componentId").value(10));
    }

    @Test
    @DisplayName("GET /issue — returns paginated issue records")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getAllIssues_success() throws Exception {
        when(transfusionService.getAllIssues(any()))
                .thenReturn(new PageImpl<>(List.of(sampleIssue), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/transfusion/api/v1/issue?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].issueId").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /issue/{id} — returns issue by ID")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getIssueById_success() throws Exception {
        when(transfusionService.getIssueById(1L)).thenReturn(sampleIssue);

        mockMvc.perform(get("/transfusion/api/v1/issue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.issueId").value(1));
    }

    @Test
    @DisplayName("GET /issue/patient/{id} — returns issues for patient")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getIssuesByPatient_success() throws Exception {
        when(transfusionService.getIssuesByPatient(100L)).thenReturn(List.of(sampleIssue));

        mockMvc.perform(get("/transfusion/api/v1/issue/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].patientId").value(100));
    }

    @Test
    @DisplayName("GET /issue/component/{id} — returns issues for component")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getIssuesByComponent_success() throws Exception {
        when(transfusionService.getIssuesByComponent(10L)).thenReturn(List.of(sampleIssue));

        mockMvc.perform(get("/transfusion/api/v1/issue/component/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].componentId").value(10));
    }

    @Test
    @DisplayName("PATCH /issue/{id}/return — marks unit as RETURNED")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void returnUnit_success() throws Exception {
        sampleIssue.setStatus(IssueStatus.RETURNED);
        when(transfusionService.returnUnit(1L)).thenReturn(sampleIssue);

        mockMvc.perform(patch("/transfusion/api/v1/issue/1/return").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"));
    }

    // ─────────────────────────────────────────────
    // SECURITY
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /issue — returns 401 when unauthenticated")
    void issue_unauthorized() throws Exception {
        mockMvc.perform(post("/transfusion/api/v1/issue")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /crossmatch/requests — returns 403 for wrong role")
    @WithMockUser(roles = "LAB_TECHNICIAN")
    void createRequest_forbidden() throws Exception {
        mockMvc.perform(post("/transfusion/api/v1/crossmatch/requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /issue — returns 401 when unauthenticated")
    void getAllIssues_unauthorized() throws Exception {
        mockMvc.perform(get("/transfusion/api/v1/issue"))
                .andExpect(status().isUnauthorized());
    }
}