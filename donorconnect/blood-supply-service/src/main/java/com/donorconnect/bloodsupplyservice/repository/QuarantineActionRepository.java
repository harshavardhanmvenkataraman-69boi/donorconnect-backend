package com.donorconnect.bloodsupplyservice.repository;

import com.donorconnect.bloodsupplyservice.entity.QuarantineAction;
import com.donorconnect.bloodsupplyservice.enums.QuarantineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuarantineActionRepository extends JpaRepository<QuarantineAction, Long> {
    List<QuarantineAction> findByComponentId(Long componentId);
    List<QuarantineAction> findByStatus(QuarantineStatus status);
}
