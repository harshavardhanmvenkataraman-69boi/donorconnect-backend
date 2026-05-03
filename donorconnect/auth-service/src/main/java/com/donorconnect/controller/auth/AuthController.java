package com.donorconnect.controller.auth;

import com.donorconnect.dto.request.auth.*;
import com.donorconnect.dto.response.ApiResponse;
import com.donorconnect.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login, register, password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/setup-admin")
    @Operation(summary = "One-time admin setup - only works if no admin exists yet")
    public ResponseEntity<ApiResponse<?>> setupAdmin(@RequestBody Setupadminrequest request) {
        String message = authService.setupFirstAdmin(request);

        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for: " + request.getEmail());

        var result = authService.login(request);

        log.info("Login success for: "+ request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login successful", result));
    }


    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Register new user (Admin token required) - can assign any role")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: " + request.getEmail() +" with role: " + request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User registered successfully",
                authService.register(request)));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change own password (token required)")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordRequest request,
                                                         Authentication auth) {
        authService.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(email), null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(request), null));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (records audit entry)")
    public ResponseEntity<ApiResponse<?>> logout(Authentication auth) {
        authService.logout(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Logout recorded", null));
    }
}
