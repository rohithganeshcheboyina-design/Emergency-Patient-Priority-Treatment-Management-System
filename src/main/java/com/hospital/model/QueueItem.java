package com.hospital.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates a patient entry in the Emergency Priority Queue.
 * Implements strict FIFO-within-priority ordering:
 * 1. Priority level (1 = Critical, 2 = High, 3 = Medium, 4 = Low)
 * 2. Arrival timestamp (earlier arrival first)
 * 3. Stable tie-breaker (patientId)
 */
public class QueueItem implements Comparable<QueueItem> {
    private int queuePosition;
    private Patient patient;

    public QueueItem(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null in QueueItem");
        }
        this.patient = patient;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public int getPatientId() {
        return patient.getPatientId();
    }

    public String getPatientCode() {
        return patient.getPatientCode();
    }

    public String getTokenNumber() {
        return patient.getTokenNumber();
    }

    public String getPatientName() {
        return patient.getFullName();
    }

    public int getAge() {
        return patient.getAge();
    }

    public String getGender() {
        return patient.getGender();
    }

    public String getSymptoms() {
        return patient.getSymptoms();
    }

    public EmergencyLevel getEmergencyLevel() {
        return patient.getEmergencyLevel();
    }

    public int getPriority() {
        return patient.getPriority();
    }

    public LocalDateTime getArrivalTime() {
        return patient.getArrivalTime();
    }

    public PatientStatus getStatus() {
        return patient.getCurrentStatus();
    }

    public String getAssignedDoctorName() {
        return patient.getAssignedDoctorName();
    }

    public Integer getAssignedDoctorId() {
        return patient.getAssignedDoctorId();
    }

    public long getWaitingTimeMinutes() {
        if (patient.getArrivalTime() == null) return 0;
        return Math.max(0, Duration.between(patient.getArrivalTime(), LocalDateTime.now()).toMinutes());
    }

    @Override
    public int compareTo(QueueItem other) {
        if (other == null) return -1;
        if (this == other) return 0;

        // Primary: Lower priority number comes first (1 = CRITICAL < 2 = HIGH < 3 = MEDIUM < 4 = LOW)
        int priorityComp = Integer.compare(this.getPriority(), other.getPriority());
        if (priorityComp != 0) {
            return priorityComp;
        }

        // Secondary: Earlier arrival time comes first
        if (this.getArrivalTime() != null && other.getArrivalTime() != null) {
            int timeComp = this.getArrivalTime().compareTo(other.getArrivalTime());
            if (timeComp != 0) {
                return timeComp;
            }
        } else if (this.getArrivalTime() != null) {
            return -1;
        } else if (other.getArrivalTime() != null) {
            return 1;
        }

        // Stable tie-breaker by patient ID
        return Integer.compare(this.getPatientId(), other.getPatientId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueItem queueItem = (QueueItem) o;
        return Objects.equals(patient, queueItem.patient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patient);
    }

    @Override
    public String toString() {
        return "#" + queuePosition + " [" + getTokenNumber() + "] " + getPatientName() +
                " (" + getEmergencyLevel() + ", Priority " + getPriority() + ") - Wait: " +
                getWaitingTimeMinutes() + "m";
    }
}
