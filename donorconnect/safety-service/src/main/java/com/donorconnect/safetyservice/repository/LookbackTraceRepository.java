package com.donorconnect.safetyservice.repository;

import com.donorconnect.safetyservice.entity.LookbackTrace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LookbackTraceRepository extends JpaRepository<LookbackTrace, Long> {
    List<LookbackTrace> findByDonationId(Long donationId);
    List<LookbackTrace> findByPatientId(Long patientId);
    List<LookbackTrace> findByComponentId(Long componentId);
}