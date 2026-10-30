package com.hospital.model;

/**
 * Operational status of a hospital bed.
 */
public enum BedStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    RESERVED("Reserved"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    BedStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BedStatus fromString(String text) {
        if (text == null) return AVAILABLE;
        for (BedStatus s : BedStatus.values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return AVAILABLE;
    }
}
