package com.donorconnect.bloodsupplyservice.dto.request;

import com.donorconnect.bloodsupplyservice.enums.TestType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * Bulk-entry request for entering all required tests for a donation in one go.
 *
 * Example body:
 * {
 *   "donationId": 12,
 *   "enteredBy": "lab1",
 *   "resultDate": "2026-05-13",
 *   "results": {
 *     "HIV":         "Non-Reactive",
 *     "HBV":         "Non-Reactive",
 *     "HCV":         "Non-Reactive",
 *     "VDRL":        "Non-Reactive",
 *     "MALARIA":     "Non-Reactive",
 *     "BLOOD_GROUP": "A",
 *     "RH":          "POSITIVE"
 *   }
 * }
 *
 * The service iterates each entry and creates a TestResult. If ANY entry is
 * "REACTIVE" (case-insensitive), the standard reactive pipeline fires.
 */
@Data
public class BulkTestResultRequest {
    @NotNull
    private Long donationId;

    private LocalDate resultDate;
    private String enteredBy;

    /** Map of TestType -> result string. Each entry becomes one TestResult row. */
    @NotNull
    private Map<TestType, String> results;
}
