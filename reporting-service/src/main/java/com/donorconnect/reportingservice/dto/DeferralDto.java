package com.donorconnect.reportingservice.dto;
import com.donorconnect.reportingservice.enums.DeferralType;
import lombok.Data;
@Data
public class DeferralDto {
    private Long deferralId;
    private DeferralType deferralType;
}
