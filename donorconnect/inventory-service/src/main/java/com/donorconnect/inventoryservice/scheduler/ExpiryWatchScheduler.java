package com.donorconnect.inventoryservice.scheduler;

import com.donorconnect.inventoryservice.entity.InventoryBalance;
import com.donorconnect.inventoryservice.enums.InventoryStatus;
import com.donorconnect.inventoryservice.kafka.InventoryKafkaProducer;
import com.donorconnect.inventoryservice.repository.InventoryBalanceRepository;
import com.donorconnect.inventoryservice.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiryWatchScheduler {

    private final InventoryBalanceRepository balanceRepo;
    private final InventoryService inventoryService;
    private final InventoryKafkaProducer kafkaProducer;

    @Value("${app.inventory.expiry-warning-days:7}")
    private int expiryWarningDays;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    /**
     * Runs daily at 6 AM.
     * Scans all AVAILABLE components and creates ExpiryWatch alerts
     * for those expiring within the warning window.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void checkExpiryAlerts() {
        LocalDate warningCutoff = LocalDate.now().plusDays(expiryWarningDays);
        List<InventoryBalance> available = balanceRepo.findByStatus(InventoryStatus.AVAILABLE);

        int flagged = 0;
        for (InventoryBalance balance : available) {
            if (balance.getExpiryDate() != null && !balance.getExpiryDate().isAfter(warningCutoff)) {
                long daysLeft = LocalDate.now().until(balance.getExpiryDate()).getDays();
                inventoryService.createExpiryWatchIfAbsent(
                        balance.getComponentId(), balance.getExpiryDate(), (int) daysLeft);
                flagged++;
            }
        }
        log.info("ExpiryWatchScheduler: flagged {} components expiring within {} days", flagged, expiryWarningDays);
    }

    /**
     * Runs daily at 7 AM.
     * Publishes low-stock alerts for blood groups below threshold.
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void checkLowStock() {
        List<InventoryBalance> lowStock = balanceRepo.findLowStock(lowStockThreshold);
        if (!lowStock.isEmpty()) {
            log.info("LowStockScheduler: {} components below threshold={}", lowStock.size(), lowStockThreshold);
            kafkaProducer.publishLowStockAlert(lowStock);
        }
    }
}
