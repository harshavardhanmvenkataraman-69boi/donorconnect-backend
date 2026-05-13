package com.donorconnect.dto.request.auth;

import com.donorconnect.enums.Enums.*;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min=6) private String password;
    @NotNull private UserRole role;
    private String phone;
}