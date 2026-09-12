package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Doctor user who performs triage reviews, diagnosis, and patient treatment.
 */
public class Doctor extends User {
    private String specialization;
    private String department;

    public Doctor() {
        super();
        setRole(Role.DOCTOR);
        this.department = "Emergency Medicine";
    }

    public Doctor(int userId, String username, String passwordHash, String fullName,
                  String phone, String email, boolean active, LocalDateTime createdAt,
                  String specialization, String department) {
        super(userId, username, passwordHash, fullName, Role.DOCTOR, phone, email, active, createdAt);
        this.specialization = specialization;
        this.department = department != null ? department : "Emergency Medicine";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
