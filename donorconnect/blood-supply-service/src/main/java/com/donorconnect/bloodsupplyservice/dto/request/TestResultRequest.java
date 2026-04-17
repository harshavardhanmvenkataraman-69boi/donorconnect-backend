package com.donorconnect.bloodsupplyservice.dto.request;


import com.donorconnect.bloodsupplyservice.enums.TestStatus;
import com.donorconnect.bloodsupplyservice.enums.TestType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TestResultRequest {
    @NotNull
    private Long donationId;
    private TestType testType;
    private String result;
    private LocalDate resultDate;
    private String enteredBy;
    private TestStatus status;
}
