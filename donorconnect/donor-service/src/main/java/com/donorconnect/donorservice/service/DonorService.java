package com.donorconnect.donorservice.service;

import com.donorconnect.donorservice.entity.*;
import com.donorconnect.donorservice.enums.*;
import com.donorconnect.donorservice.kafka.*;
import com.donorconnect.donorservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
public class DonorService {
    private final DonorRepository donorRepository;
    private final DeferralRepository deferralRepository;
    private final ScreeningRecordRepository screeningRepository;
    private final DonorKafkaProducer kafkaProducer;

    public List<Donor> getAllDonors() { return donorRepository.findAll(); }
    public Donor getDonorById(Long id) { return donorRepository.findById(id).orElseThrow(); }

    @Transactional
    public Donor registerDonor(Donor donor) { return donorRepository.save(donor); }

    @Transactional
    public Deferral deferDonor(Long donorId, Deferral deferral) {
        Donor donor = donorRepository.findById(donorId).orElseThrow();
        donor.setStatus(DonorStatus.DEFERRED);
        donorRepository.save(donor);
        deferral.setDonorId(donorId);
        deferral.setStatus(DeferralStatus.ACTIVE);
        return deferralRepository.save(deferral);
    }

    @Transactional
    public void flagDonorReactive(Long donorId, String reason) {
        Donor donor = donorRepository.findById(donorId).orElseThrow();
        donor.setStatus(DonorStatus.INELIGIBLE);
        donorRepository.save(donor);
        kafkaProducer.publishDonorFlagged(DonorFlaggedEvent.builder()
                .donorId(donorId).reason(reason)
                .timestamp(LocalDateTime.now().toString()).build());
        log.info("Donor {} flagged as reactive/ineligible", donorId);
    }

    public ScreeningRecord saveScreening(ScreeningRecord record) { return screeningRepository.save(record); }
    public List<ScreeningRecord> getScreeningsByDonor(Long donorId) { return screeningRepository.findByDonorId(donorId); }
    public List<Deferral> getDeferralsByDonor(Long donorId) { return deferralRepository.findByDonorId(donorId); }
}
