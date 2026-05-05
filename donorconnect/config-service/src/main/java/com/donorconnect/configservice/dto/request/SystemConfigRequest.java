package com.donorconnect.configservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemConfigRequest {
    @NotBlank
    private String key;
    private String value;
    private String scope;
}