package com.donorconnect.inventoryservice.service;

import com.donorconnect.inventoryservice.dto.request.*;
import com.donorconnect.inventoryservice.dto.response.*;
import com.donorconnect.inventoryservice.entity.*;
import com.donorconnect.inventoryservice.enums.*;
import com.donorconnect.inventoryservice.exception.ResourceNotFoundException;
import com.donorconnect.inventoryservice.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryBalanceRepository balanceRepo;
    private final ExpiryWatchRepository expiryRepo;
    private final StockTransactionRepository txnRepo;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    @Value("${app.inventory.expiry-warning-days:7}")
    private int expiryWarningDays;

    // ─── INVENTORY BALANCE ───────────────────────────────────────────

    /**
     * Called by blood-supply-service via Feign when a new Component is created.
     * Creates the InventoryBalance entry with denormalized component metadata.
     */
    @Transactional
    public InventoryBalanceResponse createEntry(InventoryEntryRequest request) {
        if (balanceRepo.existsByComponentId(request.getComponentId())) {
            throw new IllegalStateException("InventoryBalance already exists for componentId=" + request.getComponentId());
        }
        InventoryBalance balance = InventoryBalance.builder()
                .componentId(request.getComponentId())
                .bloodGroup(request.getBloodGroup())
                .rhFactor(request.getRhFactor())
                .componentType(request.getComponentType())
                .expiryDate(request.getExpiryDate())
                .bagNumber(request.getBagNumber())
                .quantity(1)
                .status(InventoryStatus.AVAILABLE)
                .build();
        InventoryBalance saved = balanceRepo.save(balance);

        // Record a RECEIPT stock transaction
        recordTransaction(saved.getComponentId(),
                TransactionType.RECEIPT, 1, null, "Initial receipt from collection");

        // Check if expiry is within warning window
        if (request.getExpiryDate() != null) {
            long daysLeft = LocalDate.now().until(request.getExpiryDate()).getDays();
            if (daysLeft <= expiryWarningDays) {
                createExpiryWatchIfAbsent(saved.getComponentId(), request.getExpiryDate(), (int) daysLeft);
            }
        }
        log.info("InventoryBalance created for componentId={}", saved.getComponentId());
        return toBalanceResponse(saved);
    }

    /**
     * Called by blood-supply-service via Feign when Component status changes
     * (QUARANTINE, ISSUED, EXPIRED, DISPOSED).
     */
    @Transactional
    public InventoryBalanceResponse updateStatus(Long componentId, InventoryStatusUpdateRequest request) {
        InventoryBalance balance = balanceRepo.findByComponentId(componentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InventoryBalance not found for componentId=" + componentId));

        InventoryStatus prev = balance.getStatus();
        InventoryStatus newStatus = InventoryStatus.valueOf(request.getStatus().toUpperCase()); // convert here
        balance.setStatus(newStatus);

        // Decrement quantity for ISSUED/DISPOSED
        if (newStatus == InventoryStatus.ISSUED || newStatus == InventoryStatus.DISPOSED) {
            balance.setQuantity(Math.max(0, balance.getQuantity() - 1));
        }
        // Restore quantity if released from quarantine
        if (prev == InventoryStatus.QUARANTINED && newStatus == InventoryStatus.AVAILABLE) {
            balance.setQuantity(balance.getQuantity() + 1);
        }

        InventoryBalance saved = balanceRepo.save(balance);

        TransactionType txnType = switch (newStatus) {
            case ISSUED      -> TransactionType.ISSUE;
            case QUARANTINED -> TransactionType.QUARANTINE;
            case AVAILABLE   -> TransactionType.RELEASE;
            case RESERVED    -> TransactionType.ADJUST;
            case DISPOSED    -> TransactionType.ADJUST;
            default          -> TransactionType.ADJUST;
        };

        recordTransaction(componentId, txnType, 1, null, request.getReason());

        log.info("InventoryBalance status updated for componentId={}: {} → {}",
                componentId, prev, newStatus);
        return toBalanceResponse(saved);
    }

    public List<InventoryBalanceResponse> getAvailableUnits(String bloodGroup, String rhFactor, String componentType) {
        return balanceRepo.findByBloodGroupAndRhFactorAndComponentTypeAndStatus(
                        BloodGroup.valueOf(bloodGroup.toUpperCase()),
                        RhFactor.valueOf(rhFactor.toUpperCase()),
                        ComponentType.valueOf(componentType.toUpperCase()),
                        InventoryStatus.AVAILABLE)
                .stream()
                .map(this::toBalanceResponse)
                .collect(Collectors.toList());
    }

    public List<InventoryBalanceResponse> getAll() {
        return balanceRepo.findAll().stream().map(this::toBalanceResponse).collect(Collectors.toList());
    }

    public List<InventoryBalanceResponse> getByBloodGroup(String bloodGroup) {
        BloodGroup bg = BloodGroup.valueOf(bloodGroup.toUpperCase());
        return balanceRepo.findByBloodGroup(bg).stream().map(this::toBalanceResponse).collect(Collectors.toList());
    }
    
    public InventoryBalanceResponse getByComponentId(Long componentId) {
        InventoryBalance ib = balanceRepo.findByComponentId(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryBalance not found for componentId=" + componentId));
        
        return toBalanceResponse(ib);            
    }
    
    public List<InventoryBalanceResponse> getLowStock() {
        return balanceRepo.findLowStock(lowStockThreshold).stream()
                .map(this::toBalanceResponse).collect(Collectors.toList());
    }

    public List<InventorySummaryResponse> getSummary() {
        return balanceRepo.getSummaryGrid().stream()
                .map(row -> InventorySummaryResponse.builder()
                        .bloodGroup((BloodGroup) row[0])
                        .componentType((ComponentType) row[1])
                        .totalQuantity((Long) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    // ─── STOCK TRANSACTIONS ──────────────────────────────────────────

    @Transactional
    public StockTransactionResponse createTransaction(StockTransactionRequest request) {
        StockTransaction txn = StockTransaction.builder()
                .componentId(request.getComponentId())
                .txnType(request.getTxnType())
                .quantity(request.getQuantity())
                .txnDate(request.getTxnDate() != null ? request.getTxnDate() : LocalDate.now())
                .referenceId(request.getReferenceId())
                .notes(request.getNotes())
                .build();
        return toTxnResponse(txnRepo.save(txn));
    }

    public Page<StockTransactionResponse> getAllTransactions(Pageable pageable) {
        return txnRepo.findAllByOrderByCreatedAtDesc(pageable).map(this::toTxnResponse);
    }

    public StockTransactionResponse getTransactionById(Long txnId) {
        return toTxnResponse(txnRepo.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + txnId)));
    }

    public List<StockTransactionResponse> getTransactionsByComponent(Long componentId) {
        return txnRepo.findByComponentId(componentId).stream().map(this::toTxnResponse).collect(Collectors.toList());
    }

    public List<StockTransactionResponse> getTransactionsByType(TransactionType type) {
        return txnRepo.findByTxnType(type).stream().map(this::toTxnResponse).collect(Collectors.toList());
    }

    // ─── EXPIRY WATCH ────────────────────────────────────────────────

    public List<ExpiryWatchResponse> getAllExpiryWatch() {
        return expiryRepo.findAll().stream().map(this::toExpiryResponse).collect(Collectors.toList());
    }

    public List<ExpiryWatchResponse> getOpenExpiryWatch() {
        return expiryRepo.findByStatus(ExpiryWatchStatus.OPEN).stream()
                .map(this::toExpiryResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExpiryWatchResponse actionExpiryWatch(Long expiryId) {
        ExpiryWatch watch = expiryRepo.findById(expiryId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpiryWatch not found: " + expiryId));
        watch.setStatus(ExpiryWatchStatus.ACTIONED);
        return toExpiryResponse(expiryRepo.save(watch));
    }

    // ─── Internal helpers ────────────────────────────────────────────

    public void createExpiryWatchIfAbsent(Long componentId, LocalDate expiryDate, int daysToExpire) {
        if (!expiryRepo.existsByComponentId(componentId)) {
            expiryRepo.save(ExpiryWatch.builder()
                    .componentId(componentId)
                    .expiryDate(expiryDate)
                    .daysToExpire(daysToExpire)
                    .build());
            log.info("ExpiryWatch created for componentId={}, expiresIn={}days", componentId, daysToExpire);
        }
    }

    private void recordTransaction(Long componentId, TransactionType type, int qty, String ref, String notes) {
        txnRepo.save(StockTransaction.builder()
                .componentId(componentId)
                .txnType(type)
                .quantity(qty)
                .txnDate(LocalDate.now())
                .referenceId(ref)
                .notes(notes)
                .build());
    }

    private InventoryBalanceResponse toBalanceResponse(InventoryBalance b) {
        return InventoryBalanceResponse.builder()
                .balanceId(b.getBalanceId()).componentId(b.getComponentId())
                .bloodGroup(b.getBloodGroup()).rhFactor(b.getRhFactor())
                .componentType(b.getComponentType()).bagNumber(b.getBagNumber())
                .expiryDate(b.getExpiryDate()).quantity(b.getQuantity())
            .status(b.getStatus())
                .createdAt(b.getCreatedAt()).updatedAt(b.getUpdatedAt()).build();
    }

    private ExpiryWatchResponse toExpiryResponse(ExpiryWatch e) {
        return ExpiryWatchResponse.builder()
                .expiryId(e.getExpiryId()).componentId(e.getComponentId())
                .daysToExpire(e.getDaysToExpire()).expiryDate(e.getExpiryDate())
                .flagDate(e.getFlagDate()).status(e.getStatus()).build();
    }

    private StockTransactionResponse toTxnResponse(StockTransaction t) {
        return StockTransactionResponse.builder()
                .txnId(t.getTxnId()).componentId(t.getComponentId())
            .txnType(t.getTxnType())
                .quantity(t.getQuantity()).txnDate(t.getTxnDate())
                .referenceId(t.getReferenceId()).notes(t.getNotes())
                .performedBy(t.getPerformedBy()).createdAt(t.getCreatedAt()).build();
    }
}