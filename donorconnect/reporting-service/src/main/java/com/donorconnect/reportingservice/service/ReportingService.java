package com.donorconnect.reportingservice.service;

import com.donorconnect.reportingservice.client.DonorClient;
import com.donorconnect.reportingservice.client.InventoryClient;
import com.donorconnect.reportingservice.client.SafetyClient;
import com.donorconnect.reportingservice.dto.BloodComponentDto;
import com.donorconnect.reportingservice.dto.DeferralDto;
import com.donorconnect.reportingservice.dto.DonationDto;
import com.donorconnect.reportingservice.dto.DonorDto;
import com.donorconnect.reportingservice.dto.TestResultDto;
import com.donorconnect.reportingservice.entity.LabReportPack;
import com.donorconnect.reportingservice.enums.*;
import com.donorconnect.reportingservice.exception.ResourceNotFoundException;
import com.donorconnect.reportingservice.repository.LabReportPackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final LabReportPackRepository repo;
    private final InventoryClient inventoryClient;
    private final DonorClient donorClient;
    private final SafetyClient safetyClient;

    // ── Legacy methods (kept for backward compatibility) ──────────────────────
    /** @deprecated use getAllPacks(Pageable) */
    public List<LabReportPack> getAll() { return repo.findAll(); }
    public List<LabReportPack> getByScope(ReportScope scope) { return repo.findByScope(scope); }
    public LabReportPack save(LabReportPack pack) { return repo.save(pack); }

    // ── Analytics ─────────────────────────────────────────────────────────────

    public Map<String, Object> getInventorySnapshot() {
        List<BloodComponentDto> all = inventoryClient.getAllComponents();
        Map<String, Object> result = new LinkedHashMap<>();
        for (ComponentType type : ComponentType.values()) {
            long count = all.stream()
                    .filter(c -> c.getComponentType() == type && c.getStatus() == ComponentStatus.AVAILABLE)
                    .count();
            result.put(type.name(), count);
        }
        return result;
    }

    public List<BloodComponentDto> getExpiryRisk() {
        LocalDate now = LocalDate.now();
        LocalDate cutoff = now.plusDays(7);
        return inventoryClient.getAllComponents().stream()
                .filter(c -> c.getExpiryDate() != null
                        && !c.getExpiryDate().isBefore(now)
                        && !c.getExpiryDate().isAfter(cutoff))
                .toList();
    }

    public Map<String, Long> getDonorActivity() {
        List<DonorDto> donors = donorClient.getAllDonors();
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("ACTIVE",   donors.stream().filter(d -> d.getStatus() == DonorStatus.ACTIVE).count());
        result.put("DEFERRED", donors.stream().filter(d -> d.getStatus() == DonorStatus.DEFERRED).count());
        result.put("INACTIVE", donors.stream().filter(d -> d.getStatus() == DonorStatus.INACTIVE).count());
        result.put("TOTAL",    (long) donors.size());
        return result;
    }

    public Map<String, Object> getDonationFrequency() {
        List<DonationDto> donations = donorClient.getAllDonations();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalDonations", donations.size());
        result.put("thisMonth", donations.stream()
                .filter(d -> d.getCollectionDate() != null &&
                        d.getCollectionDate().getMonth() == LocalDate.now().getMonth())
                .count());
        return result;
    }

    public Map<String, Object> getComponentWastage() {
        List<BloodComponentDto> all = inventoryClient.getAllComponents();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expired",  all.stream().filter(c -> c.getStatus() == ComponentStatus.EXPIRED).count());
        result.put("disposed", all.stream().filter(c -> c.getStatus() == ComponentStatus.DISPOSED).count());
        return result;
    }

    public Map<String, Long> getReactiveCount() {
        List<TestResultDto> reactives = safetyClient.getAllTestResults().stream()
                .filter(t -> t.getStatus() == TestStatus.REACTIVE).toList();
        Map<String, Long> result = new LinkedHashMap<>();
        for (TestType type : TestType.values()) {
            result.put(type.name(), reactives.stream().filter(t -> t.getTestType() == type).count());
        }
        return result;
    }

    public Map<String, Object> getDeferralTrends() {
        List<DeferralDto> deferrals = donorClient.getAllDeferrals();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalDeferrals", deferrals.size());
        result.put("temporary", deferrals.stream().filter(d -> d.getDeferralType() == DeferralType.TEMPORARY).count());
        result.put("permanent", deferrals.stream().filter(d -> d.getDeferralType() == DeferralType.PERMANENT).count());
        return result;
    }

    public Map<String, String> getTAT() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("note", "TAT tracking requires analyzer integration (Phase-2)");
        result.put("totalDonations", String.valueOf(donorClient.getAllDonations().size()));
        result.put("completedTests", String.valueOf(
                safetyClient.getAllTestResults().stream().filter(t -> t.getStatus() == TestStatus.COMPLETED).count()));
        return result;
    }

    public Map<String, Object> getUtilization() {
        List<BloodComponentDto> all = inventoryClient.getAllComponents();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available",  all.stream().filter(c -> c.getStatus() == ComponentStatus.AVAILABLE).count());
        result.put("issued",     all.stream().filter(c -> c.getStatus() == ComponentStatus.ISSUED).count());
        result.put("expired",    all.stream().filter(c -> c.getStatus() == ComponentStatus.EXPIRED).count());
        result.put("quarantine", all.stream().filter(c -> c.getStatus() == ComponentStatus.QUARANTINE).count());
        return result;
    }

    public LabReportPack generateReport(ReportScope scope) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("inventorySnapshot", getInventorySnapshot());
        metrics.put("donorActivity", getDonorActivity());
        metrics.put("wastage", getComponentWastage());
        LabReportPack pack = LabReportPack.builder()
                .scope(scope)
                .metricsJson(metrics.toString())
                .generatedDate(LocalDateTime.now())
                .build();
        return repo.save(pack);
    }

    public LabReportPack getPackById(Long packId) {
        return repo.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("LabReportPack", packId));
    }

    public Page<LabReportPack> getAllPacks(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
