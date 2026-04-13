package com.donorconnect.donorservice.dto.request;
import com.donorconnect.donorservice.enums.DonorType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DonorRequest {
    @NotBlank private String name;
    private String dob;
    private String gender;
    private String bloodGroup;
    private String rhFactor;
    private String contactInfo;
    private String addressJson;
    private DonorType donorType;
}
