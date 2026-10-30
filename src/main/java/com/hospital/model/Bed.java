package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Bed entity representing emergency room and ward bed allocations.
 */
public class Bed {
    private int bedId;
    private String bedNumber;
    private BedType bedType;
    private BedStatus status;
    private Integer assignedPatientId;
    private String assignedPatientName;
    private String assignedPatientCode;
    private LocalDateTime assignedAt;
    private String notes;

    public Bed() {
        this.status = BedStatus.AVAILABLE;
        this.bedType = BedType.EMERGENCY;
    }

    public Bed(int bedId, String bedNumber, BedType bedType, BedStatus status,
               Integer assignedPatientId, String assignedPatientName, String assignedPatientCode,
               LocalDateTime assignedAt, String notes) {
        this.bedId = bedId;
        this.bedNumber = bedNumber;
        this.bedType = bedType != null ? bedType : BedType.EMERGENCY;
        this.status = status != null ? status : BedStatus.AVAILABLE;
        this.assignedPatientId = assignedPatientId;
        this.assignedPatientName = assignedPatientName;
        this.assignedPatientCode = assignedPatientCode;
        this.assignedAt = assignedAt;
        this.notes = notes;
    }

    public int getBedId() {
        return bedId;
    }

    public void setBedId(int bedId) {
        this.bedId = bedId;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public BedType getBedType() {
        return bedType;
    }

    public void setBedType(BedType bedType) {
        this.bedType = bedType;
    }

    public BedStatus getStatus() {
        return status;
    }

    public void setStatus(BedStatus status) {
        this.status = status;
    }

    public Integer getAssignedPatientId() {
        return assignedPatientId;
    }

    public void setAssignedPatientId(Integer assignedPatientId) {
        this.assignedPatientId = assignedPatientId;
    }

    public String getAssignedPatientName() {
        return assignedPatientName;
    }

    public void setAssignedPatientName(String assignedPatientName) {
        this.assignedPatientName = assignedPatientName;
    }

    public String getAssignedPatientCode() {
        return assignedPatientCode;
    }

    public void setAssignedPatientCode(String assignedPatientCode) {
        this.assignedPatientCode = assignedPatientCode;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return bedNumber + " (" + bedType.getDisplayName() + ") - " + status.getDisplayName();
    }
}
