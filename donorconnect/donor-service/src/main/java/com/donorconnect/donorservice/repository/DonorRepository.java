package com.donorconnect.donorservice.repository;

import com.donorconnect.donorservice.entity.Donor;
import com.donorconnect.donorservice.enums.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroup(String bloodGroup);
    List<Donor> findByDonorType(DonorType donorType);
    @Query("SELECT d FROM Donor d WHERE (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%',:name,'%'))) AND (:bloodGroup IS NULL OR d.bloodGroup = :bloodGroup)")
    List<Donor> searchDonors(@Param("name") String name,
                             @Param("bloodGroup") String bloodGroup);
}