package com.donorconnect.donorservice.service;

import com.donorconnect.donorservice.dto.request.DonorRequest;
import com.donorconnect.donorservice.entity.*;
import com.donorconnect.donorservice.enums.*;
import com.donorconnect.donorservice.exception.ResourceNotFoundException;
import com.donorconnect.donorservice.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {

    private final DonorRepository donorRepository;
//    private final DonationRepository donationRepository;

    public Donor create(DonorRequest req) {
        log.info("Creating donor record for name={} bloodGroup={}", req.getName(), req.getBloodGroup());
        Donor donor = Donor.builder()
                .name(req.getName()).dob(req.getDob()).gender(req.getGender())
                .bloodGroup(req.getBloodGroup()).rhFactor(req.getRhFactor())
                .contactInfo(req.getContactInfo()).addressJson(req.getAddressJson())
                .donorType(req.getDonorType()).status(DonorStatus.ACTIVE).build();
        Donor savedDonor = donorRepository.save(donor);
        log.info("Donor created with donorId={}", savedDonor.getDonorId());
        return savedDonor;
    }

    public Page<Donor> getAll(Pageable pageable) { return donorRepository.findAll(pageable); }

    public Donor getById(Long donorId) {
        return donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", donorId));
    }

    public List<Donor> search(String name, String bloodGroup) {
        return donorRepository.searchDonors(name, bloodGroup);
    }

    public Donor update(Long donorId, DonorRequest req) {
        log.info("Updating donor record donorId={}", donorId);
        Donor donor = getById(donorId);
        if (req.getName() != null) donor.setName(req.getName());
        if (req.getDob() != null) donor.setDob(req.getDob());
        if (req.getGender() != null) donor.setGender(req.getGender());
        if (req.getBloodGroup() != null) donor.setBloodGroup(req.getBloodGroup());
        if (req.getRhFactor() != null) donor.setRhFactor(req.getRhFactor());
        if (req.getContactInfo() != null) donor.setContactInfo(req.getContactInfo());
        if (req.getAddressJson() != null) donor.setAddressJson(req.getAddressJson());
        if (req.getDonorType() != null) donor.setDonorType(req.getDonorType());
        Donor updatedDonor = donorRepository.save(donor);
        log.debug("Donor update completed donorId={} status={}", updatedDonor.getDonorId(), updatedDonor.getStatus());
        return updatedDonor;
    }

    // Internal use: called by DeferralService and DeferralExpiryScheduler only.
    // Use this instead of exposing raw status changes via API where possible.
    public Donor updateStatus(Long donorId, DonorStatus status) {
        log.info("Updating donor status donorId={} newStatus={}", donorId, status);
        Donor donor = getById(donorId);
        donor.setStatus(status);
        return donorRepository.save(donor);
    }

//    public List<Donation> getDonationHistory(Long donorId) { return donationRepository.findByDonorId(donorId); }
    public List<Donor> getByBloodGroup(String bg) { return donorRepository.findByBloodGroup(bg); }
    public List<Donor> getByType(DonorType type) { return donorRepository.findByDonorType(type); }
}