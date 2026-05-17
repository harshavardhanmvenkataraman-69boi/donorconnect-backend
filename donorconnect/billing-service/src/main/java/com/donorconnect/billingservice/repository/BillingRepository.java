package com.donorconnect.billingservice.repository;

import com.donorconnect.billingservice.enums.BillingStatus;
import com.donorconnect.billingservice.model.BillingRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<BillingRef, Integer> {

    Optional<BillingRef> findByIssueId(Integer issueId);

    Page<BillingRef> findByStatus(BillingStatus status, Pageable pageable);

    long countByStatus(BillingStatus status);

    @Query("SELECT b FROM BillingRef b WHERE b.billingDate BETWEEN :from AND :to ORDER BY b.billingDate ASC, b.billingId ASC")
    List<BillingRef> findByBillingDateBetween(@Param("from") LocalDate from,
                                              @Param("to") LocalDate to);

    @Query("""
        SELECT b FROM BillingRef b
         WHERE b.billingDate BETWEEN :from AND :to
           AND (:status IS NULL OR b.status = :status)
         ORDER BY b.billingDate ASC, b.billingId ASC
    """)
    List<BillingRef> findForExport(@Param("from") LocalDate from,
                                   @Param("to") LocalDate to,
                                   @Param("status") BillingStatus status);
}
