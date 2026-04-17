package com.donorconnect.bloodsupplyservice.repository;

import com.donorconnect.bloodsupplyservice.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorId(Long donorId);
    Optional<Donation> findByBagId(String bagId);
}
