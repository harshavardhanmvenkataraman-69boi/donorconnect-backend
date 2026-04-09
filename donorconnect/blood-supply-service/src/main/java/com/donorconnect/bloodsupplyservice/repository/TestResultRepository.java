package com.donorconnect.bloodsupplyservice.repository;
import com.donorconnect.bloodsupplyservice.entity.TestResult;
import com.donorconnect.bloodsupplyservice.enums.TestResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByDonationId(Long donationId);
    List<TestResult> findByStatus(TestResultStatus status);
}
