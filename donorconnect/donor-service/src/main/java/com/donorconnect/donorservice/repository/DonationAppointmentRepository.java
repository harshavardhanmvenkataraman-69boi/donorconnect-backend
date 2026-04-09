package com.donorconnect.donorservice.repository;
import com.donorconnect.donorservice.entity.DonationAppointment;
import com.donorconnect.donorservice.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonationAppointmentRepository extends JpaRepository<DonationAppointment, Long> {
    List<DonationAppointment> findByDonorId(Long donorId);
    List<DonationAppointment> findByStatus(AppointmentStatus status);
}
