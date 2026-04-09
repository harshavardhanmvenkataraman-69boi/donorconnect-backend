package com.donorconnect.donorservice.repository;
import com.donorconnect.donorservice.entity.Deferral;
import com.donorconnect.donorservice.enums.DeferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeferralRepository extends JpaRepository<Deferral, Long> {
    List<Deferral> findByDonorId(Long donorId);
    List<Deferral> findByStatusAndEndDateBefore(DeferralStatus status, LocalDate date);
    List<Deferral> findByStatus(DeferralStatus status);
}
