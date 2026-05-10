package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.dto.request.DonationRequest;
import com.donorconnect.bloodsupplyservice.dto.AppointmentDto;
import com.donorconnect.bloodsupplyservice.entity.Donation;
import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import com.donorconnect.bloodsupplyservice.feign.DonorFeignClient;
import com.donorconnect.bloodsupplyservice.feign.AppointmentFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonorFeignClient donorFeignClient;
    private final AppointmentFeignClient appointmentFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public Donation create(DonationRequest req) {

        // 1. Validate donor exists — if service down, log and continue
        try {
            CircuitBreaker cb = circuitBreakerFactory.create("donorService");
            cb.run(() -> { donorFeignClient.getDonorById(req.getDonorId()); return null; },
                throwable -> { log.warn("Donor-service unavailable: {}", throwable.getMessage()); return null; });
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Donor", "ID", req.getDonorId());
        } catch (FeignException.Forbidden | FeignException.Unauthorized e) {
            log.warn("Auth issue calling donor-service: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Could not validate donor, proceeding: {}", e.getMessage());
        }

        // 2. Check deferral — if service down, allow donation
        try {
            CircuitBreaker cb = circuitBreakerFactory.create("deferralService");
            cb.run(() -> {
                var deferralResponse = donorFeignClient.getDonorDeferralStatus(req.getDonorId());
                Object deferralData = deferralResponse.getData();
                if (deferralData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) deferralData;
                    Boolean isDeferral = (Boolean) map.get("isDeferral");
                    String reason = (String) map.get("reason");
                    if (Boolean.TRUE.equals(isDeferral)) {
                        throw new DonorDeferralException(
                            "Donor is deferred and cannot donate. Reason: " + (reason != null ? reason : "Not specified"),
                            reason != null ? reason : "DEFERRED");
                    }
                }
                return null;
            }, throwable -> { log.warn("Deferral service unavailable, allowing donation: {}", throwable.getMessage()); return null; });
        } catch (DonorDeferralException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Deferral check failed, proceeding: {}", e.getMessage());
        }

        // 3. Validate appointment — if service down, skip validation
        LocalDate collectionDate = req.getCollectionDate() != null ? req.getCollectionDate() : LocalDate.now();
        final Long[] validAppointmentId = {null};

        try {
            CircuitBreaker cb = circuitBreakerFactory.create("appointmentService");
            cb.run(() -> {
                var appointmentResponse = appointmentFeignClient.getAppointmentsByDonor(req.getDonorId());
                List<AppointmentDto> appointments = appointmentResponse.getData();

                if (appointments == null || appointments.isEmpty()) {
                    log.warn("No appointments found for donor {}, proceeding", req.getDonorId());
                    return null;
                }

                boolean hasValidAppointment = false;
                boolean hasBookedAppointment = false;

                for (AppointmentDto appointment : appointments) {
                    // FIX: if dateTime is null, skip date check — treat as valid
                    boolean dateOk = appointment.getDateTime() == null
                        || collectionDate.isAfter(appointment.getDateTime().toLocalDate())
                        || collectionDate.isEqual(appointment.getDateTime().toLocalDate());

                    if (dateOk) {
                        hasValidAppointment = true;
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
                        "Cannot create donation: No BOOKED appointment found for donor", "NOT_BOOKED");
                }
                return null;
            }, throwable -> {
                log.warn("Appointment-service unavailable, skipping validation: {}", throwable.getMessage());
                return null;
            });
        } catch (DonationDateValidationException | AppointmentStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Appointment validation failed, proceeding: {}", e.getMessage());
        }

        // 4. Save donation
        Donation d = Donation.builder()
                .donorId(req.getDonorId())
                .collectionDate(collectionDate)
                .bagId(req.getBagId())
                .volumeMl(req.getVolumeMl())
                .collectedBy(req.getCollectedBy())
                .collectionStatus(req.getCollectionStatus() != null ? req.getCollectionStatus() : CollectionStatus.COLLECTED)
                .build();

        Donation saved = donationRepository.save(d);

        // 5. Update appointment status — fire and forget
        try {
            if (validAppointmentId[0] != null) {
                CircuitBreaker cb = circuitBreakerFactory.create("updateAppointmentService");
                final Long apptId = validAppointmentId[0];
                cb.run(() -> { appointmentFeignClient.updateAppointmentStatus(apptId, "COMPLETED"); return null; },
                    throwable -> { log.warn("Could not update appointment status: {}", throwable.getMessage()); return null; });
            }
        } catch (Exception e) {
            log.warn("Appointment status update failed (non-critical): {}", e.getMessage());
        }

        return saved;
    }

    public Page<Donation> getAll(Pageable pageable) {
        return donationRepository.findAll(pageable);
    }

    public Donation getById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", String.valueOf(id)));
    }

    public List<Donation> getByDonor(Long donorId) {
        try {
            CircuitBreaker cb = circuitBreakerFactory.create("donorService");
            cb.run(() -> { donorFeignClient.getDonorById(donorId); return null; },
                throwable -> { log.warn("Donor-service unavailable: {}", throwable.getMessage()); return null; });
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Donor", "ID", donorId);
        } catch (Exception e) {
            log.warn("Donor validation failed: {}", e.getMessage());
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
