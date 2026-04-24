package com.donorconnect.bloodsupplyservice.repository;


import com.donorconnect.bloodsupplyservice.entity.DisposalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisposalRecordRepository extends JpaRepository<DisposalRecord, Long> {
    List<DisposalRecord> findByComponentId(Long componentId);
}
