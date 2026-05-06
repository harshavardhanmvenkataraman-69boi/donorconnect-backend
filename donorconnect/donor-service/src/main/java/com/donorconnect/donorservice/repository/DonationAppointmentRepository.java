package com.donorconnect.donorservice.repository;

import com.donorconnect.donorservice.entity.DonationAppointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationAppointmentRepository extends JpaRepository<DonationAppointment, Long> {
    List<DonationAppointment> findByDonorId(Long donorId);
    List<DonationAppointment> findByDriveId(Long driveId);
    List<DonationAppointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    boolean existsByDonorIdAndDateTimeBetween(Long donorId,LocalDateTime start,LocalDateTime end);
}