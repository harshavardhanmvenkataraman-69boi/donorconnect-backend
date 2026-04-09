package com.donorconnect.bloodsupplyservice.scheduler;

import com.donorconnect.bloodsupplyservice.entity.Component;
import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
import com.donorconnect.bloodsupplyservice.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component @RequiredArgsConstructor @Slf4j
public class ComponentExpiryScheduler {
    private final ComponentRepository componentRepository;

    /**
     * Runs every hour.
     * Checks all AVAILABLE components and marks them EXPIRED if ExpiryDate has passed.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireComponents() {
        LocalDate today = LocalDate.now();
        List<com.donorconnect.bloodsupplyservice.entity.Component> toExpire =
                componentRepository.findByStatusAndExpiryDateBefore(ComponentStatus.AVAILABLE, today);
        log.info("ComponentExpiryScheduler: expiring {} components", toExpire.size());
        toExpire.forEach(c -> {
            c.setStatus(ComponentStatus.EXPIRED);
            componentRepository.save(c);
            log.info("Component {} marked EXPIRED (was due {})", c.getComponentId(), c.getExpiryDate());
        });
    }
}
