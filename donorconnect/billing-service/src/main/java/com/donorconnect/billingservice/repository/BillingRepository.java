package com.donorconnect.billingservice.repository;

import com.donorconnect.billingservice.model.BillingRef;
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

    @Query("SELECT b FROM BillingRef b WHERE b.billingDate BETWEEN :from AND :to")
    List<BillingRef> findByBillingDateBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
