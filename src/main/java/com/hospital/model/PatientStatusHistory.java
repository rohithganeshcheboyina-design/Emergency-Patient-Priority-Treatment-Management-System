package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Historical record of a patient status change for audit trails and timeline view.
 */
public class PatientStatusHistory {
    private int historyId;
    private int patientId;
    private PatientStatus oldStatus;
    private PatientStatus newStatus;
    private int changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
    private String remarks;

    public PatientStatusHistory() {
        this.changedAt = LocalDateTime.now();
    }

    public PatientStatusHistory(int historyId, int patientId, PatientStatus oldStatus,
                                PatientStatus newStatus, int changedBy, String changedByName,
                                LocalDateTime changedAt, String remarks) {
        this.historyId = historyId;
        this.patientId = patientId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedByName = changedByName;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
        this.remarks = remarks;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public PatientStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(PatientStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public PatientStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PatientStatus newStatus) {
        this.newStatus = newStatus;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
