package com.hospital;

import com.hospital.dao.DatabaseConnection;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.UserDAO;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import com.hospital.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Complete Real-World Hospital Workflow Tests")
public class EndToEndHospitalWorkflowTest {

    private static PriorityQueueManager queueManager;
    private static PriorityQueueService queueService;
    private static PatientService patientService;
    private static TreatmentService treatmentService;
    private static BedService bedService;
    private static AppointmentService appointmentService;
    private static ReportService reportService;
    private static PatientDAO patientDAO;

    @BeforeAll
    public static void init() throws Exception {
        DatabaseConnection.getConnection();
        queueManager = new PriorityQueueManager();
        queueService = new PriorityQueueService(queueManager);
        patientService = new PatientService(queueManager);
        treatmentService = new TreatmentService(queueManager);
        bedService = new BedService();
        appointmentService = new AppointmentService();
        reportService = new ReportService();
        patientDAO = new PatientDAO();
    }

    @AfterEach
    public void cleanup() {
        SessionManager.getInstance().clearSession();
    }

    @Test
    @DisplayName("Complete Clinical Lifecycle: Registration -> Queue -> Vitals Update -> Bed Allocation -> Treatment -> Discharge -> Outpatient Appointment -> CSV Audit")
    public void testCompletePatientClinicalJourney() throws Exception {
        // =========================================================================
        // STEP 1: NURSE INTAKE & EMERGENCY REGISTRATION
        // =========================================================================
        Nurse nurse = new Nurse(4, "nurse", "hash", "Nurse Clara Barton", "555-4444", "nurse@hospital.com",
                true, null, "Day Shift", "Triage Station 1");
        SessionManager.getInstance().setCurrentUser(nurse);

        Patient patient = new Patient();
        patient.setPatientCode(patientDAO.getNextPatientCode());
        patient.setTokenNumber(patientDAO.getNextTokenNumber());
        patient.setFullName("E2E Test Patient Alpha");
        patient.setAge(45);
        patient.setGender("Male");
        patient.setPhone("555-0199");
        patient.setBloodGroup("O+");
        patient.setAllergies("Penicillin");
        patient.setEmergencyContact("Jane Alpha - 555-0198");
        patient.setSymptoms("Substernal crushing chest pain, radiating to left arm, diaphoresis");
        patient.setEmergencyLevel(EmergencyLevel.HIGH); // Initially registered as HIGH (P2)
        patient.setPriority(EmergencyLevel.HIGH.getPriority());
        patient.setArrivalTime(LocalDateTime.now());
        patient.setCurrentStatus(PatientStatus.WAITING);

        Patient registered = patientService.registerPatient(patient, nurse.getUserId());
        assertNotNull(registered, "Patient registration by nurse must succeed");
        assertTrue(patient.getPatientId() > 0, "Patient ID must be generated");

        // Verify patient is in active queue
        List<QueueItem> liveQueue = queueManager.getAllInOrder();
        assertTrue(liveQueue.stream().anyMatch(q -> q.getPatientId() == patient.getPatientId()),
                "Patient must be present in live emergency queue");

        // =========================================================================
        // STEP 2: VITALS DETERIORATION & DYNAMIC RE-TRIAGE
        // =========================================================================
        // Patient's vitals worsen: SpO2 drops to 87%, BP drops to 85/55 -> Reclassified to CRITICAL (P1)
        patientService.updateVitals(
                patient.getPatientId(),
                "85/55",
                132,
                99.1,
                87,
                28,
                "Acute cardiac decompensation, hypotensive, cyanotic. Upgraded to CRITICAL.",
                EmergencyLevel.CRITICAL,
                nurse.getUserId(),
                "Triage re-evaluation"
        );

        // Verify DB update
        Patient updatedFromDb = patientDAO.findById(patient.getPatientId());
        assertEquals(EmergencyLevel.CRITICAL, updatedFromDb.getEmergencyLevel());
        assertEquals("85/55", updatedFromDb.getVitalBp());
        assertEquals(87, updatedFromDb.getVitalSpo2());

        // Verify queue priority re-sort: patient should advance towards the head of queue
        QueueItem itemInQueue = queueManager.getAllInOrder().stream()
                .filter(q -> q.getPatientId() == patient.getPatientId())
                .findFirst()
                .orElse(null);
        assertNotNull(itemInQueue);
        assertEquals(EmergencyLevel.CRITICAL, itemInQueue.getEmergencyLevel());

        // =========================================================================
        // STEP 3: BED MANAGEMENT (ICU ALLOCATION)
        // =========================================================================
        // Find an available ICU bed
        List<Bed> allBeds = bedService.getAllBeds();
        Bed icuBed = allBeds.stream()
                .filter(b -> b.getBedType() == BedType.ICU && b.getStatus() == BedStatus.AVAILABLE)
                .findFirst()
                .orElse(null);

        assertNotNull(icuBed, "An available ICU bed must exist in the system");
        int bedId = icuBed.getBedId();

        boolean bedAssigned = bedService.assignBed(bedId, patient.getPatientId(), "Urgent ICU Admission");
        assertTrue(bedAssigned, "Nurse must be able to assign ICU bed to critical patient");

        // Verify bed status is now OCCUPIED by this patient
        Bed assignedBed = bedService.getAllBeds().stream().filter(b -> b.getBedId() == bedId).findFirst().orElse(null);
        assertNotNull(assignedBed);
        assertEquals(BedStatus.OCCUPIED, assignedBed.getStatus());
        assertEquals(patient.getPatientId(), assignedBed.getAssignedPatientId());

        // =========================================================================
        // STEP 4: DOCTOR CLINICAL INTAKE & TREATMENT
        // =========================================================================
        Doctor doctor = new Doctor(2, "doctor", "hash", "Dr. Robert Vance, MD", "555-2222", "vance@hospital.com",
                true, null, "Emergency MD", "Emergency Medicine");
        SessionManager.getInstance().setCurrentUser(doctor);

        // Doctor starts treatment
        Treatment treatment = treatmentService.startTreatment(patient.getPatientId(), doctor.getUserId(),
                "Started emergency thrombolytic protocol and central line");
        assertNotNull(treatment, "Doctor must be able to start treatment");

        // Status is now IN_TREATMENT and patient is removed from waiting queue
        Patient inTrPatient = patientDAO.findById(patient.getPatientId());
        assertEquals(PatientStatus.IN_TREATMENT, inTrPatient.getCurrentStatus());
        assertFalse(queueManager.getAllInOrder().stream().anyMatch(q -> q.getPatientId() == patient.getPatientId()),
                "In-treatment patient must be removed from the active waiting queue");

        // Doctor completes treatment with diagnosis and medications
        boolean completedTr = treatmentService.completeTreatment(
                patient.getPatientId(),
                doctor.getUserId(),
                "ST-Elevation Myocardial Infarction (Anterior Wall)",
                "Aspirin 325mg PO, Clopidogrel 300mg PO, Atorvastatin 80mg, Heparin drip",
                "STEMI successfully reperfused. Vitals stabilized."
        );
        assertTrue(completedTr, "Doctor must be able to complete treatment with diagnosis and medication");

        Patient treatedPatient = patientDAO.findById(patient.getPatientId());
        assertEquals(PatientStatus.TREATED, treatedPatient.getCurrentStatus());

        // Doctor discharges patient
        boolean discharged = treatmentService.dischargePatient(patient.getPatientId(), doctor.getUserId(),
                "Hemodynamically stable. Discharge to step-down ward with outpatient follow-up.");
        assertTrue(discharged, "Doctor must be able to discharge patient");

        Patient dischargedPatient = patientDAO.findById(patient.getPatientId());
        assertEquals(PatientStatus.DISCHARGED, dischargedPatient.getCurrentStatus());

        // =========================================================================
        // STEP 5: BED RELEASE & SANITIZATION
        // =========================================================================
        SessionManager.getInstance().setCurrentUser(nurse);
        boolean bedReleased = bedService.releaseBed(bedId);
        assertTrue(bedReleased, "Bed must be released upon patient discharge");

        Bed releasedBed = bedService.getAllBeds().stream().filter(b -> b.getBedId() == bedId).findFirst().orElse(null);
        assertNotNull(releasedBed);
        assertEquals(BedStatus.AVAILABLE, releasedBed.getStatus(), "Released bed must return to AVAILABLE pool");

        // =========================================================================
        // STEP 6: PATIENT PORTAL & OUTPATIENT APPOINTMENT SCHEDULING
        // =========================================================================
        User dbPatient = new UserDAO().findByUsername("patient");
        int patientUserId = dbPatient != null ? dbPatient.getUserId() : 6;
        PatientUser patientUser = new PatientUser(patientUserId, "patient", "hash", "E2E Test Patient Alpha", "555-0199",
                "alpha@patient.com", true, null, patient.getPatientCode());
        SessionManager.getInstance().setCurrentUser(patientUser);

        Appointment appt = new Appointment();
        appt.setPatientId(patient.getPatientId());
        appt.setDoctorId(doctor.getUserId());
        appt.setAppointmentTime(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
        appt.setSpecialty("Cardiology Follow-Up");
        appt.setReason("Post-MI Clinical Review");
        appt.setNotes("Scheduled from patient self-service portal");

        Appointment scheduledAppt = appointmentService.scheduleAppointment(appt);
        assertNotNull(scheduledAppt, "Patient must be able to schedule an outpatient consultation");
        assertTrue(scheduledAppt.getAppointmentId() > 0);

        // Verify patient can cancel the appointment if needed
        boolean apptCancelled = appointmentService.cancelAppointment(scheduledAppt.getAppointmentId(), "Patient rescheduled");
        assertTrue(apptCancelled, "Patient must be able to cancel scheduled appointment");

        // =========================================================================
        // STEP 7: ADMIN REPORTING & CSV EXPORT AUDIT
        // =========================================================================
        User admin = new User(1, "admin", "Admin System", "admin@hospital.com", Role.ADMIN);
        SessionManager.getInstance().setCurrentUser(admin);

        Map<String, Integer> summary = reportService.getDashboardSummary();
        assertNotNull(summary);
        assertTrue(summary.containsKey("total"));
        assertTrue(summary.containsKey("discharged"));
        assertTrue(summary.get("total") > 0);

        // Export CSV report to a temporary file
        File tempCsv = File.createTempFile("hospital_report_test_", ".csv");
        tempCsv.deleteOnExit();

        assertDoesNotThrow(() -> {
            reportService.exportPatientsToCSV(tempCsv);
        });

        assertTrue(tempCsv.exists() && tempCsv.length() > 0, "Exported CSV file must exist and contain data");
    }
}
