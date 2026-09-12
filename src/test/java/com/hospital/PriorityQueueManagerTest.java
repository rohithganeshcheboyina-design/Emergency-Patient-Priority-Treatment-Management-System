package com.hospital;

import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.QueueItem;
import com.hospital.service.PriorityQueueManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification of the core DSA Priority Queue sorting and FIFO-within-priority behavior.
 */
public class PriorityQueueManagerTest {

    private PriorityQueueManager queueManager;
    private final LocalDate today = LocalDate.of(2026, 9, 12);

    @BeforeEach
    public void setUp() {
        queueManager = new PriorityQueueManager();
    }

    private Patient createTestPatient(int id, String code, String token, String name,
                                      EmergencyLevel level, LocalTime time, PatientStatus status) {
        Patient p = new Patient();
        p.setPatientId(id);
        p.setPatientCode(code);
        p.setTokenNumber(token);
        p.setFullName(name);
        p.setEmergencyLevel(level);
        p.setArrivalTime(LocalDateTime.of(today, time));
        p.setCurrentStatus(status);
        p.setSymptoms("Test Symptoms");
        return p;
    }

    @Test
    @DisplayName("Should order Critical before High, Medium, and Low regardless of arrival order")
    public void testPriorityOrderingAcrossDifferentLevels() {
        Patient low = createTestPatient(1, "P0001", "ER-001", "Low Patient", EmergencyLevel.LOW, LocalTime.of(8, 0), PatientStatus.WAITING);
        Patient med = createTestPatient(2, "P0002", "ER-002", "Med Patient", EmergencyLevel.MEDIUM, LocalTime.of(8, 30), PatientStatus.WAITING);
        Patient high = createTestPatient(3, "P0003", "ER-003", "High Patient", EmergencyLevel.HIGH, LocalTime.of(9, 0), PatientStatus.WAITING);
        Patient crit = createTestPatient(4, "P0004", "ER-004", "Crit Patient", EmergencyLevel.CRITICAL, LocalTime.of(9, 30), PatientStatus.WAITING);

        // Enqueue in reverse priority order
        queueManager.enqueue(low);
        queueManager.enqueue(med);
        queueManager.enqueue(high);
        queueManager.enqueue(crit);

        List<QueueItem> items = queueManager.getAllInOrder();
        assertEquals(4, items.size());

        assertEquals("Crit Patient", items.get(0).getPatientName());
        assertEquals(EmergencyLevel.CRITICAL, items.get(0).getEmergencyLevel());
        assertEquals(1, items.get(0).getQueuePosition());

        assertEquals("High Patient", items.get(1).getPatientName());
        assertEquals(EmergencyLevel.HIGH, items.get(1).getEmergencyLevel());
        assertEquals(2, items.get(1).getQueuePosition());

        assertEquals("Med Patient", items.get(2).getPatientName());
        assertEquals(EmergencyLevel.MEDIUM, items.get(2).getEmergencyLevel());
        assertEquals(3, items.get(2).getQueuePosition());

        assertEquals("Low Patient", items.get(3).getPatientName());
        assertEquals(EmergencyLevel.LOW, items.get(3).getEmergencyLevel());
        assertEquals(4, items.get(3).getQueuePosition());
    }

    @Test
    @DisplayName("Should order patients with identical priority by earlier arrival time")
    public void testSamePriorityArrivalOrdering() {
        Patient c1 = createTestPatient(1, "P0001", "ER-001", "Early Critical", EmergencyLevel.CRITICAL, LocalTime.of(10, 0), PatientStatus.WAITING);
        Patient c2 = createTestPatient(2, "P0002", "ER-002", "Late Critical", EmergencyLevel.CRITICAL, LocalTime.of(10, 15), PatientStatus.WAITING);

        // Enqueue later one first
        queueManager.enqueue(c2);
        queueManager.enqueue(c1);

        List<QueueItem> items = queueManager.getAllInOrder();
        assertEquals("Early Critical", items.get(0).getPatientName());
        assertEquals("Late Critical", items.get(1).getPatientName());
    }

