package com.donorconnect.bloodsupplyservice.service;


import com.donorconnect.bloodsupplyservice.dto.request.DonationRequest;
import com.donorconnect.bloodsupplyservice.entity.Donation;
import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import com.donorconnect.bloodsupplyservice.feign.DonorFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.donorconnect.bloodsupplyservice.repository.DonationRepository;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import com.donorconnect.bloodsupplyservice.Exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonorFeignClient donorFeignClient;

    public Donation create(DonationRequest req) {
        // Validate that the donor exists in donor-service
        try {
            donorFeignClient.getDonorById(req.getDonorId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Donor", "ID", req.getDonorId());
        } catch (FeignException.Forbidden e) {
            throw new RuntimeException("Access denied: insufficient permissions to access donor service");
        } catch (FeignException.Unauthorized e) {
            throw new RuntimeException("Authentication failed: invalid or missing JWT token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate donor: " + e.getMessage());
        }

        Donation d = Donation.builder()
                .donorId(req.getDonorId())
                .collectionDate(req.getCollectionDate() != null ? req.getCollectionDate() : LocalDate.now())
                .bagId(req.getBagId())
                .volumeMl(req.getVolumeMl())
                .collectedBy(req.getCollectedBy())
                .collectionStatus(CollectionStatus.COLLECTED)
                .build();
        return donationRepository.save(d);
    }

    public Page<Donation> getAll(Pageable pageable) {
        return donationRepository.findAll(pageable);
    }

    public Donation getById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", String.valueOf(id)));
    }

    public List<Donation> getByDonor(Long donorId) {
        // Validate that the donor exists in donor-service
        try {
            donorFeignClient.getDonorById(donorId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Donor", "ID", donorId);
        } catch (FeignException.Forbidden e) {
            throw new RuntimeException("Access denied: insufficient permissions to access donor service");
        } catch (FeignException.Unauthorized e) {
            throw new RuntimeException("Authentication failed: invalid or missing JWT token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate donor: " + e.getMessage());
        }

        return donationRepository.findByDonorId(donorId);
    }

    public Donation update(Long id, DonationRequest req) {
        Donation d = getById(id);
        if (req.getVolumeMl() != null) d.setVolumeMl(req.getVolumeMl());
        if (req.getCollectedBy() != null) d.setCollectedBy(req.getCollectedBy());
        return donationRepository.save(d);
    }

    public Donation updateStatus(Long id, CollectionStatus status) {
        Donation d = getById(id);
        d.setCollectionStatus(status);
        return donationRepository.save(d);
    }

    public Donation getByBagId(String bagId) {
        return donationRepository.findByBagId(bagId)
                .orElseThrow(() -> new RuntimeException("Donation not found for bag: " + bagId));
    }
}
