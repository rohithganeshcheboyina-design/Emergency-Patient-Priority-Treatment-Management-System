package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.dao.EmergencyAssessmentDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientStatusHistoryDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing patient registration, vitals updates, and status workflow transitions.
 */
public class PatientService {

    private final PatientDAO patientDAO;
    private final EmergencyAssessmentDAO assessmentDAO;
    private final PatientStatusHistoryDAO historyDAO;
    private final ActivityLogDAO activityLogDAO;
    private final PriorityQueueManager queueManager;

    public PatientService(PriorityQueueManager queueManager) {
        this.patientDAO = new PatientDAO();
        this.assessmentDAO = new EmergencyAssessmentDAO();
        this.historyDAO = new PatientStatusHistoryDAO();
        this.activityLogDAO = new ActivityLogDAO();
        this.queueManager = queueManager;
    }

    public PatientService(PatientDAO patientDAO, EmergencyAssessmentDAO assessmentDAO,
                          PatientStatusHistoryDAO historyDAO, ActivityLogDAO activityLogDAO,
                          PriorityQueueManager queueManager) {
        this.patientDAO = patientDAO;
        this.assessmentDAO = assessmentDAO;
        this.historyDAO = historyDAO;
        this.activityLogDAO = activityLogDAO;
        this.queueManager = queueManager;
    }

    /**
     * Validates required patient fields.
     */
    public void validatePatient(Patient p) throws InvalidPatientDataException {
        if (p == null) {
            throw new InvalidPatientDataException("Patient object cannot be null.");
        }
        if (p.getFullName() == null || p.getFullName().trim().isEmpty()) {
            throw new InvalidPatientDataException("Patient full name is required.");
        }
        if (p.getAge() < 0 || p.getAge() > 130) {
            throw new InvalidPatientDataException("Age must be between 0 and 130.");
        }
        if (p.getGender() == null || p.getGender().trim().isEmpty()) {
            throw new InvalidPatientDataException("Gender must be selected.");
        }
        if (p.getPhone() == null || p.getPhone().trim().length() < 7) {
            throw new InvalidPatientDataException("A valid phone number (at least 7 digits) is required.");
        }
        if (p.getSymptoms() == null || p.getSymptoms().trim().isEmpty()) {
            throw new InvalidPatientDataException("Symptoms description is required.");
        }
        if (p.getEmergencyLevel() == null) {
            throw new InvalidPatientDataException("Emergency assessment level is required.");
        }
    }

    /**
     * Registers a new patient, saves assessment, status history, activity log,
     * and inserts the patient into the live priority queue.
     */
    public Patient registerPatient(Patient patient, int registeredByUserId) throws InvalidPatientDataException {
        // Enforce RBAC permission if logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().requirePermission(Permission.REGISTER_PATIENTS);
        }

        validatePatient(patient);

        if (patient.getArrivalTime() == null) {
            patient.setArrivalTime(LocalDateTime.now());
        }
        patient.setPriority(patient.getEmergencyLevel().getPriority());
        patient.setCurrentStatus(PatientStatus.WAITING);

        boolean created = patientDAO.createPatient(patient);
        if (!created) {
            throw new InvalidPatientDataException("Failed to register patient in database.");
        }

        // Record triage assessment
        EmergencyAssessment assessment = new EmergencyAssessment();
        assessment.setPatientId(patient.getPatientId());
        assessment.setEmergencyLevel(patient.getEmergencyLevel());
        assessment.setPriority(patient.getPriority());
        assessment.setSymptoms(patient.getSymptoms());
        assessment.setAssessedBy(registeredByUserId);
        assessment.setAssessedAt(LocalDateTime.now());
        assessment.setNotes("Initial triage registration assessment.");
        assessmentDAO.createAssessment(assessment);

        // Record status history
        historyDAO.recordStatusChange(patient.getPatientId(), null, PatientStatus.WAITING,
                registeredByUserId, "Patient registered at emergency triage counter.");

        // Record activity log
        activityLogDAO.log(registeredByUserId, "PATIENT_REGISTERED", "PATIENT", patient.getPatientId(),
                "Registered " + patient.getFullName() + " (" + patient.getPatientCode() +
                ") with priority " + patient.getEmergencyLevel() + ".");

