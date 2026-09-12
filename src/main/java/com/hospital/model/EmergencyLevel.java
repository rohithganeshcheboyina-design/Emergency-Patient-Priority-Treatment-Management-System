package com.hospital.model;

/**
 * Emergency severity levels and assigned priority ordering.
 * 1 = CRITICAL (Red)
 * 2 = HIGH (Orange)
 * 3 = MEDIUM (Yellow)
 * 4 = LOW (Green)
 */
public enum EmergencyLevel {
    CRITICAL(1, "Critical", "#EF4444", "Life-threatening emergency requiring immediate intervention"),
    HIGH(2, "High", "#F97316", "Severe illness or trauma with potential rapid deterioration"),
    MEDIUM(3, "Medium", "#F59E0B", "Urgent condition requiring prompt medical attention"),
    LOW(4, "Low", "#10B981", "Non-urgent condition, stable patient");

    private final int priority;
    private final String displayName;
    private final String colorHex;
    private final String description;

    EmergencyLevel(int priority, String displayName, String colorHex, String description) {
        this.priority = priority;
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    public static EmergencyLevel fromPriority(int priority) {
        for (EmergencyLevel level : values()) {
            if (level.priority == priority) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid emergency priority: " + priority);
    }

    public static EmergencyLevel fromString(String text) {
        if (text == null) return null;
        for (EmergencyLevel level : values()) {
            if (level.name().equalsIgnoreCase(text.trim()) || level.displayName.equalsIgnoreCase(text.trim())) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown emergency level: " + text);
    }
}
