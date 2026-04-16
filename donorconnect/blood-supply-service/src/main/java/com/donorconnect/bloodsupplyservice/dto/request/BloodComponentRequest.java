package com.donorconnect.bloodsupplyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import com.donorconnect.bloodsupplyservice.enums.ComponentType;

import java.time.LocalDate;

@Data
public class BloodComponentRequest {
    @NotNull
    private Long donationId;
    @NotNull private ComponentType componentType;
    private String bagNumber;
    private Integer volume;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
}