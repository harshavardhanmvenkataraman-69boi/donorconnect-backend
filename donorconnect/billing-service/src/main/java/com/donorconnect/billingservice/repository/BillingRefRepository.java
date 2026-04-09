package com.donorconnect.billingservice.repository;
import com.donorconnect.billingservice.entity.BillingRef;
import com.donorconnect.billingservice.enums.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillingRefRepository extends JpaRepository<BillingRef, Long> {
    List<BillingRef> findByIssueId(Long issueId);
    List<BillingRef> findByStatus(BillingStatus status);
}
