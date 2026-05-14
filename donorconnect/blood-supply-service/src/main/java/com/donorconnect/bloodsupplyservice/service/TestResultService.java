package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.dto.request.DeferralRequestDto;
import com.donorconnect.bloodsupplyservice.dto.request.QuarantineRequest;
import com.donorconnect.bloodsupplyservice.dto.request.TestResultRequest;
import com.donorconnect.bloodsupplyservice.entity.BloodComponent;
import com.donorconnect.bloodsupplyservice.entity.Donation;
import com.donorconnect.bloodsupplyservice.entity.TestResult;
import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
import com.donorconnect.bloodsupplyservice.enums.TestStatus;
import com.donorconnect.bloodsupplyservice.enums.TestType;
import com.donorconnect.bloodsupplyservice.feign.DeferralFeignClient;
import com.donorconnect.bloodsupplyservice.repository.BloodComponentRepository;
import com.donorconnect.bloodsupplyservice.repository.DonationRepository;
import com.donorconnect.bloodsupplyservice.repository.TestResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Test results pipeline.
 *
 * Behavior on REACTIVE result:
 *   1. The test result is saved with status = REACTIVE.
 *   2. Any existing BloodComponents for that donation are auto-moved to QUARANTINE
 *      (each producing a QuarantineAction row).
 *   3. The donor is deferred via Feign call to donor-service. Deferral type is
 *      determined by DeferralPolicy (HIV/HBV/HCV/NAT -> PERMANENT; VDRL/MALARIA
 *      -> TEMPORARY; BLOOD_GROUP/RH -> no deferral).
 *
 *   Components are NOT auto-disposed — a supervisor reviews them in the
 *   Quarantine page and either Disposes (confirmed unsafe) or Releases
 *   (re-test cleared, false positive).
 *
 *   For donations that have no components yet (test entered before
 *   component-registration), step 2 is a no-op; once a human tries to register
 *   a component later, BloodComponentService will auto-create it in QUARANTINE
 *   state because of the reactive test on file.
 *
 *   All side effects are best-effort: a downstream failure logs an error
 *   but the test result is still persisted.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final DonationRepository donationRepository;
    private final BloodComponentRepository bloodComponentRepository;
    private final QuarantineDisposalService quarantineDisposalService;
    private final DeferralFeignClient deferralFeignClient;

    @Transactional
    public TestResult create(TestResultRequest req) {
        boolean isReactive = req.getResult() != null
                && req.getResult().equalsIgnoreCase("REACTIVE");

        TestResult t = TestResult.builder()
                .donationId(req.getDonationId())
                .testType(req.getTestType())
                .result(req.getResult())
                .resultDate(req.getResultDate() != null ? req.getResultDate() : LocalDate.now())
                .enteredBy(req.getEnteredBy())
                .status(isReactive ? TestStatus.REACTIVE : TestStatus.COMPLETED)
                .build();

        TestResult saved = testResultRepository.save(t);

        if (isReactive) {
            handleReactive(saved);
        }
        return saved;
    }

    public TestResult getById(Long id) {
        return testResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", String.valueOf(id)));
    }

    public List<TestResult> getByDonation(Long donationId) {
        return testResultRepository.findByDonationId(donationId);
    }

    @Transactional
    public TestResult update(Long id, TestResultRequest req) {
        TestResult t = getById(id);
        boolean wasReactive = t.getStatus() == TestStatus.REACTIVE;
        boolean isReactive = false;

        if (req.getResult() != null) {
            t.setResult(req.getResult());
            isReactive = req.getResult().equalsIgnoreCase("REACTIVE");
            t.setStatus(isReactive ? TestStatus.REACTIVE : TestStatus.COMPLETED);
        }
        if (req.getEnteredBy() != null) t.setEnteredBy(req.getEnteredBy());

        TestResult saved = testResultRepository.save(t);

        // Only fire the reactive pipeline when this update flipped from
        // not-reactive -> reactive. Don't re-fire on every edit of an
        // already-reactive row.
        if (isReactive && !wasReactive) {
            handleReactive(saved);
        }
        return saved;
    }

    public List<TestResult> getReactive() {
        return testResultRepository.findByStatus(TestStatus.REACTIVE);
    }

    public List<TestResult> getPending() {
        return testResultRepository.findByStatus(TestStatus.PENDING);
    }

    /** Non-reactive = COMPLETED (all infectious-marker tests came back clean, or just typing results). */
    public List<TestResult> getNonReactive() {
        return testResultRepository.findByStatus(TestStatus.COMPLETED);
    }

    /**
     * The 7 mandatory test types for a donation. NAT is intentionally excluded —
     * many sites don't run it, so we don't require it for completion.
     */
    public static final java.util.Set<TestType> REQUIRED_TESTS = java.util.Set.of(
            TestType.HIV, TestType.HBV, TestType.HCV,
            TestType.VDRL, TestType.MALARIA,
            TestType.BLOOD_GROUP, TestType.RH
    );

    /**
     * Compute the readiness of a donation for component registration.
     * Returns a payload the controller exposes at /api/v1/donations/{id}/component-readiness.
     *
     * Rules:
     *   - If ANY test came back REACTIVE -> blocked = true, reason = REACTIVE
     *   - Else if not all 7 REQUIRED_TESTS are entered -> blocked = true, reason = INCOMPLETE,
     *     listing what's missing
     *   - Else -> blocked = false, ready to register components
     */
    public java.util.Map<String, Object> getComponentReadiness(Long donationId) {
        java.util.List<TestResult> tests = testResultRepository.findByDonationId(donationId);

        // 1) Reactive check first — once any test is reactive, registration is blocked.
        java.util.List<String> reactiveTests = tests.stream()
                .filter(t -> t.getStatus() == TestStatus.REACTIVE)
                .map(t -> t.getTestType() != null ? t.getTestType().name() : "UNKNOWN")
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("donationId", donationId);

        if (!reactiveTests.isEmpty()) {
            out.put("ready", false);
            out.put("reason", "REACTIVE");
            out.put("reactiveTests", reactiveTests);
            out.put("message", "Donation has reactive test result(s) — components cannot be created for this donation.");
            return out;
        }

        // 2) Completeness check — every REQUIRED_TESTS entry must be entered.
        java.util.Set<TestType> entered = tests.stream()
                .map(TestResult::getTestType)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.List<String> missing = REQUIRED_TESTS.stream()
                .filter(t -> !entered.contains(t))
                .map(Enum::name)
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        if (!missing.isEmpty()) {
            out.put("ready", false);
            out.put("reason", "INCOMPLETE");
            out.put("missingTests", missing);
            out.put("message", "Tests not complete. Missing: " + String.join(", ", missing));
            return out;
        }

        out.put("ready", true);
        out.put("reason", "CLEARED");
        out.put("message", "All 7 mandatory tests are entered and non-reactive — ready for component registration.");
        return out;
    }

    /**
     * Bulk-create test results for a donation in one transaction.
     *
     * Each entry in the map becomes one TestResult row. Reactive pipeline still
     * fires (once) per reactive entry — quarantine + deferral are best-effort
     * inside each create call.
     *
     * Returns the list of saved TestResults so the caller can show what landed.
     */
    @Transactional
    public List<TestResult> createBulk(com.donorconnect.bloodsupplyservice.dto.request.BulkTestResultRequest req) {
        if (req.getResults() == null || req.getResults().isEmpty()) {
            throw new IllegalArgumentException("results map is empty — nothing to save");
        }

        java.util.List<TestResult> saved = new java.util.ArrayList<>();
        for (var entry : req.getResults().entrySet()) {
            TestResultRequest single = new TestResultRequest();
            single.setDonationId(req.getDonationId());
            single.setTestType(entry.getKey());
            single.setResult(entry.getValue());
            single.setResultDate(req.getResultDate());
            single.setEnteredBy(req.getEnteredBy());
            // Don't set status here — create() decides based on result string.
            saved.add(create(single));
        }
        return saved;
    }

    // ============================================================
    // Reactive handler — runs side effects, never throws
    // ============================================================
    private void handleReactive(TestResult tr) {
        Long donationId = tr.getDonationId();
        TestType testType = tr.getTestType();
        String testTypeName = testType != null ? testType.name() : "UNKNOWN";

        log.warn("REACTIVE result detected. donationId={}, testType={} — running quarantine + deferral pipeline",
                donationId, testTypeName);

        // 1) Auto-quarantine any components that already exist for this donation.
        //    (Usually zero — but if the lab registered components first and
        //    then tested, this catches them.)
        try {
            List<BloodComponent> components = bloodComponentRepository.findByDonationId(donationId);
            for (BloodComponent c : components) {
                if (c.getStatus() == ComponentStatus.AVAILABLE) {
                    QuarantineRequest qr = new QuarantineRequest();
                    qr.setComponentId(c.getComponentId());
                    qr.setReason("Auto-quarantine: reactive " + testTypeName + " on donation " + donationId);
                    quarantineDisposalService.quarantine(qr);
                }
            }
            if (components.isEmpty()) {
                log.info("No existing components for donation {} — components registered later will be auto-quarantined", donationId);
            }
        } catch (Exception e) {
            log.error("Failed to auto-quarantine components for donation {}: {}", donationId, e.getMessage());
        }

        // 2) Defer the donor based on test type. Only infectious-disease tests
        //    trigger a deferral — blood-group/Rh results do not.
        if (!DeferralPolicy.triggersDeferral(testType)) {
            log.info("Test {} does not trigger deferral — skipping", testTypeName);
            return;
        }
        try {
            Donation donation = donationRepository.findById(donationId).orElse(null);
            if (donation == null) {
                log.error("Donation {} not found while handling reactive result — deferral skipped", donationId);
                return;
            }
            String deferralType = DeferralPolicy.deferralType(testType);
            DeferralRequestDto deferral = DeferralRequestDto.builder()
                    .donorId(donation.getDonorId())
                    .deferralType(deferralType)
                    .reason("Reactive " + testTypeName + " on donation " + donationId)
                    .build();
            deferralFeignClient.createDeferral(deferral);
            log.info("{} deferral created for donor {} (reactive {})",
                    deferralType, donation.getDonorId(), testTypeName);
        } catch (Exception e) {
            log.error("Failed to create deferral: {}", e.getMessage());
        }
    }
}
