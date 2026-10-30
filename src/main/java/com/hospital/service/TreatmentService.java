package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientStatusHistoryDAO;
import com.hospital.dao.TreatmentDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.Permission;
import com.hospital.model.Treatment;
import com.hospital.security.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing medical treatment workflows for doctors with RBAC security validation.
 */
public class TreatmentService {

    private final TreatmentDAO treatmentDAO;
    private final PatientDAO patientDAO;
    private final PatientStatusHistoryDAO historyDAO;
    private final ActivityLogDAO activityLogDAO;
    private final PriorityQueueManager queueManager;

    public TreatmentService(PriorityQueueManager queueManager) {
        this.treatmentDAO = new TreatmentDAO();
        this.patientDAO = new PatientDAO();
        this.historyDAO = new PatientStatusHistoryDAO();
        this.activityLogDAO = new ActivityLogDAO();
        this.queueManager = queueManager;
    }

    public TreatmentService(TreatmentDAO treatmentDAO, PatientDAO patientDAO,
                            PatientStatusHistoryDAO historyDAO, ActivityLogDAO activityLogDAO,
                            PriorityQueueManager queueManager) {
        this.treatmentDAO = treatmentDAO;
        this.patientDAO = patientDAO;
        this.historyDAO = historyDAO;
        this.activityLogDAO = activityLogDAO;
        this.queueManager = queueManager;
    }

    /**
     * Starts clinical treatment for a patient (Doctor only).
     */
    public Treatment startTreatment(int patientId, int doctorId, String notes) throws InvalidPatientDataException {
        return startTreatment(patientId, doctorId, null, null, notes);
    }

    public Treatment startTreatment(int patientId, int doctorId, String diagnosis, String medications, String notes) throws InvalidPatientDataException {
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().requirePermission(Permission.START_TREATMENT);
        }

        Patient p = patientDAO.findById(patientId);
        if (p == null) {
            throw new InvalidPatientDataException("Patient with ID " + patientId + " not found.");
        }

        if (p.getCurrentStatus() != PatientStatus.WAITING && p.getCurrentStatus() != PatientStatus.CALLED) {
            throw new InvalidPatientDataException("Cannot start treatment: Patient is currently " +
                    p.getCurrentStatus().getDisplayName() + ".");
        }

        Treatment treatment = new Treatment();
        treatment.setPatientId(patientId);
        treatment.setDoctorId(doctorId);
        treatment.setStartTime(LocalDateTime.now());
        treatment.setTreatmentStatus("IN_PROGRESS");
        treatment.setDiagnosis(diagnosis);
        treatment.setMedications(medications);
        treatment.setNotes(notes != null ? notes : "Emergency clinical treatment started.");

        treatmentDAO.createTreatment(treatment);

        // Update patient status to IN_TREATMENT
        PatientStatus oldStatus = p.getCurrentStatus();
        patientDAO.updateStatus(patientId, PatientStatus.IN_TREATMENT, doctorId);

        // Record history
        historyDAO.recordStatusChange(patientId, oldStatus, PatientStatus.IN_TREATMENT, doctorId,
                "Treatment initiated. Notes: " + treatment.getNotes());

        // Log activity
        activityLogDAO.log(doctorId, "TREATMENT_STARTED", "TREATMENT", treatment.getTreatmentId(),
                "Started treatment for patient " + p.getFullName() + " (" + p.getPatientCode() + ").");

        // Remove from waiting queue
        if (queueManager != null) {
            queueManager.remove(patientId);
        }

