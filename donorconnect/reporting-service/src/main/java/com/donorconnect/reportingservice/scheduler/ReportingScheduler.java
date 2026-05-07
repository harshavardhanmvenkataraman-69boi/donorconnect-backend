package com.donorconnect.reportingservice.scheduler;

import com.donorconnect.reportingservice.enums.ReportScope;
import com.donorconnect.reportingservice.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component @RequiredArgsConstructor @Slf4j
public class ReportingScheduler {
    private final ReportingService reportingService;

    /** Generates a daily snapshot report at 1AM pulling live data from other services */
    @Scheduled(cron = "0 0 1 * * *")
    public void generateDailySnapshot() {
        log.info("Generating daily snapshot report for {}", LocalDate.now());
        try {
            reportingService.generateReport(ReportScope.GLOBAL);
            log.info("Daily snapshot report saved successfully");
        } catch (Exception e) {
            log.error("Failed to generate daily snapshot report: {}", e.getMessage());
        }
    }
}
