package com.donorconnect.donorservice.scheduler;

import com.donorconnect.donorservice.entity.Deferral;
import com.donorconnect.donorservice.entity.Donor;
import com.donorconnect.donorservice.enums.DeferralStatus;
import com.donorconnect.donorservice.enums.DonorStatus;
import com.donorconnect.donorservice.repository.DeferralRepository;
import com.donorconnect.donorservice.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component @RequiredArgsConstructor @Slf4j
public class DeferralScheduler {
    private final DeferralRepository deferralRepository;
    private final DonorRepository donorRepository;

    /**
     * Runs daily at midnight.
     * Finds ACTIVE deferrals whose EndDate has passed and marks them EXPIRED.
     * Also restores the Donor status to ACTIVE if the deferral was TEMPORARY.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireCompletedDeferrals() {
        LocalDate today = LocalDate.now();
        List<Deferral> expiredDeferrals = deferralRepository.findByStatusAndEndDateBefore(DeferralStatus.ACTIVE, today);
        log.info("DeferralScheduler: found {} deferrals to expire", expiredDeferrals.size());

        for (Deferral deferral : expiredDeferrals) {
            deferral.setStatus(DeferralStatus.EXPIRED);
            deferralRepository.save(deferral);

            // Restore donor eligibility for temporary deferrals
            donorRepository.findById(deferral.getDonorId()).ifPresent(donor -> {
                if (donor.getStatus() == DonorStatus.DEFERRED) {
                    donor.setStatus(DonorStatus.ACTIVE);
                    donorRepository.save(donor);
                    log.info("Donor {} restored to ACTIVE after deferral expired", donor.getDonorId());
                }
            });
        }
    }
}
