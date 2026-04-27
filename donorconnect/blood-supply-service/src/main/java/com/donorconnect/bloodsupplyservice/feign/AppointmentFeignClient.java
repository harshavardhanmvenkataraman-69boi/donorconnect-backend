package com.donorconnect.bloodsupplyservice.feign;

import com.donorconnect.bloodsupplyservice.config.FeignConfig;
import com.donorconnect.bloodsupplyservice.dto.AppointmentDto;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@FeignClient(
        name = "donor-service",
        contextId = "appointmentFeignClient",
        path = "/api/v1/appointments",
        configuration = FeignConfig.class
)
public interface AppointmentFeignClient {

    @GetMapping("/donor/{donorId}")
    ApiResponse<List<AppointmentDto>> getAppointmentsByDonor(
            @PathVariable Long donorId
    );

    @PatchMapping("/{appointmentId}/status")
    ApiResponse<?> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam String status
    );
}
