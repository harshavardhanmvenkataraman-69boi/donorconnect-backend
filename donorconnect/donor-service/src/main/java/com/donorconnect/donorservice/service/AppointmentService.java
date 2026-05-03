package com.donorconnect.donorservice.service;

import com.donorconnect.donorservice.dto.request.AppointmentRequest;
import com.donorconnect.donorservice.entity.*;
import com.donorconnect.donorservice.enums.*;
import com.donorconnect.donorservice.exception.*;
import com.donorconnect.donorservice.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final DonationAppointmentRepository appointmentRepository;
    private final DonorRepository donorRepository;
    private final DeferralRepository deferralRepository;
    private final DriveRepository driveRepository;
    private final ScreeningRecordRepository screeningRecordRepository;

    @SuppressWarnings("null")
    public DonationAppointment book(AppointmentRequest req) {

        Donor donor=donorRepository.findById(req.getDonorId())
                .orElseThrow(()-> new ResourceNotFoundException("Donor",req.getDonorId()) );
        if(donor.getStatus()!= DonorStatus.ACTIVE){
            throw new InvalidOperationException("Donor is not active.","Current status: "+donor.getStatus());
        }

        boolean isDeferred=deferralRepository.existsByDonorIdAndStatus(req.getDonorId(), DeferralStatus.ACTIVE);
        if(isDeferred){
            throw new InvalidOperationException("Donor with id "+req.getDonorId(),"is currently deferred and cannot book an appointment");
        }
        boolean hasCleared=screeningRecordRepository.existsByDonorIdAndClearedFlagTrue(req.getDonorId());
        if(!hasCleared){
            throw new InvalidOperationException("Donor with Id"+req.getDonorId(),"is currently deferred and cannot book an appointment");

        }

        if(req.getDateTime().isBefore(LocalDateTime.now())){
            throw new InvalidOperationException("Book Appointment","Appointment date cannot be in past");
        }

        LocalDateTime startOfDay=req.getDateTime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay=startOfDay.plusDays(1);
        boolean alreadyBooked= appointmentRepository.existsByDonorIdAndDateTimeBetween(req.getDonorId(),startOfDay,endOfDay);
        
        if(alreadyBooked){
            throw new InvalidOperationException("Book Appointment","Already booked appointment on the same day.");
        }

        if(req.getDriveId()!=null){
            Drive drive=driveRepository.findById(req.getDriveId())
                    .orElseThrow(()-> new ResourceNotFoundException("Drive",req.getDriveId()) );
        }

        DonationAppointment a = DonationAppointment.builder()
                .donorId(req.getDonorId())
                .dateTime(req.getDateTime())
                .centerId(req.getCenterId())
                .driveId(req.getDriveId())
                .status(AppointmentStatus.BOOKED)
                .build();
        return appointmentRepository.save(a);
    }

    public Page<DonationAppointment> getAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    public DonationAppointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    public List<DonationAppointment> getByDonor(Long donorId) {
        return appointmentRepository.findByDonorId(donorId);
    }

    public List<DonationAppointment> getToday() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return appointmentRepository.findByDateTimeBetween(start, end);
    }

    public DonationAppointment update(Long id, AppointmentRequest req) {
        DonationAppointment a = getById(id);
        if (req.getDateTime() != null) a.setDateTime(req.getDateTime());
        if (req.getCenterId() != null) a.setCenterId(req.getCenterId());
        return appointmentRepository.save(a);
    }

    public DonationAppointment updateStatus(Long id, AppointmentStatus status) {
        DonationAppointment a = getById(id);
        AppointmentStatus current=a.getStatus();
        if(current==AppointmentStatus.COMPLETED || current==AppointmentStatus.CANCELLED){
            throw new InvalidOperationException("Update Status","cannot change status of already completed or cancelled appointment");
        }
        if(status==AppointmentStatus.NO_SHOW && current!=AppointmentStatus.BOOKED){
            throw new InvalidOperationException("Update Status","can only mark No_show from Booked Status");
        }
        if(status==AppointmentStatus.COMPLETED && current!=AppointmentStatus.CHECKED_IN){
            throw new InvalidOperationException("Update Status","can only mark COMPLETED from CHECKED_IN Status");
        }
        a.setStatus(status);
        return appointmentRepository.save(a);
    }
}