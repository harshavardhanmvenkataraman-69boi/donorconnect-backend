package com.donorconnect.billingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<com.donorconnect.billingservice.model.BillingRef, Integer> {

    Optional<com.donorconnect.billingservice.model.BillingRef> findByIssueId(Integer issueId);

    @Query("SELECT b FROM BillingRef b WHERE b.billingDate BETWEEN :from AND :to")
    List<com.donorconnect.billingservice.model.BillingRef> findByBillingDateBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