        // Add to priority queue
        if (queueManager != null) {
            queueManager.enqueue(patient);
        }

        return patient;
    }

    /**
     * Updates vital signs, symptoms observation, and triage severity level.
     */
    public boolean updateVitals(int patientId, String bp, Integer hr, Double temp, Integer spo2, Integer rr,
                                String symptoms, EmergencyLevel level, int updatedByUserId, String notes) throws InvalidPatientDataException {
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().requirePermission(Permission.UPDATE_PATIENT_VITALS);
        }

        Patient p = patientDAO.findById(patientId);
        if (p == null) {
            throw new InvalidPatientDataException("Patient with ID " + patientId + " not found.");
        }

        boolean updated = patientDAO.updateVitals(patientId, bp, hr, temp, spo2, rr, symptoms, level);
        if (updated) {
            if (level != null && level != p.getEmergencyLevel()) {
                // Record new triage re-assessment
                EmergencyAssessment assessment = new EmergencyAssessment();
                assessment.setPatientId(patientId);
                assessment.setEmergencyLevel(level);
                assessment.setPriority(level.getPriority());
                assessment.setSymptoms(symptoms != null ? symptoms : p.getSymptoms());
                assessment.setAssessedBy(updatedByUserId);
                assessment.setAssessedAt(LocalDateTime.now());
                assessment.setNotes(notes != null ? notes : "Triage vitals update & severity reassessment.");
                assessmentDAO.createAssessment(assessment);

                // Update live priority queue dynamically
                if (queueManager != null && p.getCurrentStatus().isActiveInQueue()) {
                    queueManager.updatePriority(patientId, level);
                }
            }

            activityLogDAO.log(updatedByUserId, "VITALS_UPDATED", "PATIENT", patientId,
                    "Updated clinical vitals and triage notes for patient " + p.getFullName() + " (" + p.getPatientCode() + ").");
        }
        return updated;
    }

    /**
     * Updates an existing patient's demographic and triage data.
     */
    public boolean updatePatient(Patient patient, int updatedByUserId) throws InvalidPatientDataException {
        validatePatient(patient);
        patient.setPriority(patient.getEmergencyLevel().getPriority());

        boolean updated = patientDAO.updatePatient(patient);
        if (updated) {
            if (queueManager != null) {
                if (patient.getCurrentStatus().isActiveInQueue()) {
                    queueManager.enqueue(patient);
                } else {
                    queueManager.remove(patient.getPatientId());
                }
            }

            activityLogDAO.log(updatedByUserId, "PATIENT_UPDATED", "PATIENT", patient.getPatientId(),
                    "Updated patient details for " + patient.getFullName() + " (" + patient.getPatientCode() + ").");
        }
        return updated;
    }

    /**
     * Transitions a patient's status with state machine enforcement.
     */
    public boolean updateStatus(int patientId, PatientStatus newStatus, Integer doctorId,
                                int changedByUserId, String remarks) throws InvalidPatientDataException {
        Patient p = patientDAO.findById(patientId);
        if (p == null) {
            throw new InvalidPatientDataException("Patient with ID " + patientId + " not found.");
        }

        PatientStatus current = p.getCurrentStatus();
        if (!current.canTransitionTo(newStatus)) {
            throw new InvalidPatientDataException("Invalid status transition: Cannot change status from " +
                    current.getDisplayName() + " to " + newStatus.getDisplayName() + ".");
        }

        boolean success = patientDAO.updateStatus(patientId, newStatus, doctorId);
        if (success) {
            historyDAO.recordStatusChange(patientId, current, newStatus, changedByUserId, remarks);

            activityLogDAO.log(changedByUserId, "STATUS_CHANGED", "PATIENT", patientId,
                    "Status changed from " + current + " to " + newStatus + ". Remarks: " + remarks);

            // Update priority queue
            p.setCurrentStatus(newStatus);
            if (doctorId != null && doctorId > 0) {
                p.setAssignedDoctorId(doctorId);
            }
            if (queueManager != null) {
                if (newStatus.isActiveInQueue()) {
                    queueManager.enqueue(p);
                } else {
                    queueManager.remove(patientId);
                }
            }
        }
        return success;
    }

    public Patient getPatientById(int patientId) {
        return patientDAO.findById(patientId);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }
}
