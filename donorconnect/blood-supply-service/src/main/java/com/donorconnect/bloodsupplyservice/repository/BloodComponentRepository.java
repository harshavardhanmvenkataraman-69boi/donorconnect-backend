package com.donorconnect.bloodsupplyservice.repository;
import com.donorconnect.bloodsupplyservice.entity.BloodComponent;
import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
import com.donorconnect.bloodsupplyservice.enums.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodComponentRepository extends JpaRepository<BloodComponent, Long> {
    List<BloodComponent> findByDonationId(Long donationId);
    List<BloodComponent> findByComponentType(ComponentType componentType);
    List<BloodComponent> findByStatus(ComponentStatus status);
    List<BloodComponent> findByExpiryDateBefore(LocalDate date);
    List<BloodComponent> findByExpiryDateBetween(LocalDate from, LocalDate to);
}