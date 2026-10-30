package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Patient user account for patient portal access.
 */
public class PatientUser extends User {

    private String patientCode;

    public PatientUser() {
        super();
        setRole(Role.PATIENT);
    }

    public PatientUser(int userId, String username, String fullName, String email, String patientCode) {
        super(userId, username, fullName, email, Role.PATIENT);
        this.patientCode = patientCode;
    }

    public PatientUser(int userId, String username, String passwordHash, String fullName,
                       String phone, String email, boolean active, LocalDateTime createdAt, String patientCode) {
        super(userId, username, passwordHash, fullName, Role.PATIENT, phone, email, active, createdAt);
        this.patientCode = patientCode;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }
}
