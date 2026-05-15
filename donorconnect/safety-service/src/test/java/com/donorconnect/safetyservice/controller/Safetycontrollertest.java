package com.donorconnect.safetyservice.controller;

import com.donorconnect.safetyservice.config.SecurityConfig;
import com.donorconnect.safetyservice.dto.request.LookbackRequest;
import com.donorconnect.safetyservice.dto.request.ReactionRequest;
import com.donorconnect.safetyservice.entity.LookbackTrace;
import com.donorconnect.safetyservice.entity.Reaction;
import com.donorconnect.safetyservice.enums.LookbackStatus;
import com.donorconnect.safetyservice.enums.ReactionStatus;
import com.donorconnect.safetyservice.enums.Severity;
import com.donorconnect.safetyservice.security.JwtAuthenticationFilter;
import com.donorconnect.safetyservice.service.SafetyService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = SafetyController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtAuthenticationFilter.class, SecurityConfig.class }
        )
)
@DisplayName("SafetyController Tests")
class SafetyControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SafetyService reactionService;

    private Reaction      sampleReaction;
    private LookbackTrace sampleTrace;

    @BeforeEach
    void setUp() {
        // Reaction entity: reactionId, issueId, patientId, reactionType,
        // severity, reactionDate, notes, status
        sampleReaction = Reaction.builder()
                .reactionId(1L)
                .issueId(10L)
                .patientId(100L)
                .reactionType("Febrile")
                .severity(Severity.MILD)
                .reactionDate(LocalDate.now())
                .notes("Minor fever after transfusion")
                .status(ReactionStatus.PENDING)
                .build();

        // LookbackTrace entity: traceId, donationId, componentId, patientId,
        // traceDate, status (LookbackStatus: OPEN → TRACED → CLOSED)
        sampleTrace = LookbackTrace.builder()
                .traceId(1L)
                .donationId(5L)
                .componentId(20L)
                .patientId(100L)
                .traceDate(LocalDate.now())
                .status(LookbackStatus.TRACED)
                .build();
    }

    // ─────────────────────────────────────────────
    // REACTIONS
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /reactions — logs reaction with PENDING status")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void createReaction_success() throws Exception {
        ReactionRequest req = new ReactionRequest();
        req.setIssueId(10L);
        req.setPatientId(100L);
        req.setSeverity(Severity.MILD);
        req.setReactionType("Febrile");
        req.setNotes("Minor fever");

        when(reactionService.create(any())).thenReturn(sampleReaction);

        mockMvc.perform(post("/api/v1/safety/reactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reactionId").value(1))
                .andExpect(jsonPath("$.data.issueId").value(10))
                .andExpect(jsonPath("$.data.patientId").value(100))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.severity").value("MILD"));
    }

    @Test
    @DisplayName("POST /reactions — returns 403 for wrong role")
    @WithMockUser(roles = "LAB_TECHNICIAN")
    void createReaction_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/safety/reactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /reactions — returns paginated reactions")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getAllReactions_success() throws Exception {
        when(reactionService.getAll(any()))
                .thenReturn(new PageImpl<>(List.of(sampleReaction), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/safety/reactions?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].reactionId").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /reactions/{id} — returns reaction by ID")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getReactionById_success() throws Exception {
        when(reactionService.getById(1L)).thenReturn(sampleReaction);

        mockMvc.perform(get("/api/v1/safety/reactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reactionId").value(1))
                .andExpect(jsonPath("$.data.issueId").value(10));
    }

    @Test
    @DisplayName("GET /reactions/patient/{id} — returns reactions for patient")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getReactionsByPatient_success() throws Exception {
        when(reactionService.getReactionsByPatient(100L)).thenReturn(List.of(sampleReaction));

        mockMvc.perform(get("/api/v1/safety/reactions/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].patientId").value(100));
    }

    @Test
    @DisplayName("GET /reactions/severity/{severity} — filters by MILD severity")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getReactionsBySeverity_success() throws Exception {
        when(reactionService.getBySeverity(Severity.MILD)).thenReturn(List.of(sampleReaction));

        mockMvc.perform(get("/api/v1/safety/reactions/severity/MILD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].severity").value("MILD"));
    }

    @Test
    @DisplayName("PATCH /reactions/{id}/status — updates to INVESTIGATING")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void updateReactionStatus_toInvestigating() throws Exception {
        sampleReaction.setStatus(ReactionStatus.INVESTIGATING);
        when(reactionService.updateStatus(1L, ReactionStatus.INVESTIGATING))
                .thenReturn(sampleReaction);

        mockMvc.perform(patch("/api/v1/safety/reactions/1/status")
                        .with(csrf())
                        .param("status", "INVESTIGATING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INVESTIGATING"));
    }

    @Test
    @DisplayName("PATCH /reactions/{id}/status — updates to CLOSED")
    @WithMockUser(roles = "ADMIN")
    void updateReactionStatus_toClosed() throws Exception {
        sampleReaction.setStatus(ReactionStatus.CLOSED);
        when(reactionService.updateStatus(1L, ReactionStatus.CLOSED))
                .thenReturn(sampleReaction);

        mockMvc.perform(patch("/api/v1/safety/reactions/1/status")
                        .with(csrf())
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    // ─────────────────────────────────────────────
    // LOOKBACK — WRITE (Admin only)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /lookback — initiates trace with TRACED status (Admin only)")
    @WithMockUser(roles = "ADMIN")
    void createLookbackTrace_success() throws Exception {
        LookbackRequest req = new LookbackRequest();
        req.setDonationId(5L);
        req.setComponentId(20L);
        req.setPatientId(100L);

        when(reactionService.createTrace(any())).thenReturn(sampleTrace);

        mockMvc.perform(post("/api/v1/safety/lookback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.traceId").value(1))
                .andExpect(jsonPath("$.data.donationId").value(5))
                .andExpect(jsonPath("$.data.componentId").value(20))
                .andExpect(jsonPath("$.data.patientId").value(100))
                .andExpect(jsonPath("$.data.status").value("TRACED"));
    }

    @Test
    @DisplayName("POST /lookback — returns 403 for TRANSFUSION_OFFICER")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void createLookbackTrace_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/safety/lookback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /lookback/{id}/status — closes trace (Admin only)")
    @WithMockUser(roles = "ADMIN")
    void updateLookbackStatus_toClosed() throws Exception {
        sampleTrace.setStatus(LookbackStatus.CLOSED);
        when(reactionService.updateLookbackStatus(1L, LookbackStatus.CLOSED))
                .thenReturn(sampleTrace);

        mockMvc.perform(patch("/api/v1/safety/lookback/1/status")
                        .with(csrf())
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("PATCH /lookback/{id}/status — returns 403 for TRANSFUSION_OFFICER")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void updateLookbackStatus_forbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/safety/lookback/1/status")
                        .with(csrf())
                        .param("status", "CLOSED"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // LOOKBACK — READ (Both roles)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /lookback/donation/{id} — returns traces for donation")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getLookbackByDonation_success() throws Exception {
        when(reactionService.getByDonation(5L)).thenReturn(List.of(sampleTrace));

        mockMvc.perform(get("/api/v1/safety/lookback/donation/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].donationId").value(5))
                .andExpect(jsonPath("$.data[0].traceId").value(1));
    }

    @Test
    @DisplayName("GET /lookback/patient/{id} — returns traces for patient")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getLookbackByPatient_success() throws Exception {
        when(reactionService.getLookbackByPatient(100L)).thenReturn(List.of(sampleTrace));

        mockMvc.perform(get("/api/v1/safety/lookback/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].patientId").value(100));
    }

    @Test
    @DisplayName("GET /lookback/component/{id} — returns traces for component")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getLookbackByComponent_success() throws Exception {
        when(reactionService.getByComponent(20L)).thenReturn(List.of(sampleTrace));

        mockMvc.perform(get("/api/v1/safety/lookback/component/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].componentId").value(20));
    }

    @Test
    @DisplayName("GET /lookback/exists/patient/{id} — returns true when trace exists")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void lookbackExistsForPatient_exists() throws Exception {
        when(reactionService.getTracesByPatient(100L)).thenReturn(List.of(sampleTrace));

        mockMvc.perform(get("/api/v1/safety/lookback/exists/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    @DisplayName("GET /lookback/exists/patient/{id} — returns false when no trace")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void lookbackExistsForPatient_notExists() throws Exception {
        when(reactionService.getTracesByPatient(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/safety/lookback/exists/patient/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    // ─────────────────────────────────────────────
    // LOOKBACK DETAILS — Admin only
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /lookback-details/{donationId} — full investigation (Admin only)")
    @WithMockUser(roles = "ADMIN")
    void getLookbackDetails_success() throws Exception {
        Map<String, Object> details = Map.of(
                "donation",   Map.of("donationId", 5, "donorId", 99),
                "donor",      Map.of("name", "John Kumar", "bloodGroup", "A"),
                "components", List.of()
        );
        when(reactionService.getLookbackDetails(5L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/safety/lookback-details/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.donation.donationId").value(5))
                .andExpect(jsonPath("$.data.donor.name").value("John Kumar"));
    }

    @Test
    @DisplayName("GET /lookback-details — returns 403 for TRANSFUSION_OFFICER")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getLookbackDetails_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/safety/lookback-details/5"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // LOOKUP HELPERS
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /issue-component/{issueId} — returns componentId for issue")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getComponentIdByIssue_success() throws Exception {
        when(reactionService.getComponentIdByIssue(10L)).thenReturn(20L);

        mockMvc.perform(get("/api/v1/safety/issue-component/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.componentId").value(20));
    }

    @Test
    @DisplayName("GET /component-donation/{componentId} — returns donationId for component")
    @WithMockUser(roles = "TRANSFUSION_OFFICER")
    void getDonationIdByComponent_success() throws Exception {
        when(reactionService.getDonationIdByComponent(20L)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/safety/component-donation/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.donationId").value(5));
    }

    // ─────────────────────────────────────────────
    // SECURITY
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /reactions — returns 401 when unauthenticated")
    void getAllReactions_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/safety/reactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /lookback — returns 401 when unauthenticated")
    void createLookback_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/safety/lookback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}