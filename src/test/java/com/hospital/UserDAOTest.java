package com.hospital;

import com.hospital.dao.DatabaseConnection;
import com.hospital.dao.UserDAO;
import com.hospital.model.*;
import com.hospital.security.PasswordUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User DAO and Identity Persistence Tests")
public class UserDAOTest {

    private static UserDAO userDAO;

    @BeforeAll
    public static void setUp() throws Exception {
        DatabaseConnection.getConnection();
        userDAO = new UserDAO();
    }

    @Test
    @DisplayName("Should retrieve existing demo users from database")
    public void testGetExistingUsers() {
        User admin = userDAO.findByUsername("admin");
        assertNotNull(admin, "Admin account must exist");
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isActive());

        User doctor = userDAO.findByUsername("doctor");
        assertNotNull(doctor, "Doctor account must exist");
        assertEquals(Role.DOCTOR, doctor.getRole());
        assertTrue(doctor instanceof Doctor);

        User nurse = userDAO.findByUsername("nurse");
        assertNotNull(nurse, "Nurse account must exist");
        assertEquals(Role.NURSE, nurse.getRole());
        assertTrue(nurse instanceof Nurse);

        User patient = userDAO.findByUsername("patient");
        assertNotNull(patient, "Patient demo account must exist");
        assertEquals(Role.PATIENT, patient.getRole());
        assertTrue(patient instanceof PatientUser);
    }

    @Test
    @DisplayName("Should create, find, update and soft-delete a new staff account")
    public void testUserLifecycle() {
        String uniqueUser = "test_staff_" + System.currentTimeMillis();
        String passHash = PasswordUtil.hashPassword("secret123");

        Doctor doc = new Doctor(0, uniqueUser, passHash, "Dr. Test Specialist", "555-9988", "test@hospital.com",
                true, LocalDateTime.now(), "Cardiology Specialist", "Cardiology");

        boolean created = userDAO.createUser(doc);
        assertTrue(created, "User must be created");
        assertTrue(doc.getUserId() > 0, "Generated ID must be populated");

        // Find by username
        User found = userDAO.findByUsername(uniqueUser);
        assertNotNull(found);
        assertEquals("Dr. Test Specialist", found.getFullName());
        assertEquals(Role.DOCTOR, found.getRole());

        // Update profile
        found.setFullName("Dr. Test Senior Specialist");
        found.setPhone("555-7766");
        boolean updated = userDAO.updateUser(found);
        assertTrue(updated);

        User updatedFound = userDAO.findById(found.getUserId());
        assertEquals("Dr. Test Senior Specialist", updatedFound.getFullName());
        assertEquals("555-7766", updatedFound.getPhone());

        // Update password
        String newHash = PasswordUtil.hashPassword("newSecret456");
        boolean passUpdated = userDAO.updatePassword(found.getUserId(), newHash);
        assertTrue(passUpdated);
        assertTrue(PasswordUtil.verifyPassword("newSecret456", userDAO.findById(found.getUserId()).getPasswordHash()));

        // Soft delete
        boolean deleted = userDAO.deleteUser(found.getUserId());
        assertTrue(deleted);

        // findByUsername filters active = TRUE, so it should return null
        assertNull(userDAO.findByUsername(uniqueUser));

        // findById returns the deactivated record with active = false
        User inactive = userDAO.findById(found.getUserId());
        assertNotNull(inactive);
        assertFalse(inactive.isActive());
    }

    @Test
    @DisplayName("Should filter available active doctors and nurses correctly")
    public void testGetAvailableStaff() {
        List<Doctor> doctors = userDAO.getAvailableDoctors();
        assertNotNull(doctors);
        assertFalse(doctors.isEmpty());
        for (Doctor d : doctors) {
            assertEquals(Role.DOCTOR, d.getRole());
            assertTrue(d.isActive());
        }

        List<Nurse> nurses = userDAO.getAvailableNurses();
        assertNotNull(nurses);
        assertFalse(nurses.isEmpty());
        for (Nurse n : nurses) {
            assertEquals(Role.NURSE, n.getRole());
            assertTrue(n.isActive());
        }
    }
}
