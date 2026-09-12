package com.hospital;

import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.service.PatientService;
import com.hospital.service.PriorityQueueManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PatientValidationTest {

    private PatientService patientService;

    @BeforeEach
    public void setUp() {
        PriorityQueueManager pq = new PriorityQueueManager();
        patientService = new PatientService(pq);
    }

    private Patient createValidPatient() {
        Patient p = new Patient();
        p.setFullName("Test Patient");
        p.setAge(35);
        p.setGender("Male");
        p.setPhone("555-12345");
        p.setSymptoms("Fever and severe cough");
        p.setEmergencyLevel(EmergencyLevel.MEDIUM);
        return p;
    }

    @Test
    @DisplayName("Valid patient should pass validation without exceptions")
    public void testValidPatient() {
        Patient p = createValidPatient();
        assertDoesNotThrow(() -> patientService.validatePatient(p));
    }

    @Test
    @DisplayName("Should reject blank or null patient name")
    public void testBlankName() {
        Patient p = createValidPatient();
        p.setFullName("");
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));

        p.setFullName("   ");
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));

        p.setFullName(null);
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));
    }

    @Test
    @DisplayName("Should reject invalid negative age or unrealistic age")
    public void testInvalidAge() {
        Patient p = createValidPatient();
        p.setAge(-1);
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));

        p.setAge(135);
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));
    }

    @Test
    @DisplayName("Should reject invalid short phone number")
    public void testInvalidPhone() {
        Patient p = createValidPatient();
        p.setPhone("1234");
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));

        p.setPhone(null);
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));
    }

    @Test
    @DisplayName("Should reject empty symptoms")
    public void testEmptySymptoms() {
        Patient p = createValidPatient();
        p.setSymptoms("  ");
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));
    }

    @Test
    @DisplayName("Should reject null emergency level")
    public void testNullEmergencyLevel() {
        Patient p = createValidPatient();
        p.setEmergencyLevel(null);
        assertThrows(InvalidPatientDataException.class, () -> patientService.validatePatient(p));
    }
}
