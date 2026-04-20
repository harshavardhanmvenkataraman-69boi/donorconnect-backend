package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.Exception.ResourceNotFoundException;
import com.donorconnect.bloodsupplyservice.dto.request.BloodComponentRequest;
import com.donorconnect.bloodsupplyservice.entity.*;
import com.donorconnect.bloodsupplyservice.enums.*;
import com.donorconnect.bloodsupplyservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.donorconnect.bloodsupplyservice.enums.ComponentType;


import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BloodComponentService {

    private final BloodComponentRepository bloodComponentRepository;

    public BloodComponent create(BloodComponentRequest req) {
        BloodComponent c = BloodComponent.builder()
                .donationId(req.getDonationId())
                .componentType(req.getComponentType())
                .bagNumber(req.getBagNumber())
                .volume(req.getVolume())
                .manufactureDate(req.getManufactureDate() != null ? req.getManufactureDate() : LocalDate.now())
                .expiryDate(req.getExpiryDate())
                .status(ComponentStatus.AVAILABLE)
                .build();
        return bloodComponentRepository.save(c);
    }

    public Page<BloodComponent> getAll(Pageable pageable) {
        return bloodComponentRepository.findAll(pageable);
    }

    public BloodComponent getById(Long id) {
        return bloodComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", id));
    }

    public List<BloodComponent> getAvailable() {
        return bloodComponentRepository.findByStatus(ComponentStatus.AVAILABLE);
    }


    public List<BloodComponent> getByDonation(Long donationId) {
        return bloodComponentRepository.findByDonationId(donationId);
    }

    public List<BloodComponent> getByType(ComponentType type) {
        return bloodComponentRepository.findByComponentType(type);
    }

    public List<BloodComponent> getByStatus(ComponentStatus status) {
        return bloodComponentRepository.findByStatus(status);
    }

    public BloodComponent updateStatus(Long id, ComponentStatus status) {
        BloodComponent c = getById(id);
        c.setStatus(status);
        return bloodComponentRepository.save(c);
    }

    public List<BloodComponent> getExpiring(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return bloodComponentRepository.findByExpiryDateBetween(LocalDate.now(), threshold);
    }
}