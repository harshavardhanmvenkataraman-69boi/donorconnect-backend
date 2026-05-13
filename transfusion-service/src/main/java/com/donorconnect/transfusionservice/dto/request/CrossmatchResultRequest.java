package com.donorconnect.transfusionservice.dto.request;

import com.donorconnect.transfusionservice.enums.Compatibility;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
@Data
public class CrossmatchResultRequest {
    @NotNull
    private Long requestId;
    @NotNull private Long componentId;
    private Compatibility compatibility;
    private String testedBy;
    private LocalDate testedDate;
}