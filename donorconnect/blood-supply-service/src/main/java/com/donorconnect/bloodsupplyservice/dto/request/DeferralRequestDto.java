package com.donorconnect.bloodsupplyservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local DTO mirroring donor-service's DeferralRequest.
 * Used by DeferralFeignClient.createDeferral when a donation tests reactive.
 *
 * deferralType is a String ("PERMANENT" or "TEMPORARY") to avoid
 * pulling donor-service's enum into this service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeferralRequestDto {
    private Long donorId;
    private String deferralType;
    private String reason;
}
