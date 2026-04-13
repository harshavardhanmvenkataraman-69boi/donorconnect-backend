package com.donorconnect.donorservice.scheduler;

import com.donorconnect.donorservice.entity.Deferral;

import com.donorconnect.donorservice.service.DeferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeferralExpiryScheduler {
    private final DeferralService deferralService;

    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    public void expireTemporaryDeferrals() {
        List<Deferral> expired = deferralService.getExpiredTemporaryDeferrals();
        if (expired.isEmpty()) {
            log.info("[DeferralExpiryScheduler] No deferrals to expire today.");
            return;
        }
        log.info("[DeferralExpiryScheduler] Expiring {} deferral(s).", expired.size());
        for (Deferral d : expired) {
            try {
                deferralService.expireDeferral(d);
                log.info("[DeferralExpiryScheduler] Expired deferralId={} for donorId={}",
                        d.getDeferralId(), d.getDonorId());
            } catch (Exception e) {
                log.error("[DeferralExpiryScheduler] Failed to expire deferralId={}: {}",
                        d.getDeferralId(), e.getMessage());
            }
        }
    }
}
