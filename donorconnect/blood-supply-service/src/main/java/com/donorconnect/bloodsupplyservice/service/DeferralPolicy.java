package com.donorconnect.bloodsupplyservice.service;

import com.donorconnect.bloodsupplyservice.enums.TestType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Policy: which test types, when reactive, trigger which deferral type.
 *
 * Real-world blood banking rules (simplified):
 *   - HIV, HBV, HCV, NAT  -> chronic / lifelong infections -> PERMANENT
 *   - VDRL (syphilis)     -> treatable                     -> TEMPORARY (12 months typical)
 *   - MALARIA             -> recovers / time-bound risk    -> TEMPORARY (3 years typical)
 *   - BLOOD_GROUP, RH     -> typing, not infection         -> no deferral
 *
 * NOTE: This deliberately lives outside the policy enum itself so we can change
 * the mapping without touching the data model.
 */
public final class DeferralPolicy {

    private static final Set<TestType> PERMANENT =
            EnumSet.of(TestType.HIV, TestType.HBV, TestType.HCV, TestType.NAT);

    private static final Set<TestType> TEMPORARY =
            EnumSet.of(TestType.VDRL, TestType.MALARIA);

    /** Whether this test type, when reactive, should trigger any deferral. */
    public static boolean triggersDeferral(TestType type) {
        return type != null && (PERMANENT.contains(type) || TEMPORARY.contains(type));
    }

    /** "PERMANENT" or "TEMPORARY". Caller should check triggersDeferral first. */
    public static String deferralType(TestType type) {
        if (type != null && PERMANENT.contains(type)) return "PERMANENT";
        return "TEMPORARY";
    }

    private DeferralPolicy() {}
}
