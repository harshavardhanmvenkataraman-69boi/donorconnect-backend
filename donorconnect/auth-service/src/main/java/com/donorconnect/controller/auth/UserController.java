package com.donorconnect.controller.auth;

import com.donorconnect.dto.response.ApiResponse;
import com.donorconnect.enums.Enums.*;
import com.donorconnect.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController // @Controller + @ResponseBody
@RequestMapping("/api/v1/users") // used to map http requests to a controller class or method
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Manage system users")
public class UserController {

        private final AuthService authService;

    @GetMapping // read and fetch data
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all users (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(authService.getAllUsers(PageRequest.of(page, size))));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_TRANSFUSION_OFFICER')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(authService.getUserById(userId)));
    }


    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Filter users by role")
    public ResponseEntity<ApiResponse<?>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(ApiResponse.success(authService.getUsersByRole(role)));
        // role -> The variable name used inside your Java logic
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Filter users by status")
    public ResponseEntity<ApiResponse<?>> getUsersByStatus(@PathVariable UserStatus status) {
        return ResponseEntity.ok(ApiResponse.success(authService.getUsersByStatus(status)));
    }


    @PatchMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Lock user account")
    public ResponseEntity<ApiResponse<?>> lockUser(@PathVariable Long userId) {
        authService.lockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User locked", null));
    }

    @PatchMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Unlock user account")
    public ResponseEntity<ApiResponse<?>> unlockUser(@PathVariable Long userId) {
        authService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User unlocked", null));
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<ApiResponse<?>> deactivateUser(@PathVariable Long userId) {
        authService.deactivateUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }
}
