package com.donorconnect.donorservice.repository;
import com.donorconnect.donorservice.entity.Donor;
import com.donorconnect.donorservice.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByStatus(DonorStatus status);
    List<Donor> findByBloodGroupAndRhFactor(BloodGroup bg, RhFactor rh);
}
