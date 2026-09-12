package com.hospital;

import com.hospital.model.PatientStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatusWorkflowTest {

    @Test
    @DisplayName("Should allow valid emergency triage workflow transitions")
    public void testValidTransitions() {
        // WAITING -> CALLED
        assertTrue(PatientStatus.WAITING.canTransitionTo(PatientStatus.CALLED));

        // WAITING -> IN_TREATMENT (Direct emergency intake)
        assertTrue(PatientStatus.WAITING.canTransitionTo(PatientStatus.IN_TREATMENT));

        // CALLED -> IN_TREATMENT
        assertTrue(PatientStatus.CALLED.canTransitionTo(PatientStatus.IN_TREATMENT));

        // IN_TREATMENT -> TREATED
        assertTrue(PatientStatus.IN_TREATMENT.canTransitionTo(PatientStatus.TREATED));

        // TREATED -> DISCHARGED
        assertTrue(PatientStatus.TREATED.canTransitionTo(PatientStatus.DISCHARGED));

        // WAITING -> CANCELLED
        assertTrue(PatientStatus.WAITING.canTransitionTo(PatientStatus.CANCELLED));
    }

    @Test
    @DisplayName("Should disallow invalid or premature status transitions")
    public void testDisallowedTransitions() {
        // DISCHARGED patient cannot directly return to IN_TREATMENT
        assertFalse(PatientStatus.DISCHARGED.canTransitionTo(PatientStatus.IN_TREATMENT));
        assertFalse(PatientStatus.DISCHARGED.canTransitionTo(PatientStatus.WAITING));

        // CANCELLED cannot transition anywhere
        assertFalse(PatientStatus.CANCELLED.canTransitionTo(PatientStatus.IN_TREATMENT));

        // WAITING cannot directly become DISCHARGED without treatment or evaluation
        assertFalse(PatientStatus.WAITING.canTransitionTo(PatientStatus.DISCHARGED));

        // Self-transitions should be disallowed
        assertFalse(PatientStatus.WAITING.canTransitionTo(PatientStatus.WAITING));
        assertFalse(PatientStatus.IN_TREATMENT.canTransitionTo(PatientStatus.IN_TREATMENT));
    }
}
