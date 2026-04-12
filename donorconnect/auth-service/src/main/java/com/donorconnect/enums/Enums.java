package com.donorconnect.enums;

public class Enums {

    public enum UserRole {
        ROLE_DONOR, ROLE_RECEPTION, ROLE_PHLEBOTOMIST,
        ROLE_LAB_TECHNICIAN, ROLE_TRANSFUSION_OFFICER,
        ROLE_INVENTORY_CONTROLLER, ROLE_ADMIN
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED, PENDING
    }

    public enum DonorType {
        VOLUNTARY, REPLACEMENT, STUDENT, CORPORATE
    }

    public enum DonorStatus {
        ACTIVE, DEFERRED, BLACKLISTED, INACTIVE
    }

    public enum DeferralType {
        TEMPORARY, PERMANENT
    }

    public enum DeferralStatus {
        ACTIVE, LIFTED, EXPIRED
    }

    public enum AppointmentStatus {
        BOOKED, CHECKED_IN, COMPLETED, CANCELLED, NO_SHOW
    }

    public enum DriveStatus {
        PLANNED, ACTIVE, COMPLETED, CANCELLED
    }

    public enum CollectionStatus {
        COLLECTED, INCOMPLETE, REJECTED
    }

    public enum TestType {
        HIV, HBV, HCV, VDRL, MALARIA, NAT, BLOOD_GROUP, RH
    }

    public enum TestStatus {
        PENDING, COMPLETED, REACTIVE
    }

    public enum ComponentType {
        PRBC, PLATELET, PLASMA, CRYO
    }

    public enum ComponentStatus {
        AVAILABLE, EXPIRED, QUARANTINE, ISSUED, DISPOSED
    }

    public enum TransactionType {
        RECEIPT, ISSUE, RETURN, TRANSFER_IN, TRANSFER_OUT, ADJUST
    }

    public enum ExpiryWatchStatus {
        OPEN, ACTIONED
    }

    public enum CrossmatchPriority {
        ROUTINE, STAT
    }

    public enum CrossmatchStatus {
        PENDING, MATCHED, REJECTED
    }

    public enum Compatibility {
        COMPATIBLE, INCOMPATIBLE
    }

    public enum IssueStatus {
        ISSUED, RETURNED
    }

    public enum ReactionSeverity {
        MILD, MODERATE, SEVERE, FATAL
    }

    public enum ReactionStatus {
        PENDING, INVESTIGATING, CLOSED
    }

    public enum RecallStatus {
        OPEN, CLOSED
    }

    public enum QuarantineStatus {
        QUARANTINED, RELEASED, DISPOSED
    }

    public enum ChargeType {
        PROCESSING, SERVICE, CROSSMATCH
    }

    public enum BillingStatus {
        PENDING, PAID, CANCELLED
    }

    public enum ReportScope {
        SITE, PERIOD, COMPONENT_TYPE
    }

    public enum NotificationCategory {
        APPOINTMENT, EXPIRY, REACTIVE, CROSSMATCH, RECALL, STOCK
    }

    public enum NotificationStatus {
        UNREAD, READ, DISMISSED
    }
}
