package com.donorconnect.transfusionservice.repository;
import com.donorconnect.transfusionservice.entity.CrossmatchRequest;
import com.donorconnect.transfusionservice.enums.CrossmatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrossmatchRequestRepository extends JpaRepository<CrossmatchRequest, Long> {
    List<CrossmatchRequest> findByPatientId(Long patientId);
    List<CrossmatchRequest> findByStatus(CrossmatchStatus status);
}
