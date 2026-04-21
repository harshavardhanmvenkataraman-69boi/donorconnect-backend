package com.donorconnect.inventoryservice.controller;

import com.donorconnect.inventoryservice.dto.request.*;
import com.donorconnect.inventoryservice.dto.response.*;
import com.donorconnect.inventoryservice.enums.TransactionType;
import com.donorconnect.inventoryservice.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Blood inventory, stock transactions and expiry watch")
public class InventoryController {

    private final InventoryService inventoryService;

    // ─── INVENTORY BALANCE ────────────────────────────────────────────

    /**
     * Called by blood-supply-service via Feign when a new Component is created.
     * Not exposed to external clients directly.
     */
    @PostMapping("/api/v1/inventory/entry")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER', 'ROLE_ADMIN', 'ROLE_LAB_TECHNICIAN')")
    @Operation(summary = "Create inventory entry (called by blood-supply-service via Feign)")
    public ResponseEntity<ApiResponse<InventoryBalanceResponse>> createEntry(
            @Valid @RequestBody InventoryEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory entry created", inventoryService.createEntry(request)));
    }

    /**
     * Called by blood-supply-service via Feign when Component status changes.
     */
    @PatchMapping("/api/v1/inventory/{componentId}/status")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER', 'ROLE_ADMIN', 'ROLE_TRANSFUSION_OFFICER')") 
    @Operation(summary = "Update inventory status (called by blood-supply-service via Feign)")
    public ResponseEntity<ApiResponse<InventoryBalanceResponse>> updateStatus(
            @PathVariable Long componentId,
            @Valid @RequestBody InventoryStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", inventoryService.updateStatus(componentId, request)));
    }

    @GetMapping("/api/v1/inventory")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Full inventory snapshot")
    public ResponseEntity<ApiResponse<List<InventoryBalanceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAll()));
    }

    @GetMapping("/api/v1/inventory/available")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER', 'ROLE_TRANSFUSION_OFFICER', 'ROLE_ADMIN')")
    @Operation(summary = "Get available units by blood group and component type")
    public ResponseEntity<ApiResponse<List<InventoryBalanceResponse>>> getAvailable(
        @RequestParam String bloodGroup, @RequestParam String rhFactor, @RequestParam String componentType) {
            return ResponseEntity.ok(ApiResponse.success(inventoryService.getAvailableUnits(bloodGroup, rhFactor, componentType)));
    }
    
    @GetMapping("/api/v1/inventory/blood-group/{bloodGroup}")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Stock by blood group")
    public ResponseEntity<ApiResponse<List<InventoryBalanceResponse>>> getByBloodGroup(
            @PathVariable String bloodGroup) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByBloodGroup(bloodGroup)));
    }

    @GetMapping("/api/v1/inventory/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_TRANSFUSION_OFFICER', 'ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Get inventory record by componentID")
    public ResponseEntity<ApiResponse<List<InventoryBalanceResponse>>> getByComponentId(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByComponentId(componentId)));
    }

    @GetMapping("/api/v1/inventory/low-stock")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Items below threshold")
    public ResponseEntity<ApiResponse<List<InventoryBalanceResponse>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStock()));
    }

    @GetMapping("/api/v1/inventory/summary")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER', 'ROLE_ADMIN', 'ROLE_TRANSFUSION_OFFICER')")
    @Operation(summary = "Summary grid (blood group x component type)")
    public ResponseEntity<ApiResponse<List<InventorySummaryResponse>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getSummary()));
    }

    // ─── STOCK TRANSACTIONS ───────────────────────────────────────────

    @PostMapping("/api/v1/stock-transactions")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Record stock transaction")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> createTransaction(
            @Valid @RequestBody StockTransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transaction recorded", inventoryService.createTransaction(request)));
    }

    @GetMapping("/api/v1/stock-transactions")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "All transactions (paginated)")
    public ResponseEntity<ApiResponse<?>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllTransactions(PageRequest.of(page, size))));
    }

    @GetMapping("/api/v1/stock-transactions/{txnId}")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> getTransactionById(@PathVariable Long txnId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getTransactionById(txnId)));
    }

    @GetMapping("/api/v1/stock-transactions/component/{componentId}")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Transactions for a component")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getTransactionsByComponent(componentId)));
    }

    @GetMapping("/api/v1/stock-transactions/type/{txnType}")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Filter by transaction type")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getByType(@PathVariable TransactionType txnType) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getTransactionsByType(txnType)));
    }

    // ─── EXPIRY WATCH ─────────────────────────────────────────────────

    @GetMapping("/api/v1/expiry-watch")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "All expiry watch records")
    public ResponseEntity<ApiResponse<List<ExpiryWatchResponse>>> getAllExpiryWatch() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllExpiryWatch()));
    }

    @GetMapping("/api/v1/expiry-watch/open")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Unactioned expiry alerts")
    public ResponseEntity<ApiResponse<List<ExpiryWatchResponse>>> getOpenExpiryWatch() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getOpenExpiryWatch()));
    }

    @PatchMapping("/api/v1/expiry-watch/{expiryId}/action")
    @PreAuthorize("hasAnyRole('ROLE_INVENTORY_CONTROLLER','ROLE_ADMIN')")
    @Operation(summary = "Mark expiry alert as actioned")
    public ResponseEntity<ApiResponse<ExpiryWatchResponse>> actionExpiryWatch(@PathVariable Long expiryId) {
        return ResponseEntity.ok(ApiResponse.success("Expiry watch actioned", inventoryService.actionExpiryWatch(expiryId)));
    }
}