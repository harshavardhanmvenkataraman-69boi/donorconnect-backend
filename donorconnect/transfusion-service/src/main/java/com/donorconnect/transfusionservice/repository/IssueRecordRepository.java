package com.donorconnect.transfusionservice.repository;
import com.donorconnect.transfusionservice.entity.IssueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findByPatientId(Long patientId);
    List<IssueRecord> findByComponentId(Long componentId);
}
