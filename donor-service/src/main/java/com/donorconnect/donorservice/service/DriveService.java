package com.donorconnect.donorservice.service;

import com.donorconnect.donorservice.dto.request.DriveRequest;
import com.donorconnect.donorservice.entity.*;
import com.donorconnect.donorservice.enums.DriveStatus;
import com.donorconnect.donorservice.exception.*;
import com.donorconnect.donorservice.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final DonationAppointmentRepository appointmentRepository;

    public Drive create(DriveRequest req) {
        if(req.getScheduledDate().isBefore(LocalDate.now())){
            throw new InvalidOperationException("Create Drive","Scheduled date cannot be in the past");
        }
        Drive d = Drive.builder()
                .name(req.getName())
                .location(req.getLocation())
                .scheduledDate(req.getScheduledDate())
                .capacity(req.getCapacity())
                .organizer(req.getOrganizer())
                .status(DriveStatus.PLANNED)
                .build();
        return driveRepository.save(d);
    }

    public List<Drive> getAll() {
        return driveRepository.findAll();
    }

    public Drive getById(Long id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive", id));
    }

    public List<Drive> getUpcoming() {
        return driveRepository.findByScheduledDateAfter(LocalDate.now());
    }

    public Drive update(Long id, DriveRequest req) {
        Drive d = getById(id);

        if(d.getStatus()==DriveStatus.COMPLETED || d.getStatus()==DriveStatus.CANCELLED){
            throw new InvalidOperationException("Update Drive","cannot update a COMPLETED or CANCELLED drive");
        }
        if (req.getName() != null) d.setName(req.getName());
        if (req.getLocation() != null) d.setLocation(req.getLocation());
        if (req.getScheduledDate() != null) d.setScheduledDate(req.getScheduledDate());
        if (req.getCapacity() != null) d.setCapacity(req.getCapacity());
        if (req.getOrganizer() != null) d.setOrganizer(req.getOrganizer());
        return driveRepository.save(d);
    }

    public Drive updateStatus(Long id, DriveStatus newStatus) {
        Drive d = getById(id);
        DriveStatus current=d.getStatus();
        boolean validTransition=
                (current==DriveStatus.PLANNED && newStatus==DriveStatus.ACTIVE)||
                        (current==DriveStatus.PLANNED && newStatus==DriveStatus.CANCELLED)||
                        (current==DriveStatus.ACTIVE && newStatus==DriveStatus.COMPLETED)||
                        (current==DriveStatus.ACTIVE && newStatus==DriveStatus.CANCELLED);
        if(!validTransition){
            throw new InvalidOperationException("Update Drive status","Cannot transition drive from"+current+" to "+newStatus);
        }
        d.setStatus(newStatus);
        return driveRepository.save(d);
    }

    public List<DonationAppointment> getAppointmentsByDrive(Long driveId) {
        getById(driveId);
        return appointmentRepository.findByDriveId(driveId);
    }
}