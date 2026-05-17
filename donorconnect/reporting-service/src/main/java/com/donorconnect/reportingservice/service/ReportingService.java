package com.donorconnect.reportingservice.service;

import com.donorconnect.reportingservice.client.BloodSupplyClient;
import com.donorconnect.reportingservice.client.DonorClient;
import com.donorconnect.reportingservice.client.InventoryClient;
import com.donorconnect.reportingservice.client.SafetyClient;
import com.donorconnect.reportingservice.dto.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final LabReportPackRepository repo;
    private final InventoryClient inventoryClient;
    private final DonorClient donorClient;
    private final SafetyClient safetyClient;
    private final BloodSupplyClient bloodSupplyClient;

    // ── unwrap helpers ────────────────────────────────────────────────────────
    private <T> List<T> unwrapList(ServiceResponse<List<T>> response) {
        if (response == null || response.getData() == null) return Collections.emptyList();
        return response.getData();
    }

    private <T> List<T> unwrapPage(ServiceResponse<PageResponse<T>> response) {
        if (response == null || response.getData() == null) return Collections.emptyList();
        List<T> content = response.getData().getContent();
        return content != null ? content : Collections.emptyList();
    }

    // ── Legacy methods ────────────────────────────────────────────────────────
    public List<LabReportPack> getAll() { return repo.findAll(); }
    public List<LabReportPack> getByScope(ReportScope scope) { return repo.findByScope(scope); }
    public LabReportPack save(LabReportPack pack) { return repo.save(pack); }

    // ── Analytics ─────────────────────────────────────────────────────────────

    public Map<String, Object> getInventorySnapshot() {
        List<InventoryBalanceDto> all = unwrapList(inventoryClient.getAllInventory());
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Long> byType = new LinkedHashMap<>();
        for (InventoryBalanceDto inv : all) {
            if ("AVAILABLE".equalsIgnoreCase(inv.getStatus())) {
                String type = inv.getComponentType() != null ? inv.getComponentType() : "UNKNOWN";
                long qty = inv.getQuantity() != null ? inv.getQuantity() : 0L;
                byType.merge(type, qty, Long::sum);
            }
        }
        result.put("availableByComponentType", byType);

        Map<String, Long> byBloodGroup = new LinkedHashMap<>();
        for (InventoryBalanceDto inv : all) {
            if ("AVAILABLE".equalsIgnoreCase(inv.getStatus())) {
                String bg = (inv.getBloodGroup() != null ? inv.getBloodGroup() : "?")
                        + (inv.getRhFactor() != null ? inv.getRhFactor() : "");
                long qty = inv.getQuantity() != null ? inv.getQuantity() : 0L;
                byBloodGroup.merge(bg, qty, Long::sum);
            }
        }
        result.put("availableByBloodGroup", byBloodGroup);
        result.put("totalAvailableUnits", all.stream()
                .filter(i -> "AVAILABLE".equalsIgnoreCase(i.getStatus()))
                .mapToLong(i -> i.getQuantity() != null ? i.getQuantity() : 0L).sum());
        return result;
    }

    public List<InventoryBalanceDto> getExpiryRisk() {
        LocalDate now = LocalDate.now();
        LocalDate cutoff = now.plusDays(7);
        return unwrapList(inventoryClient.getAllInventory()).stream()
                .filter(i -> i.getExpiryDate() != null
                        && !i.getExpiryDate().isBefore(now)
                        && !i.getExpiryDate().isAfter(cutoff))
                .toList();
    }

    public Map<String, Long> getDonorActivity() {
        // uses /search with no params → returns plain List
        List<DonorDto> donors = unwrapList(donorClient.getAllDonors(null, null));
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("ACTIVE",      donors.stream().filter(d -> DonorStatus.ACTIVE.name().equals(sname(d.getStatus()))).count());
        result.put("DEFERRED",    donors.stream().filter(d -> DonorStatus.DEFERRED.name().equals(sname(d.getStatus()))).count());
        result.put("INACTIVE",    donors.stream().filter(d -> DonorStatus.INACTIVE.name().equals(sname(d.getStatus()))).count());
        result.put("BLACKLISTED", donors.stream().filter(d -> DonorStatus.BLACKLISTED.name().equals(sname(d.getStatus()))).count());
        result.put("TOTAL",       (long) donors.size());
        return result;
    }

    private String sname(DonorStatus s) { return s != null ? s.name() : ""; }

    public Map<String, Object> getDonationFrequency() {
        List<DonationDto> donations = unwrapPage(bloodSupplyClient.getAllDonations(0, 10000));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalDonations", donations.size());
        result.put("thisMonth", donations.stream()
                .filter(d -> d.getCollectionDate() != null
                        && d.getCollectionDate().getMonth() == LocalDate.now().getMonth()
                        && d.getCollectionDate().getYear() == LocalDate.now().getYear())
                .count());
        return result;
    }

    public Map<String, Object> getComponentWastage() {
        List<InventoryBalanceDto> all = unwrapList(inventoryClient.getAllInventory());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expired",     all.stream().filter(i -> "EXPIRED".equalsIgnoreCase(i.getStatus())).count());
        result.put("disposed",    all.stream().filter(i -> "DISPOSED".equalsIgnoreCase(i.getStatus())).count());
        result.put("quarantined", all.stream().filter(i ->
                "QUARANTINED".equalsIgnoreCase(i.getStatus()) || "QUARANTINE".equalsIgnoreCase(i.getStatus())).count());
        return result;
    }

    public Map<String, Long> getReactiveCount() {
        List<TestResultDto> reactives = unwrapList(bloodSupplyClient.getReactiveTestResults());
        Map<String, Long> result = new LinkedHashMap<>();
        for (TestType type : TestType.values()) {
            result.put(type.name(), reactives.stream()
                    .filter(t -> t.getTestType() != null && t.getTestType() == type)
                    .count());
        }
        return result;
    }

    public Map<String, Object> getDeferralTrends() {
        List<DeferralDto> deferrals = unwrapList(donorClient.getActiveDeferrals());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeDeferrals", deferrals.size());
        result.put("temporary", deferrals.stream().filter(d -> d.getDeferralType() == DeferralType.TEMPORARY).count());
        result.put("permanent", deferrals.stream().filter(d -> d.getDeferralType() == DeferralType.PERMANENT).count());
        return result;
    }

    public Map<String, String> getTAT() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("note", "TAT tracking requires analyzer integration (Phase-2)");
        result.put("totalDonations", String.valueOf(unwrapPage(bloodSupplyClient.getAllDonations(0, 10000)).size()));
        result.put("pendingTests",   String.valueOf(unwrapList(bloodSupplyClient.getPendingTestResults()).size()));
        result.put("reactiveTests",  String.valueOf(unwrapList(bloodSupplyClient.getReactiveTestResults()).size()));
        return result;
    }

    public Map<String, Object> getUtilization() {
        List<InventoryBalanceDto> all = unwrapList(inventoryClient.getAllInventory());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available",  all.stream().filter(i -> "AVAILABLE".equalsIgnoreCase(i.getStatus())).count());
        result.put("issued",     all.stream().filter(i -> "ISSUED".equalsIgnoreCase(i.getStatus())).count());
        result.put("reserved",   all.stream().filter(i -> "RESERVED".equalsIgnoreCase(i.getStatus())).count());
        result.put("expired",    all.stream().filter(i -> "EXPIRED".equalsIgnoreCase(i.getStatus())).count());
        result.put("quarantine", all.stream().filter(i ->
                "QUARANTINED".equalsIgnoreCase(i.getStatus()) || "QUARANTINE".equalsIgnoreCase(i.getStatus())).count());
        result.put("disposed",   all.stream().filter(i -> "DISPOSED".equalsIgnoreCase(i.getStatus())).count());
        return result;
    }

    public Map<String, Object> getAdverseReactionSummary() {
        List<ReactionDto> reactions = unwrapList(safetyClient.getAllReactions(0, 10000));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", reactions.size());
        result.put("bySeverity", reactions.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getSeverity() != null ? r.getSeverity() : "UNKNOWN",
                        Collectors.counting())));
        result.put("byStatus", reactions.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus() != null ? r.getStatus() : "UNKNOWN",
                        Collectors.counting())));
        return result;
    }

    public LabReportPack generateReport(ReportScope scope) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("inventorySnapshot", getInventorySnapshot());
        metrics.put("donorActivity", getDonorActivity());
        metrics.put("wastage", getComponentWastage());
        metrics.put("utilization", getUtilization());
        metrics.put("deferralTrends", getDeferralTrends());
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
