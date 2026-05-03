package com.donorconnect.bloodsupplyservice.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AppointmentStatusException extends RuntimeException {
    private final String appointmentStatus;

    public AppointmentStatusException(String message, String appointmentStatus) {
        super(message);
        this.appointmentStatus = appointmentStatus;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }
}
