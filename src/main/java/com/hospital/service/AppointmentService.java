package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.Appointment;
import com.hospital.model.Patient;
import com.hospital.model.Permission;
import com.hospital.model.User;
import com.hospital.security.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing non-emergency outpatient appointments.
 * Strict triage protocol: Emergency triage patients are always prioritized over appointments.
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final ActivityLogDAO activityLogDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public AppointmentService(AppointmentDAO appointmentDAO, PatientDAO patientDAO, ActivityLogDAO activityLogDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.activityLogDAO = activityLogDAO;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    public List<Appointment> getUpcomingAppointments() {
        return appointmentDAO.getUpcomingAppointments();
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return appointmentDAO.getAppointmentsByPatient(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return appointmentDAO.getAppointmentsByDoctor(doctorId);
    }

    public Appointment scheduleAppointment(Appointment appt) throws InvalidPatientDataException {
        if (appt == null) {
            throw new InvalidPatientDataException("Appointment details cannot be null.");
        }
        if (appt.getPatientId() <= 0) {
            throw new InvalidPatientDataException("Valid patient is required for appointment.");
        }
        if (appt.getDoctorId() <= 0) {
            throw new InvalidPatientDataException("Assigned physician is required.");
        }
        if (appt.getAppointmentTime() == null || appt.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new InvalidPatientDataException("Appointment time must be in the future.");
        }

        boolean created = appointmentDAO.createAppointment(appt);
        if (!created) {
            throw new InvalidPatientDataException("Failed to persist appointment in database.");
        }

        User staff = SessionManager.getInstance().getCurrentUser();
        int staffId = staff != null ? staff.getUserId() : 1;
        activityLogDAO.log(staffId, "APPOINTMENT_SCHEDULED", "APPOINTMENT", appt.getAppointmentId(),
                "Scheduled appointment for patient ID " + appt.getPatientId() + " with doctor ID " + appt.getDoctorId());

        return appt;
    }

    public boolean cancelAppointment(int appointmentId, String reason) {
        boolean cancelled = appointmentDAO.cancelAppointment(appointmentId, reason);
        if (cancelled) {
            User staff = SessionManager.getInstance().getCurrentUser();
            int staffId = staff != null ? staff.getUserId() : 1;
            activityLogDAO.log(staffId, "APPOINTMENT_CANCELLED", "APPOINTMENT", appointmentId,
                    "Cancelled appointment ID " + appointmentId + ". Reason: " + reason);
        }
        return cancelled;
    }
}
