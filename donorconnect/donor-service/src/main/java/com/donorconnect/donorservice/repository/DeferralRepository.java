package com.donorconnect.donorservice.repository;
import com.donorconnect.donorservice.entity.Deferral;
import com.donorconnect.donorservice.enums.DeferralStatus;
import com.donorconnect.donorservice.enums.DeferralType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeferralRepository extends JpaRepository<Deferral, Long> {
    List<Deferral> findByDonorId(Long donorId);
    List<Deferral> findByStatus(DeferralStatus status);

    // Used  by the scheduler to find expired temporary deferrals
    List<Deferral> findByStatusAndDeferralTypeAndEndDateLessThanEqual(
            DeferralStatus status, DeferralType deferralType, LocalDate date);

    // Used to check if a donor still has any active deferral before restoring ACTIVE status
    boolean existsByDonorIdAndStatus(Long donorId, DeferralStatus status);
}

