package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.entity.*;
import com.donorconnect.bloodsupplyservice.enums.*;
import com.donorconnect.bloodsupplyservice.kafka.*;
import com.donorconnect.bloodsupplyservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
public class BloodSupplyService {
    private final DonationRepository donationRepository;
    private final ComponentRepository componentRepository;
    private final TestResultRepository testResultRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final BloodSupplyKafkaProducer kafkaProducer;

    public Donation saveDonation(Donation donation) { return donationRepository.save(donation); }
    public Component saveComponent(Component component) { return componentRepository.save(component); }
    public Component getComponentById(Long id) { return componentRepository.findById(id).orElseThrow(); }
    public List<Component> getAvailableComponents() { return componentRepository.findByStatus(ComponentStatus.AVAILABLE); }

    @Transactional
    public TestResult recordTestResult(TestResult testResult) {
        TestResult saved = testResultRepository.save(testResult);
        if (saved.getStatus() == TestResultStatus.REACTIVE) {
            Donation donation = donationRepository.findById(saved.getDonationId()).orElseThrow();
            kafkaProducer.publishTestReactive(TestResultReactiveEvent.builder()
                    .donationId(saved.getDonationId())
                    .donorId(donation.getDonorId())
                    .testType(saved.getTestType().name())
                    .timestamp(LocalDateTime.now().toString())
                    .build());
            log.info("Reactive test result for donationId={}, triggering quarantine", saved.getDonationId());
        }
        return saved;
    }

    public List<InventoryBalance> getAllInventory() { return inventoryBalanceRepository.findAll(); }

    @Transactional
    public InventoryBalance updateInventory(InventoryBalance balance) { return inventoryBalanceRepository.save(balance); }
}
