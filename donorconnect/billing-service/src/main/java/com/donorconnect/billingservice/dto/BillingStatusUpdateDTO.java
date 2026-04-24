package com.donorconnect.billingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    private String status;
}
