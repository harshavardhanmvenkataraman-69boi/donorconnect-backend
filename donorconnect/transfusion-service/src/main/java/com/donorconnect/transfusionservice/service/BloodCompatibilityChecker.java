package com.donorconnect.transfusionservice.service;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BloodCompatibilityChecker {

    // ─── MAIN METHOD — routes by component type ───────────────────────
    public List<String[]> getCompatibleGroups(String bloodGroup, String rhFactor, String componentType) {
        return switch (componentType.toUpperCase()) {
            case "PRBC"     -> getCompatibleGroupsForPRBC(bloodGroup, rhFactor);
            case "PLASMA"   -> getCompatibleGroupsForPlasma(bloodGroup);
            case "PLATELET" -> getCompatibleGroupsForPlatelet(bloodGroup);
            case "CRYO"     -> getCompatibleGroupsForCryo();
            default         -> getCompatibleGroupsForPRBC(bloodGroup, rhFactor);
        };
    }

    // ─── PRBC ────────────────────────────────────────────────────────
    // Strict ABO + Rh matching
    private List<String[]> getCompatibleGroupsForPRBC(String bloodGroup, String rhFactor) {
        String recipient = bloodGroup + (rhFactor.equalsIgnoreCase("POSITIVE") ? "+" : "-");
        return switch (recipient) {
            case "O-"  -> List.<String[]>of(
                    new String[]{"O", "NEGATIVE"});
            case "O+"  -> List.of(
                    new String[]{"O", "POSITIVE"},
                    new String[]{"O", "NEGATIVE"});
            case "A-"  -> List.of(
                    new String[]{"A", "NEGATIVE"},
                    new String[]{"O", "NEGATIVE"});
            case "A+"  -> List.of(
                    new String[]{"A", "POSITIVE"}, new String[]{"A", "NEGATIVE"},
                    new String[]{"O", "POSITIVE"}, new String[]{"O", "NEGATIVE"});
            case "B-"  -> List.of(
                    new String[]{"B", "NEGATIVE"},
                    new String[]{"O", "NEGATIVE"});
            case "B+"  -> List.of(
                    new String[]{"B", "POSITIVE"}, new String[]{"B", "NEGATIVE"},
                    new String[]{"O", "POSITIVE"}, new String[]{"O", "NEGATIVE"});
            case "AB-" -> List.of(
                    new String[]{"AB", "NEGATIVE"}, new String[]{"A", "NEGATIVE"},
                    new String[]{"B",  "NEGATIVE"}, new String[]{"O", "NEGATIVE"});
            case "AB+" -> List.of(
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"},
                    new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                    new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                    new String[]{"O",  "POSITIVE"}, new String[]{"O",  "NEGATIVE"});
            default -> List.<String[]>of(new String[]{bloodGroup, rhFactor});
        };
    }

    // ─── PLASMA ──────────────────────────────────────────────────────
    // Rh factor does NOT matter — only ABO matters
    // AB plasma = universal donor
    private List<String[]> getCompatibleGroupsForPlasma(String bloodGroup) {
        return switch (bloodGroup.toUpperCase()) {
            case "O"  -> List.of(
                    new String[]{"O", "POSITIVE"},
                    new String[]{"O", "NEGATIVE"});
            case "A"  -> List.of(
                    new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"});
            case "B"  -> List.of(
                    new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"});
            case "AB" -> List.of(
                    new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                    new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"},
                    new String[]{"O",  "POSITIVE"}, new String[]{"O",  "NEGATIVE"});
            default -> List.of(new String[]{bloodGroup, "POSITIVE"}, new String[]{bloodGroup, "NEGATIVE"});
        };
    }

    // ─── PLATELET ────────────────────────────────────────────────────
    // ABO-compatible preferred, flexible in emergency
    // Rh factor not strictly required
    private List<String[]> getCompatibleGroupsForPlatelet(String bloodGroup) {
        return switch (bloodGroup.toUpperCase()) {
            case "O"  -> List.of(
                    new String[]{"O",  "POSITIVE"}, new String[]{"O",  "NEGATIVE"},
                    new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                    new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"});
            case "A"  -> List.of(
                    new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"});
            case "B"  -> List.of(
                    new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"});
            case "AB" -> List.of(
                    new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                    new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                    new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"},
                    new String[]{"O",  "POSITIVE"}, new String[]{"O",  "NEGATIVE"});
            default -> List.of(new String[]{bloodGroup, "POSITIVE"}, new String[]{bloodGroup, "NEGATIVE"});
        };
    }

    // ─── CRYO ────────────────────────────────────────────────────────
    // Universal — any group can receive from any group
    // Rh factor does NOT matter
    private List<String[]> getCompatibleGroupsForCryo() {
        return List.of(
                new String[]{"A",  "POSITIVE"}, new String[]{"A",  "NEGATIVE"},
                new String[]{"B",  "POSITIVE"}, new String[]{"B",  "NEGATIVE"},
                new String[]{"AB", "POSITIVE"}, new String[]{"AB", "NEGATIVE"},
                new String[]{"O",  "POSITIVE"}, new String[]{"O",  "NEGATIVE"});
    }
}