package com.donorconnect.donorservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DriveRequest {
    @NotBlank private String name;
    private String location;
    private LocalDate scheduledDate;
    private Integer capacity;
    private String organizer;
}
