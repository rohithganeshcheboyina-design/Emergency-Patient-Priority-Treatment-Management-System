package com.hospital;

import com.hospital.dao.DatabaseConnection;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.UserDAO;
import com.hospital.exception.UnauthorizedException;
import com.hospital.model.*;
import com.hospital.security.PasswordUtil;
import com.hospital.security.SessionManager;
import com.hospital.service.AuthenticationService;
import com.hospital.service.PatientService;
import com.hospital.service.PriorityQueueManager;
import com.hospital.service.TreatmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security, Injection & Privilege Escalation Audit Tests")
public class SecurityAndInjectionTest {

    private static PatientDAO patientDAO;
    private static UserDAO userDAO;
    private static AuthenticationService authService;
    private static PatientService patientService;
    private static TreatmentService treatmentService;
    private static PriorityQueueManager queueManager;

    @BeforeAll
    public static void init() throws Exception {
        DatabaseConnection.getConnection();
        patientDAO = new PatientDAO();
        userDAO = new UserDAO();
        authService = new AuthenticationService();
        queueManager = new PriorityQueueManager();
        patientService = new PatientService(queueManager);
        treatmentService = new TreatmentService(queueManager);
    }

    @AfterEach
    public void cleanSession() {
        SessionManager.getInstance().clearSession();
    }

    @Test
    @DisplayName("SQL Injection Resistance: Search queries must be safe against SQL payload injection")
    public void testSqlInjectionInPatientSearch() {
        // Typical SQL injection patterns
        String[] payloads = {
                "' OR '1'='1",
                "' OR 1=1 --",
                "admin' --",
                "\"; DROP TABLE patients; --",
                "1' UNION SELECT * FROM users --",
                "' OR ''='"
        };

        for (String payload : payloads) {
            // PatientDAO search should safely treat injection string as literal text without database error
            assertDoesNotThrow(() -> {
                List<Patient> result = patientDAO.searchPatients(payload);
                assertNotNull(result);
            }, "Query with payload [" + payload + "] must execute safely via PreparedStatement parameterization");
        }
    }

    @Test
    @DisplayName("SQL Injection Resistance: Username lookup must parameterize input safely")
    public void testSqlInjectionInUserLookup() {
        String payload = "admin' OR '1'='1";
        User user = userDAO.findByUsername(payload);
        assertNull(user, "Malicious payload must not match any user");
    }

    @Test
    @DisplayName("Password Security: BCrypt salted hashing and timing-safe verification")
    public void testPasswordHashingSecurity() {
        String raw = "ClinicalPassword2026!";
        String hash1 = PasswordUtil.hashPassword(raw);
        String hash2 = PasswordUtil.hashPassword(raw);

        // Hashing the same password twice must produce different hashes due to unique cryptographic salts
        assertNotEquals(hash1, hash2, "BCrypt must generate unique salts for each hash");

        // Both hashes must verify correctly
        assertTrue(PasswordUtil.verifyPassword(raw, hash1));
        assertTrue(PasswordUtil.verifyPassword(raw, hash2));

        // Wrong password must fail
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash1));
        assertFalse(PasswordUtil.verifyPassword("", hash1));
        assertFalse(PasswordUtil.verifyPassword(null, hash1));
    }

    @Test
    @DisplayName("Privilege Escalation Protection: Nurse cannot start or complete treatments")
    public void testNurseCannotExecuteTreatment() {
        Nurse nurse = new Nurse(4, "nurse", "hash", "Nurse Staff", "555-1111", "nurse@hospital.com",
                true, null, "General", "Triage 1");
        SessionManager.getInstance().setCurrentUser(nurse);

        assertThrows(UnauthorizedException.class, () -> {
            treatmentService.startTreatment(1, 4, "Unauthorized nurse attempt");
        }, "Nurse must not be allowed to start medical treatments");

        assertThrows(UnauthorizedException.class, () -> {
            treatmentService.completeTreatment(1, 4, "Unauthorized nurse attempt");
        }, "Nurse must not be allowed to complete medical treatments");

        assertThrows(UnauthorizedException.class, () -> {
            treatmentService.dischargePatient(1, 4, "Unauthorized nurse attempt");
        }, "Nurse must not be allowed to discharge patients");
    }

    @Test
    @DisplayName("Privilege Escalation Protection: Doctor cannot manage system users")
    public void testDoctorCannotManageUsers() {
        Doctor doctor = new Doctor(2, "doctor", "hash", "Dr. Robert", "555-2222", "doc@hospital.com",
                true, null, "Emergency MD", "Emergency");
        SessionManager.getInstance().setCurrentUser(doctor);

        assertThrows(UnauthorizedException.class, () -> {
            SessionManager.getInstance().requirePermission(Permission.MANAGE_USERS);
        });

        assertThrows(UnauthorizedException.class, () -> {
            SessionManager.getInstance().requirePermission(Permission.MANAGE_SETTINGS);
        });
    }

    @Test
    @DisplayName("Privilege Escalation Protection: Admin cannot register patients or administer treatments")
    public void testAdminCannotAdministerClinicalTreatmentsOrRegister() {
        User admin = new User(1, "admin", "Admin System", "admin@hospital.com", Role.ADMIN);
        SessionManager.getInstance().setCurrentUser(admin);

        assertThrows(UnauthorizedException.class, () -> {
            Patient p = new Patient();
            p.setFullName("Test Patient");
            p.setAge(30);
            p.setGender("Male");
            p.setPhone("555-1234");
            p.setSymptoms("Flu");
            p.setEmergencyLevel(EmergencyLevel.LOW);
            patientService.registerPatient(p, 1);
        }, "Admin is explicitly restricted from patient intake registration");

        assertThrows(UnauthorizedException.class, () -> {
            treatmentService.startTreatment(1, 1, "Admin treatment attempt");
        }, "Admin is explicitly restricted from starting clinical treatments");
    }

    @Test
    @DisplayName("Privilege Escalation Protection: Patient cannot access emergency queue or staff controls")
    public void testPatientAccessRestrictions() {
        PatientUser patient = new PatientUser(10, "patient", "hash", "John Doe", "555-3333", "john@mail.com",
                true, null, "P0001");
        SessionManager.getInstance().setCurrentUser(patient);

        assertThrows(UnauthorizedException.class, () -> {
            SessionManager.getInstance().requirePermission(Permission.VIEW_EMERGENCY_QUEUE);
        });
        assertThrows(UnauthorizedException.class, () -> {
            SessionManager.getInstance().requirePermission(Permission.REGISTER_PATIENTS);
        });
        assertThrows(UnauthorizedException.class, () -> {
            SessionManager.getInstance().requirePermission(Permission.START_TREATMENT);
        });
        assertThrows(UnauthorizedException.class, () -> {
            SessionManager.getInstance().requirePermission(Permission.MANAGE_BEDS);
        });
    }
}
