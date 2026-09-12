package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Nurse user who performs initial intake, registration, and symptom recording.
 */
public class Nurse extends User {
    private String shift;
    private String station;

    public Nurse() {
        super();
        setRole(Role.NURSE);
        this.station = "Triage Station 1";
    }

    public Nurse(int userId, String username, String passwordHash, String fullName,
                 String phone, String email, boolean active, LocalDateTime createdAt,
                 String shift, String station) {
        super(userId, username, passwordHash, fullName, Role.NURSE, phone, email, active, createdAt);
        this.shift = shift;
        this.station = station != null ? station : "Triage Station 1";
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }
}
