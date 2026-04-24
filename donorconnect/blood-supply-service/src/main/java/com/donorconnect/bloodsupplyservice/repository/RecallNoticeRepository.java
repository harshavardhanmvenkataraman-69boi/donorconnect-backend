package com.donorconnect.bloodsupplyservice.repository;
import com.donorconnect.bloodsupplyservice.entity.RecallNotice;
import com.donorconnect.bloodsupplyservice.enums.RecallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RecallNoticeRepository extends JpaRepository<RecallNotice, Long> {
    List<RecallNotice> findByDonationId(Long donationId);
    List<RecallNotice> findByStatus(RecallStatus status);
}
