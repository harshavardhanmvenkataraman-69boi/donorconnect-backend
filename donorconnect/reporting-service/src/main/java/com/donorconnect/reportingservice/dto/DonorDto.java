package com.donorconnect.reportingservice.dto;
import com.donorconnect.reportingservice.enums.DonorStatus;
import lombok.Data;
@Data
public class DonorDto {
    private Long donorId;
    private String name;
    private DonorStatus status;
}
