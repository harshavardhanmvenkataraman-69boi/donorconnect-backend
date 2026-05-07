package com.donorconnect.reportingservice.dto;
import com.donorconnect.reportingservice.enums.TestStatus;
import com.donorconnect.reportingservice.enums.TestType;
import lombok.Data;
@Data
public class TestResultDto {
    private Long testId;
    private TestType testType;
    private TestStatus status;
}
