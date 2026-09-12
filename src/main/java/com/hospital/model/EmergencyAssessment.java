package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Audit record of a triage assessment made by medical staff.
 */
public class EmergencyAssessment {
    private int assessmentId;
    private int patientId;
    private EmergencyLevel emergencyLevel;
    private int priority;
    private String symptoms;
    private int assessedBy;
    private String assessorName;
    private LocalDateTime assessedAt;
    private String notes;

    public EmergencyAssessment() {
        this.assessedAt = LocalDateTime.now();
    }

    public EmergencyAssessment(int assessmentId, int patientId, EmergencyLevel emergencyLevel,
                               String symptoms, int assessedBy, String assessorName,
                               LocalDateTime assessedAt, String notes) {
        this.assessmentId = assessmentId;
        this.patientId = patientId;
        this.emergencyLevel = emergencyLevel;
        this.priority = emergencyLevel != null ? emergencyLevel.getPriority() : 4;
        this.symptoms = symptoms;
        this.assessedBy = assessedBy;
        this.assessorName = assessorName;
        this.assessedAt = assessedAt != null ? assessedAt : LocalDateTime.now();
        this.notes = notes;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public EmergencyLevel getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(EmergencyLevel emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
        if (emergencyLevel != null) {
            this.priority = emergencyLevel.getPriority();
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public int getAssessedBy() {
        return assessedBy;
    }

    public void setAssessedBy(int assessedBy) {
        this.assessedBy = assessedBy;
    }

    public String getAssessorName() {
        return assessorName;
    }

    public void setAssessorName(String assessorName) {
        this.assessorName = assessorName;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
