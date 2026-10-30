package com.hospital;

import com.hospital.exception.UnauthorizedException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role-Based Access Control (RBAC) Specification Tests")
public class RoleAuthorizationTest {

    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @AfterEach
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("Admin Role Permissions: Administrative allowed, Clinical restricted")
    public void testAdminPermissions() {
        User admin = new User(1, "admin", "Admin User", "admin@hospital.com", Role.ADMIN);
        sessionManager.setCurrentUser(admin);

        // Allowed
        assertTrue(sessionManager.hasPermission(Permission.VIEW_ADMIN_DASHBOARD));
        assertTrue(sessionManager.hasPermission(Permission.MANAGE_USERS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_ALL_PATIENTS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_EMERGENCY_QUEUE));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_REPORTS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_ACTIVITY_LOGS));
        assertTrue(sessionManager.hasPermission(Permission.MANAGE_SETTINGS));
        assertTrue(sessionManager.hasPermission(Permission.MANAGE_BEDS));
        assertTrue(sessionManager.hasPermission(Permission.MANAGE_APPOINTMENTS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_ROLE_AUTHORIZATION));

        // Explicitly RESTRICTED
        assertFalse(sessionManager.hasPermission(Permission.REGISTER_PATIENTS));
        assertFalse(sessionManager.hasPermission(Permission.START_TREATMENT));
        assertFalse(sessionManager.hasPermission(Permission.COMPLETE_TREATMENT));
        assertFalse(sessionManager.hasPermission(Permission.DISCHARGE_PATIENT));

        // Security check enforces exception
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.START_TREATMENT));
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.REGISTER_PATIENTS));
    }

    @Test
    @DisplayName("Doctor Role Permissions: Clinical allowed, Admin & Intake restricted")
    public void testDoctorPermissions() {
        Doctor doctor = new Doctor();
        doctor.setUserId(2);
        doctor.setUsername("doctor");
        doctor.setFullName("Dr. Vance");
        doctor.setEmail("vance@hospital.com");
        doctor.setRole(Role.DOCTOR);
        sessionManager.setCurrentUser(doctor);

        // Allowed
        assertTrue(sessionManager.hasPermission(Permission.VIEW_DOCTOR_DASHBOARD));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_EMERGENCY_QUEUE));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_PATIENT_DETAILS));
        assertTrue(sessionManager.hasPermission(Permission.START_TREATMENT));
        assertTrue(sessionManager.hasPermission(Permission.UPDATE_TREATMENT));
        assertTrue(sessionManager.hasPermission(Permission.COMPLETE_TREATMENT));
        assertTrue(sessionManager.hasPermission(Permission.DISCHARGE_PATIENT));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_PATIENT_HISTORY));
        assertTrue(sessionManager.hasPermission(Permission.ASSIGN_BEDS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_BEDS));
        assertTrue(sessionManager.hasPermission(Permission.MANAGE_APPOINTMENTS));

        // Explicitly RESTRICTED
        assertFalse(sessionManager.hasPermission(Permission.MANAGE_USERS));
        assertFalse(sessionManager.hasPermission(Permission.MANAGE_SETTINGS));
        assertFalse(sessionManager.hasPermission(Permission.REGISTER_PATIENTS));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_ACTIVITY_LOGS));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_REPORTS));

        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.MANAGE_USERS));
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.MANAGE_SETTINGS));
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.REGISTER_PATIENTS));
    }

    @Test
    @DisplayName("Nurse Role Permissions: Intake, Queue, Vitals allowed, Treatment & Admin restricted")
    public void testNursePermissions() {
        Nurse nurse = new Nurse();
        nurse.setUserId(4);
        nurse.setUsername("nurse");
        nurse.setFullName("Nurse Lisa");
        nurse.setEmail("lisa@hospital.com");
        nurse.setRole(Role.NURSE);
        sessionManager.setCurrentUser(nurse);

        // Allowed
        assertTrue(sessionManager.hasPermission(Permission.VIEW_NURSE_DASHBOARD));
        assertTrue(sessionManager.hasPermission(Permission.REGISTER_PATIENTS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_EMERGENCY_QUEUE));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_ALL_PATIENTS));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_PATIENT_DETAILS));
        assertTrue(sessionManager.hasPermission(Permission.UPDATE_PATIENT_VITALS));
        assertTrue(sessionManager.hasPermission(Permission.CALL_PATIENT));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_BEDS));
        assertTrue(sessionManager.hasPermission(Permission.ASSIGN_BEDS));

        // Explicitly RESTRICTED
        assertFalse(sessionManager.hasPermission(Permission.START_TREATMENT));
        assertFalse(sessionManager.hasPermission(Permission.COMPLETE_TREATMENT));
        assertFalse(sessionManager.hasPermission(Permission.DISCHARGE_PATIENT));
        assertFalse(sessionManager.hasPermission(Permission.MANAGE_USERS));
        assertFalse(sessionManager.hasPermission(Permission.MANAGE_SETTINGS));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_REPORTS));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_ACTIVITY_LOGS));

        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.START_TREATMENT));
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.DISCHARGE_PATIENT));
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.MANAGE_USERS));
    }

    @Test
    @DisplayName("Patient Role Permissions: Portal & Appointments allowed, Clinical/Admin restricted")
    public void testPatientPermissions() {
        PatientUser patient = new PatientUser(5, "patient", "John Doe", "john@example.com", "P0001");
        sessionManager.setCurrentUser(patient);

        // Allowed
        assertTrue(sessionManager.hasPermission(Permission.VIEW_PATIENT_DASHBOARD));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_OWN_PROFILE));
        assertTrue(sessionManager.hasPermission(Permission.BOOK_OWN_APPOINTMENT));
        assertTrue(sessionManager.hasPermission(Permission.CANCEL_OWN_APPOINTMENT));
        assertTrue(sessionManager.hasPermission(Permission.VIEW_OWN_RECORDS));

        // Explicitly RESTRICTED
        assertFalse(sessionManager.hasPermission(Permission.VIEW_ADMIN_DASHBOARD));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_DOCTOR_DASHBOARD));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_NURSE_DASHBOARD));
        assertFalse(sessionManager.hasPermission(Permission.REGISTER_PATIENTS));
        assertFalse(sessionManager.hasPermission(Permission.START_TREATMENT));
        assertFalse(sessionManager.hasPermission(Permission.MANAGE_USERS));
        assertFalse(sessionManager.hasPermission(Permission.VIEW_ALL_PATIENTS));

        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.VIEW_ADMIN_DASHBOARD));
        assertThrows(UnauthorizedException.class, () -> sessionManager.requirePermission(Permission.VIEW_ALL_PATIENTS));
    }
}
