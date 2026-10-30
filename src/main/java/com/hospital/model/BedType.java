package com.hospital.model;

/**
 * Types of hospital and emergency ward beds.
 */
public enum BedType {
    EMERGENCY("Emergency Bay"),
    ICU("ICU Critical Care"),
    TRAUMA_BAY("Trauma Resuscitation"),
    OBSERVATION("Observation Unit"),
    GENERAL_WARD("General Ward");

    private final String displayName;

    BedType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BedType fromString(String text) {
        if (text == null) return EMERGENCY;
        for (BedType t : BedType.values()) {
            if (t.name().equalsIgnoreCase(text.trim()) || t.displayName.equalsIgnoreCase(text.trim())) {
                return t;
            }
        }
        return EMERGENCY;
    }
}
