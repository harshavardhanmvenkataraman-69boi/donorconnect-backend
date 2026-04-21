package com.donorconnect.bloodsupplyservice.controller;



import com.donorconnect.bloodsupplyservice.dto.request.BloodComponentRequest;
import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
import com.donorconnect.bloodsupplyservice.enums.ComponentType;
import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import com.donorconnect.bloodsupplyservice.service.BloodComponentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/components")
@RequiredArgsConstructor
@Tag(name = "Blood Components", description = "Blood component processing and management")
public class BloodComponentController {

    private final BloodComponentService bloodComponentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "Create blood component")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody BloodComponentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Component created", bloodComponentService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER')")
    @Operation(summary = "All components (paginated)")
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Get all available components")
    public ResponseEntity<ApiResponse<?>> getAvailableComponents() {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getAvailable()));
    }

    @GetMapping("/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN','ROLE_INVENTORY_CONTROLLER','ROLE_TRANSFUSION_OFFICER')")
    @Operation(summary = "Get component by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getById(componentId)));
    }

    @GetMapping("/donation/{donationId}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN')")
    @Operation(summary = "Components from a donation")
    public ResponseEntity<ApiResponse<?>> getByDonation(@PathVariable Long donationId) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getByDonation(donationId)));
    }

    @GetMapping("/type/{componentType}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Filter by component type")
    public ResponseEntity<ApiResponse<?>> getByType(@PathVariable ComponentType componentType) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getByType(componentType)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Filter by component status")
    public ResponseEntity<ApiResponse<?>> getByStatus(@PathVariable ComponentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getByStatus(status)));
    }

    @PutMapping("/{componentId}/status")
    @PreAuthorize("hasAnyRole('ROLE_LAB_TECHNICIAN','ROLE_ADMIN','ROLE_TRANSFUSION_OFFICER')")
    @Operation(summary = "Update component status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long componentId,
                                                       @RequestParam ComponentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", bloodComponentService.updateStatus(componentId, status)));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Components expiring in N days")
    public ResponseEntity<ApiResponse<?>> getExpiring(@RequestParam(defaultValue = "3") int days) {
        return ResponseEntity.ok(ApiResponse.success(bloodComponentService.getExpiring(days)));
    }
}