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
}
