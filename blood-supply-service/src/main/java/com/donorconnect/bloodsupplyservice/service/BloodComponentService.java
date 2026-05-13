package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.Exception.ResourceNotFoundException;
import com.donorconnect.bloodsupplyservice.dto.request.BloodComponentRequest;
import com.donorconnect.bloodsupplyservice.dto.request.InventoryEntryRequest;
import com.donorconnect.bloodsupplyservice.entity.*;
import com.donorconnect.bloodsupplyservice.enums.*;
import com.donorconnect.bloodsupplyservice.repository.*;
import com.donorconnect.bloodsupplyservice.feign.InventoryFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Blood component registration with full validation pipeline:
 *
 *   1. Donation must exist.
 *   2. All 7 mandatory tests must be entered AND none can be reactive.
 *      - Reactive donations cannot be registered as components at all.
 *      - Donations with missing tests cannot be registered yet.
 *      Both cases return HTTP 409 with a clear message.
 *   3. Same donation cannot have two components of the same type
 *      (one PRBC per donation, one Plasma per donation, etc. —
 *      multiple types from one donation IS allowed).
 *   4. Total volume across components for a donation cannot exceed
 *      the donation's collected volume.
 *   5. Bag number is auto-populated from the donation (user input ignored
 *      to avoid mismatches).
 *
 *   Reactive donations therefore never produce a component through this service.
 *   The quarantine + deferral side effects still fire when the reactive test
 *   itself is entered (see TestResultService.handleReactive).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BloodComponentService {

    private final BloodComponentRepository bloodComponentRepository;
    private final DonationRepository donationRepository;
    private final TestResultRepository testResultRepository;
    private final TestResultService testResultService;
    private final InventoryFeignClient inventoryFeignClient;

    /** Thin wrapper so create() reads cleanly. */
    public java.util.Map<String, Object> readinessFor(Long donationId) {
        return testResultService.getComponentReadiness(donationId);
    }

    @Transactional
    public BloodComponent create(BloodComponentRequest req) {
        // ---- 1. Donation must exist ----
        Donation donation = donationRepository.findById(req.getDonationId())
                .orElseThrow(() -> new IllegalStateException(
                        "Donation " + req.getDonationId() + " does not exist."));

        // ---- 1a. Tests must be COMPLETE and NON-REACTIVE before any component
        //          can be registered. This is the single gate that enforces:
        //          - reactive donations -> registration blocked entirely
        //          - donations missing any of the 7 mandatory tests -> blocked
        //          Both cases throw IllegalStateException which the global handler
        //          maps to HTTP 409 Conflict with a clean message.
        java.util.Map<String, Object> readiness = readinessFor(req.getDonationId());
        if (!Boolean.TRUE.equals(readiness.get("ready"))) {
            throw new IllegalStateException(String.valueOf(readiness.get("message")));
        }

        // ---- 2. No duplicate component-type for the same donation ----
        Optional<BloodComponent> existing = bloodComponentRepository
                .findByDonationIdAndComponentType(req.getDonationId(), req.getComponentType());
        if (existing.isPresent()) {
            throw new IllegalStateException(
                    "A " + req.getComponentType() + " component already exists for donation "
                            + req.getDonationId() + " (component ID " + existing.get().getComponentId() + ").");
        }

        // ---- 3. Volume math ----
        int requestedVolume = req.getVolume() != null ? req.getVolume() : 0;
        if (requestedVolume <= 0) {
            throw new IllegalStateException("Volume must be greater than 0.");
        }
        int donationVolume = donation.getVolumeMl() != null ? donation.getVolumeMl() : 450; // sensible default
        int usedVolume = bloodComponentRepository.findByDonationId(req.getDonationId()).stream()
                .filter(c -> c.getStatus() != ComponentStatus.DISPOSED)
                .mapToInt(c -> c.getVolume() != null ? c.getVolume() : 0)
                .sum();
        int remaining = donationVolume - usedVolume;
        if (requestedVolume > remaining) {
            throw new IllegalStateException(
                    "Volume " + requestedVolume + "ml exceeds remaining donation volume " + remaining + "ml "
                            + "(donation total: " + donationVolume + "ml, already used: " + usedVolume + "ml).");
        }

        // ---- 4. Build & save (reactive case is already blocked above, so
        //          we only reach here for cleared donations) ----
        BloodComponent c = BloodComponent.builder()
                .donationId(req.getDonationId())
                .componentType(req.getComponentType())
                .bagNumber(donation.getBagId())  // auto-populated from donation
                .volume(requestedVolume)
                .bloodGroup(req.getBloodGroup())
                .rhFactor(req.getRhFactor())
                .manufactureDate(req.getManufactureDate() != null ? req.getManufactureDate() : LocalDate.now())
                .expiryDate(req.getExpiryDate())
                .status(ComponentStatus.AVAILABLE)
                .build();

        BloodComponent saved = bloodComponentRepository.save(c);

        // ---- 5. Push to inventory ----
        try {
            inventoryFeignClient.createEntry(InventoryEntryRequest.builder()
                    .componentId(saved.getComponentId())
                    .bloodGroup(saved.getBloodGroup())
                    .rhFactor(saved.getRhFactor())
                    .componentType(saved.getComponentType().toString())
                    .expiryDate(saved.getExpiryDate())
                    .bagNumber(saved.getBagNumber())
                    .build());
            log.info("Inventory entry created for component ID={}", saved.getComponentId());
        } catch (Exception e) {
            log.warn("Failed to notify inventory-service for componentId={}: {}",
                    saved.getComponentId(), e.getMessage());
        }

        return saved;
    }

    public Page<BloodComponent> getAll(Pageable pageable) {
        return bloodComponentRepository.findAll(pageable);
    }

    public BloodComponent getById(Long id) {
        return bloodComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodComponent", id));
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

    /**
     * Helper for the frontend: get a donation's remaining allocatable volume.
     * Used by BloodComponentsPage to show "X ml remaining of 450 ml" in the
     * registration form.
     */
    public DonationVolumeInfo getVolumeInfo(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalStateException("Donation " + donationId + " does not exist."));
        int total = donation.getVolumeMl() != null ? donation.getVolumeMl() : 450;
        int used = bloodComponentRepository.findByDonationId(donationId).stream()
                .filter(c -> c.getStatus() != ComponentStatus.DISPOSED)
                .mapToInt(c -> c.getVolume() != null ? c.getVolume() : 0)
                .sum();
        List<String> existingTypes = bloodComponentRepository.findByDonationId(donationId).stream()
                .filter(c -> c.getStatus() != ComponentStatus.DISPOSED)
                .map(c -> c.getComponentType().name())
                .toList();
        boolean hasReactive = testResultRepository.findByDonationId(donationId).stream()
                .anyMatch(t -> t.getStatus() == TestStatus.REACTIVE);
        return DonationVolumeInfo.builder()
                .donationId(donationId)
                .totalVolumeMl(total)
                .usedVolumeMl(used)
                .remainingVolumeMl(total - used)
                .bagId(donation.getBagId())
                .existingComponentTypes(existingTypes)
                .hasReactiveTest(hasReactive)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DonationVolumeInfo {
        private Long donationId;
        private String bagId;
        private int totalVolumeMl;
        private int usedVolumeMl;
        private int remainingVolumeMl;
        private List<String> existingComponentTypes;
        private boolean hasReactiveTest;
    }
}
