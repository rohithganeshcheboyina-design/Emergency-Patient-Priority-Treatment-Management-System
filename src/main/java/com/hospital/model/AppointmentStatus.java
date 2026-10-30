package com.hospital.model;

/**
 * Status of a patient appointment consultation.
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AppointmentStatus fromString(String text) {
        if (text == null) return SCHEDULED;
        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return SCHEDULED;
    }
}
