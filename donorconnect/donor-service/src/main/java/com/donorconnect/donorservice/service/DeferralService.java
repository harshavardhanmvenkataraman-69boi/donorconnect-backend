package com.donorconnect.donorservice.service;

import com.donorconnect.donorservice.dto.request.DeferralRequest;
import com.donorconnect.donorservice.entity.Deferral;
import com.donorconnect.donorservice.enums.*;
import com.donorconnect.donorservice.exception.*;
import com.donorconnect.donorservice.repository.DeferralRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeferralService {

    private final DeferralRepository deferralRepo;
    private final DonorService donorService;

    /**
     * Creates a deferral and immediately syncs Donor.status to DEFERRED.
     * For TEMPORARY deferrals, endDate is mandatory.
     */

    @Transactional
    public Deferral create(DeferralRequest req) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = (req.getDeferralType() == DeferralType.TEMPORARY)
                ? startDate.plusMonths(3)
                : null;

        Deferral d = Deferral.builder().donorId(req.getDonorId()).deferralType(req.getDeferralType())
                .reason(req.getReason()).startDate(startDate).endDate(endDate)
                .status(DeferralStatus.ACTIVE).build();
        Deferral saved = deferralRepo.save(d);

        // Sync donor status to DEFERRED immediately
        donorService.updateStatus(req.getDonorId(), DonorStatus.DEFERRED);

        return saved;
    }

    public Deferral getById(Long id) {
        expireDueTemporaryDeferralsIfAny();
        return deferralRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Deferral", id));
    }

    public List<Deferral> getByDonor(Long donorId) {
        expireDueTemporaryDeferralsIfAny();
        return deferralRepo.findByDonorId(donorId);
    }

    public Deferral update(Long id, DeferralRequest req) {
        expireDueTemporaryDeferralsIfAny();
        Deferral d = getById(id);
        if (req.getReason() != null) d.setReason(req.getReason());
        if (req.getDeferralType() != null) {
            DeferralType newType = req.getDeferralType();
            if (newType == DeferralType.TEMPORARY && d.getEndDate() == null) {
                d.setEndDate(LocalDate.now().plusMonths(3));
            }
            d.setDeferralType(newType);
        }
        return deferralRepo.save(d);
    }

    /**
     * Manually lifts a deferral (only for TEMPORARY type).
     * Also restores Donor.status to ACTIVE — but only if the donor
     * has no other ACTIVE deferrals remaining.
     */

    @Transactional
    public Deferral lift(Long id) {
        expireDueTemporaryDeferralsIfAny();
        Deferral d = getById(id);
        if (d.getDeferralType() == DeferralType.PERMANENT)
            throw new PermanentDeferralException(id);
        d.setStatus(DeferralStatus.LIFTED);
        Deferral saved = deferralRepo.save(d);

        // Only restore donor to ACTIVE if no other active deferrals exist
        restoreDonorIfEligible(d.getDonorId());

        return saved;
    }

    /**
     * Called by the scheduler to expire a single temporary deferral whose endDate has passed.
     * Also restores Donor.status to ACTIVE if no other active deferrals remain.
     */
    @Transactional
    public void expireDeferral(Deferral d) {
        if (d.getStatus() != DeferralStatus.ACTIVE ||
                d.getDeferralType() != DeferralType.TEMPORARY ||
                d.getEndDate() == null ||
                d.getEndDate().isAfter(LocalDate.now())) {
            return;
        }
        d.setStatus(DeferralStatus.EXPIRED);
        deferralRepo.save(d);
        restoreDonorIfEligible(d.getDonorId());
    }

    public List<Deferral> getActive() {
        expireDueTemporaryDeferralsIfAny();
        return deferralRepo.findByStatus(DeferralStatus.ACTIVE);
    }

    public List<Deferral> getExpiredDeferrals() { return deferralRepo.findByStatus(DeferralStatus.EXPIRED); }
    /**
     * Returns all ACTIVE temporary deferrals whose endDate is before or equal to today.
     * Used by the scheduler.
     */
    public List<Deferral> getExpiredTemporaryDeferrals() {
        return deferralRepo.findByStatusAndDeferralTypeAndEndDateLessThanEqual(
                DeferralStatus.ACTIVE, DeferralType.TEMPORARY, LocalDate.now());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void restoreDonorIfEligible(Long donorId) {
        boolean hasOtherActiveDeferrals = deferralRepo
                .existsByDonorIdAndStatus(donorId, DeferralStatus.ACTIVE);
        if (!hasOtherActiveDeferrals) {
            donorService.updateStatus(donorId, DonorStatus.ACTIVE);
        }
    }

    private void expireDueTemporaryDeferralsIfAny() {
        List<Deferral> due = getExpiredTemporaryDeferrals();
        for (Deferral d : due) {
            expireDeferral(d);
        }
    }
}