package com.hospital;

import com.hospital.model.Bed;
import com.hospital.model.BedStatus;
import com.hospital.model.BedType;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.service.BedService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bed Management & Ward Capacity Unit Tests")
public class BedManagementTest {

    private BedService bedService;
    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        bedService = new BedService();
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @AfterEach
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("Should retrieve bed inventory and capacity summary")
    public void testBedInventory() {
        User admin = new User(1, "admin", "Admin", "admin@hospital.com", Role.ADMIN);
        sessionManager.setCurrentUser(admin);

        List<Bed> beds = bedService.getAllBeds();
        assertNotNull(beds);
        assertFalse(beds.isEmpty(), "Sample database should have pre-configured beds");

        Map<String, Integer> summary = bedService.getBedStatistics();
        assertNotNull(summary);
        assertTrue(summary.containsKey("total"));
        assertTrue(summary.containsKey("available"));
        assertTrue(summary.containsKey("occupied"));
        assertTrue(summary.get("total") >= 6);
    }

    @Test
    @DisplayName("Doctor or Nurse should be permitted to assign and release available bed")
    public void testAssignAndReleaseBed() throws Exception {
        User doctor = new User(2, "doctor", "Dr. Vance", "vance@hospital.com", Role.DOCTOR);
        sessionManager.setCurrentUser(doctor);

        List<Bed> beds = bedService.getAllBeds();
        Bed targetBed = beds.stream()
                .filter(b -> b.getStatus() == BedStatus.AVAILABLE)
                .findFirst()
                .orElse(null);

        assertNotNull(targetBed, "There should be an available bed in sample data");

        // Assign bed to patient 1
        boolean assigned = bedService.assignBed(targetBed.getBedId(), 1, "Patient admitted to Emergency Bay");
        assertTrue(assigned);

        Bed updated = bedService.getAllBeds().stream()
                .filter(b -> b.getBedId() == targetBed.getBedId())
                .findFirst()
                .orElse(null);

        assertNotNull(updated);
        assertEquals(BedStatus.OCCUPIED, updated.getStatus());
        assertEquals(1, updated.getAssignedPatientId());

        // Release bed
        boolean released = bedService.releaseBed(targetBed.getBedId());
        assertTrue(released);

        Bed releasedBed = bedService.getAllBeds().stream()
                .filter(b -> b.getBedId() == targetBed.getBedId())
                .findFirst()
                .orElse(null);

        assertNotNull(releasedBed);
        assertEquals(BedStatus.AVAILABLE, releasedBed.getStatus());
        assertNull(releasedBed.getAssignedPatientId());
    }
}