        return treatment;
    }

    /**
     * Completes an in-progress treatment (Doctor only).
     */
    public boolean completeTreatment(int patientId, int doctorId, String completionNotes) throws InvalidPatientDataException {
        return completeTreatment(patientId, doctorId, null, null, completionNotes);
    }

    public boolean completeTreatment(int patientId, int doctorId, String diagnosis, String medications, String completionNotes) throws InvalidPatientDataException {
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().requirePermission(Permission.COMPLETE_TREATMENT);
        }

        Patient p = patientDAO.findById(patientId);
        if (p == null) {
            throw new InvalidPatientDataException("Patient with ID " + patientId + " not found.");
        }

        if (p.getCurrentStatus() != PatientStatus.IN_TREATMENT) {
            throw new InvalidPatientDataException("Cannot complete treatment: Patient is not currently in treatment.");
        }

        Treatment activeTreatment = treatmentDAO.getActiveTreatmentForPatient(patientId);
        if (activeTreatment != null) {
            activeTreatment.setEndTime(LocalDateTime.now());
            activeTreatment.setTreatmentStatus("COMPLETED");
            if (diagnosis != null && !diagnosis.isEmpty()) activeTreatment.setDiagnosis(diagnosis);
            if (medications != null && !medications.isEmpty()) activeTreatment.setMedications(medications);
            String existingNotes = activeTreatment.getNotes() != null ? activeTreatment.getNotes() : "";
            activeTreatment.setNotes(existingNotes + (completionNotes != null ? " [Completion: " + completionNotes + "]" : ""));
            treatmentDAO.updateTreatment(activeTreatment);
        }

        // Update patient status to TREATED
        patientDAO.updateStatus(patientId, PatientStatus.TREATED, doctorId);

        historyDAO.recordStatusChange(patientId, PatientStatus.IN_TREATMENT, PatientStatus.TREATED, doctorId,
                completionNotes != null ? completionNotes : "Clinical treatment course completed successfully.");

        activityLogDAO.log(doctorId, "TREATMENT_COMPLETED", "PATIENT", patientId,
                "Completed treatment for " + p.getFullName() + " (" + p.getPatientCode() + ").");

        return true;
    }

    /**
     * Discharges a treated or stabilized patient (Doctor only).
     */
    public boolean dischargePatient(int patientId, int doctorId, String dischargeSummary) throws InvalidPatientDataException {
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().requirePermission(Permission.DISCHARGE_PATIENT);
        }

        Patient p = patientDAO.findById(patientId);
        if (p == null) {
            throw new InvalidPatientDataException("Patient with ID " + patientId + " not found.");
        }

        if (p.getCurrentStatus() != PatientStatus.TREATED && p.getCurrentStatus() != PatientStatus.IN_TREATMENT) {
            throw new InvalidPatientDataException("Cannot discharge patient: Current status is " +
                    p.getCurrentStatus().getDisplayName() + " (must be TREATED or IN_TREATMENT).");
        }

        PatientStatus oldStatus = p.getCurrentStatus();

        // If in treatment, close active treatment
        if (oldStatus == PatientStatus.IN_TREATMENT) {
            Treatment activeTreatment = treatmentDAO.getActiveTreatmentForPatient(patientId);
            if (activeTreatment != null) {
                activeTreatment.setEndTime(LocalDateTime.now());
                activeTreatment.setTreatmentStatus("COMPLETED");
                activeTreatment.setNotes((activeTreatment.getNotes() != null ? activeTreatment.getNotes() : "") +
                        " [Discharged directly: " + dischargeSummary + "]");
                treatmentDAO.updateTreatment(activeTreatment);
            }
        }

        // Update patient status to DISCHARGED
        patientDAO.updateStatus(patientId, PatientStatus.DISCHARGED, doctorId);

        historyDAO.recordStatusChange(patientId, oldStatus, PatientStatus.DISCHARGED, doctorId,
                dischargeSummary != null ? dischargeSummary : "Patient discharged from emergency department.");

        activityLogDAO.log(doctorId, "PATIENT_DISCHARGED", "PATIENT", patientId,
                "Discharged patient " + p.getFullName() + " (" + p.getPatientCode() + "). Summary: " + dischargeSummary);

        // Remove from queue if present
        if (queueManager != null) {
            queueManager.remove(patientId);
        }

        return true;
    }

    public List<Treatment> getPatientTreatments(int patientId) {
        return treatmentDAO.getTreatmentsByPatientId(patientId);
    }
}
