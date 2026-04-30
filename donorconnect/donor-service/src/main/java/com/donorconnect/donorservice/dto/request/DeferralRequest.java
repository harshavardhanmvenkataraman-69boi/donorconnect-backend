package com.donorconnect.donorservice.dto.request;

import com.donorconnect.donorservice.enums.DeferralType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeferralRequest {
    @NotNull
    private Long donorId;
    private DeferralType deferralType;
    private String reason;
}