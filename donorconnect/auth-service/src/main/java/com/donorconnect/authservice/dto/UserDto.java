package com.donorconnect.authservice.dto;

import com.donorconnect.authservice.enums.UserRole;
import com.donorconnect.authservice.enums.UserStatus;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private Long userId;
    private String name;
    private UserRole role;
    private String email;
    private String phone;
    private UserStatus status;
}
