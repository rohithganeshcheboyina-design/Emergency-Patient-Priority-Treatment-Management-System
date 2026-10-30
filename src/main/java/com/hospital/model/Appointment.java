package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Appointment entity representing scheduled clinical consultations.
 * NOTE: Emergency triage patients have immediate priority over scheduled appointments.
 */
public class Appointment {
    private int appointmentId;
    private int patientId;
    private String patientName;
    private String patientCode;
    private int doctorId;
    private String doctorName;
    private LocalDateTime appointmentTime;
    private String specialty;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;

    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
    }

    public Appointment(int appointmentId, int patientId, String patientName, String patientCode,
                       int doctorId, String doctorName, LocalDateTime appointmentTime,
                       String specialty, AppointmentStatus status, String reason, String notes,
                       LocalDateTime createdAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientCode = patientCode;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.appointmentTime = appointmentTime;
        this.specialty = specialty;
        this.status = status != null ? status : AppointmentStatus.SCHEDULED;
        this.reason = reason;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
