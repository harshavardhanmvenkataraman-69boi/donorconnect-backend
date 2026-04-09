package com.donorconnect.bloodsupplyservice.repository;
import com.donorconnect.bloodsupplyservice.entity.Component;
import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    List<Component> findByStatus(ComponentStatus status);
    List<Component> findByStatusAndExpiryDateBefore(ComponentStatus status, LocalDate date);
    List<Component> findByDonationId(Long donationId);
}
