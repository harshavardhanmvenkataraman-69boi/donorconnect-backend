package com.donorconnect.bloodsupplyservice.service;


import com.donorconnect.bloodsupplyservice.dto.request.DonationRequest;
import com.donorconnect.bloodsupplyservice.dto.AppointmentDto;
import com.donorconnect.bloodsupplyservice.entity.Donation;
import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import com.donorconnect.bloodsupplyservice.feign.DonorFeignClient;
import com.donorconnect.bloodsupplyservice.feign.AppointmentFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.donorconnect.bloodsupplyservice.repository.DonationRepository;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.donorconnect.bloodsupplyservice.Exception.ResourceNotFoundException;
import com.donorconnect.bloodsupplyservice.Exception.DonationDateValidationException;
import com.donorconnect.bloodsupplyservice.Exception.AppointmentStatusException;
import com.donorconnect.bloodsupplyservice.Exception.DonorDeferralException;
import com.donorconnect.bloodsupplyservice.Exception.ServiceUnavailableException;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonorFeignClient donorFeignClient;
    private final AppointmentFeignClient appointmentFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public Donation create(DonationRequest req) {
        // Validate donor with circuit breaker
        CircuitBreaker donorCircuitBreaker = circuitBreakerFactory.create("donorService");
        try {
            donorCircuitBreaker.run(() -> {
                donorFeignClient.getDonorById(req.getDonorId());
                return null;
            }, throwable -> {
                throw new ServiceUnavailableException("Donor service is currently unavailable. Please try again later.", "donor-service");
            });
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Donor", "ID", req.getDonorId());
        } catch (FeignException.Forbidden e) {
            throw new RuntimeException("Access denied: insufficient permissions to access donor service");
        } catch (FeignException.Unauthorized e) {
            throw new RuntimeException("Authentication failed: invalid or missing JWT token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate donor: " + e.getMessage());
        }

        // Check donor deferral with circuit breaker
        CircuitBreaker deferralCircuitBreaker = circuitBreakerFactory.create("deferralService");
        try {
            deferralCircuitBreaker.run(() -> {
                var deferralResponse = donorFeignClient.getDonorDeferralStatus(req.getDonorId());
                Object deferralData = deferralResponse.getData();

                if (deferralData != null) {
                    if (deferralData instanceof Map) {
                        Map<String, Object> deferralMap = (Map<String, Object>) deferralData;
                        Boolean isDeferral = (Boolean) deferralMap.get("isDeferral");
                        String reason = (String) deferralMap.get("reason");

                        if (isDeferral != null && isDeferral) {
                            throw new DonorDeferralException(
                                    "Donor is deferred and cannot donate. Reason: " + (reason != null ? reason : "Not specified"),
                                    reason != null ? reason : "DEFERRED"
                            );
                        }
                    }
                }
                return null;
            }, throwable -> {
                // If deferral service is down, allow donation but log the issue
                // You might want to add logging here
                return null;
            });
        } catch (DonorDeferralException e) {
            throw e; // Re-throw deferral exception
        } catch (Exception e) {
            // Handle other exceptions if needed
        }

        // Validate appointment with circuit breaker
        LocalDate collectionDate = req.getCollectionDate() != null ? req.getCollectionDate() : LocalDate.now();
        final Long[] validAppointmentId = {null};

        CircuitBreaker appointmentCircuitBreaker = circuitBreakerFactory.create("appointmentService");
        try {
            appointmentCircuitBreaker.run(() -> {
                var appointmentResponse = appointmentFeignClient.getAppointmentsByDonor(req.getDonorId());
                List<AppointmentDto> appointments = appointmentResponse.getData();

                boolean hasValidAppointment = false;
                boolean hasBookedAppointment = false;

                // Check appointment date validation and find booked appointment
                for (AppointmentDto appointment : appointments) {
                    if (collectionDate.isAfter(appointment.getDateTime().toLocalDate())) {
                        hasValidAppointment = true;

                        // Check if appointment status is BOOKED
                        if (appointment.getStatus() != null && appointment.getStatus().equalsIgnoreCase("BOOKED")) {
                            hasBookedAppointment = true;
                            validAppointmentId[0] = appointment.getAppointmentId();
                            break;
                        }
                    }
                }

                if (!hasValidAppointment) {
                    throw new DonationDateValidationException("Donation date must be after the appointment date");
                }

                if (!hasBookedAppointment) {
                    throw new AppointmentStatusException(
                            "Cannot create donation: No appointment with BOOKED status found for donor",
                            "NOT_BOOKED"
                    );
                }
                return null;
            }, throwable -> {
                throw new ServiceUnavailableException("Appointment service is currently unavailable. Please try again later.", "appointment-service");
            });
        } catch (DonationDateValidationException | AppointmentStatusException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate appointment: " + e.getMessage());
        }

        // Create the donation
        Donation d = Donation.builder()
                .donorId(req.getDonorId())
                .collectionDate(req.getCollectionDate() != null ? req.getCollectionDate() : LocalDate.now())
                .bagId(req.getBagId())
                .volumeMl(req.getVolumeMl())
                .collectedBy(req.getCollectedBy())
                .collectionStatus(CollectionStatus.COLLECTED)
                .build();

        Donation savedDonation = donationRepository.save(d);

        // Update appointment status to COMPLETED with circuit breaker
        try {
            CircuitBreaker updateAppointmentCircuitBreaker = circuitBreakerFactory.create("updateAppointmentService");
            updateAppointmentCircuitBreaker.run(() -> {
                if (validAppointmentId[0] != null) {
                    appointmentFeignClient.updateAppointmentStatus(validAppointmentId[0], "COMPLETED");
                }
                return null;
            }, throwable -> {
                // Log the error but don't fail the donation creation
                // You might want to add logging here
                return null;
            });
        } catch (Exception e) {
            // Log the error but don't fail the donation creation
        }

        return savedDonation;
    }

    public Page<Donation> getAll(Pageable pageable) {
        return donationRepository.findAll(pageable);
    }

    public Donation getById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", String.valueOf(id)));
    }

    public List<Donation> getByDonor(Long donorId) {
        // Validate that the donor exists in donor-service with circuit breaker
        CircuitBreaker donorCircuitBreaker = circuitBreakerFactory.create("donorService");
        try {
            donorCircuitBreaker.run(() -> {
                donorFeignClient.getDonorById(donorId);
                return null;
            }, throwable -> {
                throw new ServiceUnavailableException("Donor service is currently unavailable. Please try again later.", "donor-service");
            });
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
