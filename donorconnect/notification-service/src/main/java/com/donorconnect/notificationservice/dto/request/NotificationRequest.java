package com.donorconnect.notificationservice.dto.request;

import com.donorconnect.notificationservice.enums.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotNull
    private Long userId;

    @NotBlank
    private String message;

    @NotNull
    private NotificationCategory category;
}
