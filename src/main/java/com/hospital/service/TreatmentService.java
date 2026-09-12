package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientStatusHistoryDAO;
import com.hospital.dao.TreatmentDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.Treatment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing medical treatment workflows for doctors.
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

    /**
     * Starts treatment for a patient:
     * - Creates a Treatment record with status IN_PROGRESS.
     * - Updates patient status to IN_TREATMENT and assigns doctor.
     * - Evicts patient from the waiting priority queue.
     * - Records status history and audit log.
     */
    public Treatment startTreatment(int patientId, int doctorId, String notes) throws InvalidPatientDataException {
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
     * Completes an in-progress treatment:
     * - Updates Treatment record with endTime and status COMPLETED.
     * - Updates patient status to TREATED.
     * - Records status history and audit log.
     */
    public boolean completeTreatment(int patientId, int doctorId, String completionNotes) throws InvalidPatientDataException {
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
            if (completionNotes != null && !completionNotes.trim().isEmpty()) {
                String existing = activeTreatment.getNotes() != null ? activeTreatment.getNotes() + " | " : "";
                activeTreatment.setNotes(existing + "Completion: " + completionNotes);
            }
            treatmentDAO.updateTreatment(activeTreatment);
        }

        PatientStatus oldStatus = p.getCurrentStatus();
        patientDAO.updateStatus(patientId, PatientStatus.TREATED, doctorId);

        historyDAO.recordStatusChange(patientId, oldStatus, PatientStatus.TREATED, doctorId,
                "Treatment completed. Notes: " + completionNotes);

        activityLogDAO.log(doctorId, "TREATMENT_COMPLETED", "PATIENT", patientId,
                "Completed treatment for patient " + p.getFullName() + " (" + p.getPatientCode() + ").");

        return true;
    }

    /**
     * Discharges a treated patient:
     * - Updates patient status to DISCHARGED.
     * - Records status history and audit log.
     */
    public boolean dischargePatient(int patientId, int doctorId, String dischargeNotes) throws InvalidPatientDataException {
        Patient p = patientDAO.findById(patientId);
        if (p == null) {
            throw new InvalidPatientDataException("Patient with ID " + patientId + " not found.");
        }

        if (p.getCurrentStatus() != PatientStatus.TREATED && p.getCurrentStatus() != PatientStatus.IN_TREATMENT) {
            throw new InvalidPatientDataException("Cannot discharge: Patient must be TREATED or evaluated first.");
        }

        PatientStatus oldStatus = p.getCurrentStatus();
        patientDAO.updateStatus(patientId, PatientStatus.DISCHARGED, doctorId);

        historyDAO.recordStatusChange(patientId, oldStatus, PatientStatus.DISCHARGED, doctorId,
                "Patient discharged. Summary: " + dischargeNotes);

        activityLogDAO.log(doctorId, "PATIENT_DISCHARGED", "PATIENT", patientId,
                "Discharged patient " + p.getFullName() + " (" + p.getPatientCode() + ").");

        if (queueManager != null) {
            queueManager.remove(patientId);
        }

        return true;
    }

    public Treatment getActiveTreatment(int patientId) {
        return treatmentDAO.getActiveTreatmentForPatient(patientId);
    }

    public List<Treatment> getTreatmentsForPatient(int patientId) {
        return treatmentDAO.getTreatmentsByPatientId(patientId);
    }
}
