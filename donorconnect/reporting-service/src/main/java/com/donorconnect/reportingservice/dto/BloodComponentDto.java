package com.donorconnect.reportingservice.dto;
import com.donorconnect.reportingservice.enums.ComponentStatus;
import com.donorconnect.reportingservice.enums.ComponentType;
import lombok.Data;
import java.time.LocalDate;
@Data
public class BloodComponentDto {
    private Long componentId;
    private ComponentType componentType;
    private ComponentStatus status;
    private LocalDate expiryDate;
}
