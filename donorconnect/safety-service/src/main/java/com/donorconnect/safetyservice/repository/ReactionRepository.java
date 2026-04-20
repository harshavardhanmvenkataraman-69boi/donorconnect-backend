package com.donorconnect.safetyservice.repository;



import com.donorconnect.safetyservice.entity.Reaction;
import com.donorconnect.safetyservice.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByPatientId(Long patientId);
    List<Reaction> findBySeverity(Severity severity);
}
