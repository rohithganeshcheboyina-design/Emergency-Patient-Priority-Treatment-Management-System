package com.hospital.model;

/**
 * User roles within the Hospital Emergency Department System.
 */
public enum Role {
    ADMIN("Admin"),
    DOCTOR("Doctor"),
    NURSE("Nurse");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        if (text == null) return null;
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(text.trim()) || r.displayName.equalsIgnoreCase(text.trim())) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }
}
