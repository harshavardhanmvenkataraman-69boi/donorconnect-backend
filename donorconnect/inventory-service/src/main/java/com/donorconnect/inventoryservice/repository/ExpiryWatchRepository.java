package com.donorconnect.inventoryservice.repository;

import com.donorconnect.inventoryservice.entity.ExpiryWatch;
import com.donorconnect.inventoryservice.enums.ExpiryWatchStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpiryWatchRepository extends JpaRepository<ExpiryWatch, Long> {

    List<ExpiryWatch> findByStatus(ExpiryWatchStatus status);

    List<ExpiryWatch> findByComponentId(Long componentId);

    /** Find components expiring within N days that haven't been flagged yet */
    @Query("SELECT e FROM ExpiryWatch e WHERE e.expiryDate <= :cutoff AND e.status = 'OPEN'")
    List<ExpiryWatch> findOpenExpiringSoon(@Param("cutoff") LocalDate cutoff);

    boolean existsByComponentId(Long componentId);
}