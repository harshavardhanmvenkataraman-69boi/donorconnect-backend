//package com.donorconnect.bloodsupplyservice.scheduler;
//
//import com.donorconnect.bloodsupplyservice.entity.BloodComponent;
//import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
//import com.donorconnect.bloodsupplyservice.repository.BloodComponentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@BloodComponent
//@RequiredArgsConstructor @Slf4j
//public class ComponentExpiryScheduler {
//    private final BloodComponentRepository componentRepository;
//
//    /**
//     * Runs every hour.
//     * Checks all AVAILABLE components and marks them EXPIRED if ExpiryDate has passed.
//     */
//    @Scheduled(cron = "0 0 * * * *")
//    @Transactional
//    public void expireComponents() {
//        LocalDate today = LocalDate.now();
//        List<BloodComponent> toExpire =
//                componentRepository.findByStatusAndExpiryDateBefore(ComponentStatus.AVAILABLE, today);
//        log.info("ComponentExpiryScheduler: expiring {} components", toExpire.size());
//        toExpire.forEach(c -> {
//            c.setStatus(ComponentStatus.EXPIRED);
//            componentRepository.save(c);
//            log.info("Component {} marked EXPIRED (was due {})", c.getComponentId(), c.getExpiryDate());
//        });
//    }
//}
