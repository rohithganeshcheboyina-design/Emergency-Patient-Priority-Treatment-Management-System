package com.hospital;

import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.service.AppointmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Appointment & Outpatient Scheduling Unit Tests")
public class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        appointmentService = new AppointmentService();
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @AfterEach
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("Should retrieve upcoming and past scheduled appointments")
    public void testGetAppointments() {
        List<Appointment> all = appointmentService.getAllAppointments();
        assertNotNull(all);
        assertFalse(all.isEmpty(), "Sample appointments should exist in initial database");

        List<Appointment> patientAppts = appointmentService.getAppointmentsByPatient(1);
        assertNotNull(patientAppts);
    }

    @Test
    @DisplayName("Staff or Patient should be able to schedule and cancel outpatient consultation")
    public void testScheduleAndCancelAppointment() throws Exception {
        User doctor = new User(2, "doctor", "Dr. Vance", "vance@hospital.com", Role.DOCTOR);
        sessionManager.setCurrentUser(doctor);

        LocalDateTime apptTime = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        Appointment appt = new Appointment();
        appt.setPatientId(1);
        appt.setDoctorId(2);
        appt.setAppointmentTime(apptTime);
        appt.setSpecialty("Cardiology Follow-up");
        appt.setReason("Routine post-ER checkup");
        appt.setNotes("Patient instructed to bring ECG");

        Appointment scheduled = appointmentService.scheduleAppointment(appt);

        assertNotNull(scheduled);
        assertTrue(scheduled.getAppointmentId() > 0);
        assertEquals(AppointmentStatus.SCHEDULED, scheduled.getStatus());

        // Cancel appointment
        boolean cancelled = appointmentService.cancelAppointment(scheduled.getAppointmentId(), "Patient rescheduled");
        assertTrue(cancelled);
    }
}
