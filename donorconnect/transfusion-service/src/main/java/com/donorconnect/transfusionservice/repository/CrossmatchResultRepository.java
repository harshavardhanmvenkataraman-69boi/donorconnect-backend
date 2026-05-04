package com.donorconnect.transfusionservice.repository;

import com.donorconnect.transfusionservice.entity.CrossmatchResult;

import com.donorconnect.transfusionservice.enums.Compatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrossmatchResultRepository extends JpaRepository<CrossmatchResult, Long> {
    List<CrossmatchResult> findByRequestId(Long requestId);

    long countByRequestIdAndCompatibility(Long requestId, Compatibility compatibility);
}