package com.donorconnect.controller.auth;

import com.donorconnect.dto.response.ApiResponse;
import com.donorconnect.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(authService.getAllAuditLogs(PageRequest.of(page, size))));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(authService.getAuditLogsByUser(userId)));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getByAction(@PathVariable String action) {
        return ResponseEntity.ok(ApiResponse.success(authService.getAuditLogsByAction(action)));
    }
}