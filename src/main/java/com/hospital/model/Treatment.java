package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Treatment entity recording doctor care, clinical diagnosis, prescribed medications, and procedures.
 */
public class Treatment {
    private int treatmentId;
    private int patientId;
    private int doctorId;
    private String doctorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String treatmentStatus; // e.g., IN_PROGRESS, COMPLETED, SUSPENDED
    private String diagnosis;
    private String medications;
    private String notes;

    public Treatment() {
        this.startTime = LocalDateTime.now();
        this.treatmentStatus = "IN_PROGRESS";
    }

    public Treatment(int treatmentId, int patientId, int doctorId, String doctorName,
                     LocalDateTime startTime, LocalDateTime endTime, String treatmentStatus, String notes) {
        this.treatmentId = treatmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.startTime = startTime != null ? startTime : LocalDateTime.now();
        this.endTime = endTime;
        this.treatmentStatus = treatmentStatus != null ? treatmentStatus : "IN_PROGRESS";
        this.notes = notes;
    }

    public Treatment(int treatmentId, int patientId, int doctorId, String doctorName,
                     LocalDateTime startTime, LocalDateTime endTime, String treatmentStatus,
                     String diagnosis, String medications, String notes) {
        this.treatmentId = treatmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.startTime = startTime != null ? startTime : LocalDateTime.now();
        this.endTime = endTime;
        this.treatmentStatus = treatmentStatus != null ? treatmentStatus : "IN_PROGRESS";
        this.diagnosis = diagnosis;
        this.medications = medications;
        this.notes = notes;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTreatmentStatus() {
        return treatmentStatus;
    }

    public void setTreatmentStatus(String treatmentStatus) {
        this.treatmentStatus = treatmentStatus;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
