package com.donorconnect.donorservice.repository;

import com.donorconnect.donorservice.entity.ScreeningRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ScreeningRecordRepository extends JpaRepository<ScreeningRecord, Long> {
    List<ScreeningRecord> findByDonorId(Long donorId);
    Page<ScreeningRecord> findAll(Pageable pageable);
    void deleteByDonorId(Long donorId);
    boolean existsByDonorIdAndClearedFlagTrue(Long donorId);
}