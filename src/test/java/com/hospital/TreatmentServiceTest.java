package com.hospital;

import com.hospital.exception.InvalidPatientDataException;
import com.hospital.exception.UnauthorizedException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import com.hospital.service.PatientService;
import com.hospital.service.PriorityQueueManager;
import com.hospital.service.TreatmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Clinical Emergency Treatment Lifecycle Unit Tests")
public class TreatmentServiceTest {

    private TreatmentService treatmentService;
    private PatientService patientService;
    private PriorityQueueManager queueManager;
    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        queueManager = new PriorityQueueManager();
        treatmentService = new TreatmentService(queueManager);
        patientService = new PatientService(queueManager);
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @AfterEach
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("Doctor should be able to start, document, and complete treatment")
    public void testDoctorTreatmentWorkflow() throws InvalidPatientDataException {
        // 1. Nurse registers a new emergency patient at intake
        User nurse = new User(4, "nurse", "Nurse Lisa", "lisa@hospital.com", Role.NURSE);
        sessionManager.setCurrentUser(nurse);

        Patient p = new Patient();
        p.setFullName("Emergency Trauma Test Patient");
        p.setAge(42);
        p.setGender("Male");
        p.setPhone("555-99887");
        p.setEmergencyLevel(EmergencyLevel.HIGH);
        p.setPriority(2);
        p.setSymptoms("Severe abdominal trauma");
        p.setCurrentStatus(PatientStatus.WAITING);

        Patient registered = patientService.registerPatient(p, 4);
        assertNotNull(registered);
        int patientId = registered.getPatientId();

        // 2. Doctor assumes clinical responsibility
        User doctor = new User(2, "doctor", "Dr. Vance", "vance@hospital.com", Role.DOCTOR);
        sessionManager.setCurrentUser(doctor);

        // 1. Doctor starts treatment with initial diagnosis and orders
        Treatment started = treatmentService.startTreatment(
                patientId, 2, "Blunt Abdominal Trauma", "Morphine 4mg IV, IV fluids", "Patient placed on continuous monitoring."
        );
        assertNotNull(started);
        assertEquals("IN_PROGRESS", started.getTreatmentStatus());
        assertEquals("Blunt Abdominal Trauma", started.getDiagnosis());

        // Verify patient status promoted to IN_TREATMENT
        Patient current = patientService.getPatientById(patientId);
        assertEquals(PatientStatus.IN_TREATMENT, current.getCurrentStatus());

        // 2. Doctor completes treatment
        boolean completed = treatmentService.completeTreatment(
                patientId, 2, "Blunt Abdominal Trauma (Stabilized)", "Discontinue IV", "Vitals stabilized. Ready for observation."
        );
        assertTrue(completed);

        current = patientService.getPatientById(patientId);
        assertEquals(PatientStatus.TREATED, current.getCurrentStatus());

        // 3. Doctor discharges patient
        boolean discharged = treatmentService.dischargePatient(patientId, 2, "Discharged with oral analgesics and follow-up plan.");
        assertTrue(discharged);

        current = patientService.getPatientById(patientId);
        assertEquals(PatientStatus.DISCHARGED, current.getCurrentStatus());
    }

    @Test
    @DisplayName("Admin or Nurse should be restricted from initiating clinical treatment")
    public void testNonDoctorTreatmentRestriction() throws InvalidPatientDataException {
        User admin = new User(1, "admin", "Admin", "admin@hospital.com", Role.ADMIN);
        sessionManager.setCurrentUser(admin);

        assertThrows(UnauthorizedException.class, () ->
                treatmentService.startTreatment(1, 1, "Diagnosis", "Meds", "Notes"));

        User nurse = new User(4, "nurse", "Nurse", "nurse@hospital.com", Role.NURSE);
        sessionManager.setCurrentUser(nurse);

        assertThrows(UnauthorizedException.class, () ->
                treatmentService.startTreatment(1, 4, "Diagnosis", "Meds", "Notes"));
    }
}
