package com.donorconnect.donorservice.repository;

import com.donorconnect.donorservice.entity.Drive;
import com.donorconnect.donorservice.enums.DriveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
    List<Drive> findByScheduledDateAfter(LocalDate date);
    List<Drive> findByStatus(DriveStatus status);
}