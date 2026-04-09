package com.donorconnect.reportingservice.scheduler;

import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.ReportScope;
import com.donorconnect.reportingservice.repository.LabReportPackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class ReportingScheduler {
    private final LabReportPackRepository reportRepo;

    /** Generates a daily snapshot report at 1AM */
    @Scheduled(cron = "0 0 1 * * *")
    public void generateDailySnapshot() {
        log.info("Generating daily snapshot report for {}", LocalDate.now());
        String metrics = """
                {"reportDate":"%s","type":"DAILY_SNAPSHOT","note":"Auto-generated daily inventory snapshot"}
                """.formatted(LocalDate.now());
        reportRepo.save(LabReportPack.builder()
                .scope(ReportScope.GLOBAL)
                .metricsJson(metrics.trim())
                .build());
    }
}
