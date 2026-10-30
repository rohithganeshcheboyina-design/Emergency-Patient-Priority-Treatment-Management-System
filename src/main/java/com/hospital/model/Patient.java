package com.hospital.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Patient entity representing an emergency department admission with complete triage demographics and vitals.
 */
public class Patient {
    private int patientId;
    private String patientCode; // e.g. P0001
    private String tokenNumber; // e.g. ER-001
    private String fullName;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String bloodGroup;
    private String allergies;
    private String emergencyContact;

    // Vital Signs
    private String vitalBp; // e.g. 120/80 mmHg
    private Integer vitalHeartRate; // bpm
    private Double vitalTemperature; // °F or °C
    private Integer vitalSpo2; // %
    private Integer vitalRespiratoryRate; // breaths/min

    private String symptoms;
    private EmergencyLevel emergencyLevel;
    private int priority;
    private LocalDateTime arrivalTime;
    private Integer assignedDoctorId;
    private String assignedDoctorName;
    private PatientStatus currentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Patient() {
        this.arrivalTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentStatus = PatientStatus.WAITING;
        this.emergencyLevel = EmergencyLevel.MEDIUM;
        this.priority = this.emergencyLevel.getPriority();
    }

    public Patient(int patientId, String patientCode, String tokenNumber, String fullName,
                   int age, String gender, String phone, String address, String bloodGroup,
                   String symptoms, EmergencyLevel emergencyLevel, LocalDateTime arrivalTime,
                   Integer assignedDoctorId, PatientStatus currentStatus) {
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.tokenNumber = tokenNumber;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.symptoms = symptoms;
        this.emergencyLevel = emergencyLevel != null ? emergencyLevel : EmergencyLevel.MEDIUM;
        this.priority = this.emergencyLevel.getPriority();
        this.arrivalTime = arrivalTime != null ? arrivalTime : LocalDateTime.now();
        this.assignedDoctorId = assignedDoctorId;
        this.currentStatus = currentStatus != null ? currentStatus : PatientStatus.WAITING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getVitalBp() {
        return vitalBp;
    }

    public void setVitalBp(String vitalBp) {
        this.vitalBp = vitalBp;
    }

    public Integer getVitalHeartRate() {
        return vitalHeartRate;
    }

    public void setVitalHeartRate(Integer vitalHeartRate) {
        this.vitalHeartRate = vitalHeartRate;
    }

    public Double getVitalTemperature() {
        return vitalTemperature;
    }

    public void setVitalTemperature(Double vitalTemperature) {
        this.vitalTemperature = vitalTemperature;
    }

    public Integer getVitalSpo2() {
        return vitalSpo2;
    }

    public void setVitalSpo2(Integer vitalSpo2) {
        this.vitalSpo2 = vitalSpo2;
    }

    public Integer getVitalRespiratoryRate() {
        return vitalRespiratoryRate;
    }

    public void setVitalRespiratoryRate(Integer vitalRespiratoryRate) {
        this.vitalRespiratoryRate = vitalRespiratoryRate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
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

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(Integer assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public void setAssignedDoctorName(String assignedDoctorName) {
        this.assignedDoctorName = assignedDoctorName;
    }

    public PatientStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(PatientStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Calculates elapsed waiting time in minutes from arrival time to now.
     */
    public long getWaitingTimeMinutes() {
        if (arrivalTime == null) return 0;
        return Math.max(0, Duration.between(arrivalTime, LocalDateTime.now()).toMinutes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return patientId == patient.patientId && Objects.equals(patientCode, patient.patientCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, patientCode);
    }

    @Override
    public String toString() {
        return "[" + (patientCode != null ? patientCode : "NEW") + "] " + fullName + " (" + emergencyLevel + ")";
    }
}
