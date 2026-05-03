package com.donorconnect.configservice.controller;

import com.donorconnect.configservice.dto.request.SystemConfigRequest;
import com.donorconnect.configservice.dto.response.ApiResponse;
import com.donorconnect.configservice.service.SystemConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
@Tag(name = "Admin Configuration", description = "System configuration management")
public class SystemConfigController {

    private final SystemConfigService service;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create system config entry")
    public ResponseEntity<ApiResponse<?>> create(
            @Valid @RequestBody SystemConfigRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Config created",
                service.create(req, auth.getName())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get all config entries")
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @GetMapping("/{configId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get config by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long configId) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(configId)));
    }

    @GetMapping("/key/{key}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get config by key")
    public ResponseEntity<ApiResponse<?>> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(service.getByKey(key)));
    }

    @GetMapping("/scope/{scope}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "Get configs by scope (GLOBAL or SITE)")
    public ResponseEntity<ApiResponse<?>> getByScope(@PathVariable String scope) {
        return ResponseEntity.ok(ApiResponse.success(service.getByScope(scope)));
    }

    @PutMapping("/{configId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update config entry")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable Long configId,
            @RequestBody SystemConfigRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Config updated",
                service.update(configId, req, auth.getName())));
    }

    @DeleteMapping("/{configId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete config entry")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long configId) {
        service.delete(configId);
        return ResponseEntity.ok(ApiResponse.success("Config deleted", null));
    }
}