    @Test
    @DisplayName("Should strictly match the problem specification scenario from Section 1")
    public void testSpecificationScenario() {
        // Patient A → Critical → 10:05
        // Patient B → High → 10:01
        // Patient C → Critical → 10:10
        // Patient D → Medium → 09:55
        Patient patientA = createTestPatient(1, "P0001", "ER-001", "Patient A", EmergencyLevel.CRITICAL, LocalTime.of(10, 5), PatientStatus.WAITING);
        Patient patientB = createTestPatient(2, "P0002", "ER-002", "Patient B", EmergencyLevel.HIGH, LocalTime.of(10, 1), PatientStatus.WAITING);
        Patient patientC = createTestPatient(3, "P0003", "ER-003", "Patient C", EmergencyLevel.CRITICAL, LocalTime.of(10, 10), PatientStatus.WAITING);
        Patient patientD = createTestPatient(4, "P0004", "ER-004", "Patient D", EmergencyLevel.MEDIUM, LocalTime.of(9, 55), PatientStatus.WAITING);

        // Add them in random order
        queueManager.enqueue(patientD);
        queueManager.enqueue(patientB);
        queueManager.enqueue(patientA);
        queueManager.enqueue(patientC);

        List<QueueItem> result = queueManager.getAllInOrder();
        assertEquals(4, result.size());

        // Correct queue order:
        // 1. Patient A → Critical
        // 2. Patient C → Critical
        // 3. Patient B → High
        // 4. Patient D → Medium
        assertEquals("Patient A", result.get(0).getPatientName(), "Position #1 must be Patient A (Critical, 10:05)");
        assertEquals(1, result.get(0).getQueuePosition());

        assertEquals("Patient C", result.get(1).getPatientName(), "Position #2 must be Patient C (Critical, 10:10)");
        assertEquals(2, result.get(1).getQueuePosition());

        assertEquals("Patient B", result.get(2).getPatientName(), "Position #3 must be Patient B (High, 10:01)");
        assertEquals(3, result.get(2).getQueuePosition());

        assertEquals("Patient D", result.get(3).getPatientName(), "Position #4 must be Patient D (Medium, 09:55)");
        assertEquals(4, result.get(3).getQueuePosition());
    }

    @Test
    @DisplayName("Should pop head of queue and shift next patient to position 1")
    public void testDequeueOperation() {
        Patient p1 = createTestPatient(1, "P0001", "ER-001", "Patient 1", EmergencyLevel.CRITICAL, LocalTime.of(10, 0), PatientStatus.WAITING);
        Patient p2 = createTestPatient(2, "P0002", "ER-002", "Patient 2", EmergencyLevel.HIGH, LocalTime.of(10, 5), PatientStatus.WAITING);

        queueManager.enqueue(p1);
        queueManager.enqueue(p2);

        assertEquals("Patient 1", queueManager.peek().getPatientName());

        QueueItem dequeued = queueManager.dequeue();
        assertNotNull(dequeued);
        assertEquals("Patient 1", dequeued.getPatientName());

        assertEquals(1, queueManager.size());
        assertEquals("Patient 2", queueManager.peek().getPatientName());
        assertEquals(1, queueManager.peek().getQueuePosition());
    }

    @Test
    @DisplayName("Should not allow discharged or inactive patients in waiting queue")
    public void testInactivePatientsExcluded() {
        Patient discharged = createTestPatient(1, "P0001", "ER-001", "Discharged Patient", EmergencyLevel.CRITICAL, LocalTime.of(10, 0), PatientStatus.DISCHARGED);
        queueManager.enqueue(discharged);
        assertTrue(queueManager.isEmpty());

        Patient waiting = createTestPatient(2, "P0002", "ER-002", "Waiting Patient", EmergencyLevel.CRITICAL, LocalTime.of(10, 0), PatientStatus.WAITING);
        queueManager.enqueue(waiting);
        assertEquals(1, queueManager.size());

        // Update status to IN_TREATMENT
        waiting.setCurrentStatus(PatientStatus.IN_TREATMENT);
        queueManager.enqueue(waiting);
        assertTrue(queueManager.isEmpty(), "Patient in treatment should be removed from waiting queue");
    }
}
