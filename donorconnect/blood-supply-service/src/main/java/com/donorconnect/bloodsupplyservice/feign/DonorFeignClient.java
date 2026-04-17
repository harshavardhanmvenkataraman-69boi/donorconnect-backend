package com.donorconnect.bloodsupplyservice.feign;

import com.donorconnect.bloodsupplyservice.config.FeignConfig;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "donor-service", path = "/api/v1/donors", configuration = FeignConfig.class)
public interface DonorFeignClient {

    @GetMapping("/{donorId}")
    ApiResponse<?> getDonorById(@PathVariable Long donorId);
}
