package com.hospital.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * User roles within the Hospital Emergency Department System with strict RBAC mappings.
 */
public enum Role {
    ADMIN("Admin", EnumSet.of(
            Permission.VIEW_ADMIN_DASHBOARD,
            Permission.MANAGE_USERS,
            Permission.VIEW_ALL_PATIENTS,
            Permission.VIEW_EMERGENCY_QUEUE,
            Permission.MANAGE_EMERGENCY_QUEUE,
            Permission.VIEW_REPORTS,
            Permission.VIEW_ACTIVITY_LOGS,
            Permission.MANAGE_SETTINGS,
            Permission.MANAGE_BEDS,
            Permission.VIEW_BEDS,
            Permission.ASSIGN_BEDS,
            Permission.VIEW_PATIENT_DETAILS,
            Permission.VIEW_PATIENT_HISTORY,
            Permission.MANAGE_APPOINTMENTS,
            Permission.VIEW_ROLE_AUTHORIZATION
    )),

    DOCTOR("Doctor", EnumSet.of(
            Permission.VIEW_DOCTOR_DASHBOARD,
            Permission.VIEW_EMERGENCY_QUEUE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.VIEW_PATIENT_DETAILS,
            Permission.VIEW_PATIENT_HISTORY,
            Permission.START_TREATMENT,
            Permission.UPDATE_TREATMENT,
            Permission.COMPLETE_TREATMENT,
            Permission.DISCHARGE_PATIENT,
            Permission.CALL_PATIENT,
            Permission.VIEW_BEDS,
            Permission.ASSIGN_BEDS,
            Permission.MANAGE_APPOINTMENTS,
            Permission.VIEW_ROLE_AUTHORIZATION
    )),

    NURSE("Nurse", EnumSet.of(
            Permission.VIEW_NURSE_DASHBOARD,
            Permission.REGISTER_PATIENTS,
            Permission.VIEW_EMERGENCY_QUEUE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.VIEW_PATIENT_DETAILS,
            Permission.UPDATE_PATIENT_VITALS,
            Permission.CALL_PATIENT,
            Permission.VIEW_BEDS,
            Permission.ASSIGN_BEDS,
            Permission.VIEW_ROLE_AUTHORIZATION
    )),

    PATIENT("Patient", EnumSet.of(
            Permission.VIEW_PATIENT_DASHBOARD,
            Permission.VIEW_OWN_PROFILE,
            Permission.BOOK_OWN_APPOINTMENT,
            Permission.CANCEL_OWN_APPOINTMENT,
            Permission.VIEW_OWN_RECORDS,
            Permission.VIEW_ROLE_AUTHORIZATION
    ));

    private final String displayName;
    private final Set<Permission> permissions;

    Role(String displayName, Set<Permission> permissions) {
        this.displayName = displayName;
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permission != null && permissions.contains(permission);
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
