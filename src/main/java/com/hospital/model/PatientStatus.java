package com.hospital.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Emergency patient workflow statuses with transition validation.
 */
public enum PatientStatus {
    WAITING("Waiting"),
    CALLED("Called"),
    IN_TREATMENT("In Treatment"),
    TREATED("Treated"),
    DISCHARGED("Discharged"),
    CANCELLED("Cancelled");

    private final String displayName;

    PatientStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determines whether transitioning from this status to targetStatus is allowed.
     */
    public boolean canTransitionTo(PatientStatus target) {
        if (target == null || target == this) {
            return false;
        }

        switch (this) {
            case WAITING:
                return target == CALLED || target == IN_TREATMENT || target == CANCELLED;
            case CALLED:
                return target == IN_TREATMENT || target == WAITING || target == CANCELLED;
            case IN_TREATMENT:
                return target == TREATED || target == CANCELLED;
            case TREATED:
                return target == DISCHARGED || target == IN_TREATMENT;
            case DISCHARGED:
            case CANCELLED:
                // Terminal statuses; cannot transition further without re-registration
                return false;
            default:
                return false;
        }
    }

    /**
     * Determines whether the patient is considered active in the emergency waiting/active queue.
     */
    public boolean isActiveInQueue() {
        return this == WAITING || this == CALLED;
    }

    public static PatientStatus fromString(String text) {
        if (text == null) return null;
        for (PatientStatus s : values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown patient status: " + text);
    }
}
