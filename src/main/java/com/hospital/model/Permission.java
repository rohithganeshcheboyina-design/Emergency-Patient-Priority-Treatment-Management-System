package com.hospital.model;

/**
 * Granular permissions for Role-Based Access Control (RBAC).
 */
public enum Permission {
    VIEW_ADMIN_DASHBOARD("Admin Dashboard", "Access administrative oversight dashboard and charts"),
    VIEW_DOCTOR_DASHBOARD("Doctor Dashboard", "Access physician clinical workstation and next patient card"),
    VIEW_NURSE_DASHBOARD("Nurse Dashboard", "Access nurse triage workstation and intake preview"),
    VIEW_PATIENT_DASHBOARD("Patient Dashboard", "Access patient self-service portal"),

    MANAGE_USERS("Manage Users", "Create, edit, and deactivate staff user accounts"),
    VIEW_ALL_PATIENTS("View All Patients", "Search and view full patient directory and history"),
    VIEW_EMERGENCY_QUEUE("View Emergency Queue", "Monitor active emergency priority queue"),
    MANAGE_EMERGENCY_QUEUE("Manage Emergency Queue", "Re-order, adjust, or manage active triage queue"),

    REGISTER_PATIENTS("Register Patients", "Intake new emergency patients and enter demographics"),
    VIEW_PATIENT_DETAILS("View Patient Details", "Inspect medical symptoms, triage level, and history"),
    UPDATE_PATIENT_VITALS("Update Patient Vitals & Notes", "Record vital signs, observations, and nursing notes"),
    CALL_PATIENT("Call Patient to Bay", "Summon patient to examination counter or triage bay"),

    START_TREATMENT("Start Treatment", "Initiate clinical emergency treatment for a patient"),
    UPDATE_TREATMENT("Update Treatment", "Add diagnosis, procedure notes, and medication orders"),
    COMPLETE_TREATMENT("Complete Treatment", "Finalize clinical treatment course"),
    DISCHARGE_PATIENT("Discharge Patient", "Discharge patient with clinical summary"),
    VIEW_PATIENT_HISTORY("View Patient History", "Review comprehensive clinical and audit history"),

    VIEW_REPORTS("View Reports & Statistics", "Generate departmental reports and CSV data exports"),
    VIEW_ACTIVITY_LOGS("View Activity Logs", "Audit system and staff operational activity logs"),
    MANAGE_SETTINGS("System Settings", "Configure wait-time thresholds and refresh cadences"),

    MANAGE_BEDS("Manage Beds", "Add, configure, and supervise hospital bed allocations"),
    VIEW_BEDS("View Beds", "Check bed availability status and ward occupancy"),
    ASSIGN_BEDS("Assign / Release Beds", "Allocate beds to patients and release on discharge"),

    MANAGE_APPOINTMENTS("Manage Appointments", "Schedule and coordinate non-emergency consultations"),
    VIEW_OWN_PROFILE("View Own Profile", "Access personal profile and medical summary"),
    BOOK_OWN_APPOINTMENT("Book Appointment", "Schedule self-service appointment"),
    CANCEL_OWN_APPOINTMENT("Cancel Appointment", "Cancel own upcoming appointment"),
    VIEW_OWN_RECORDS("View Own Records", "Review approved clinical consultation summaries"),

    VIEW_ROLE_AUTHORIZATION("View Role Authorization", "Inspect the RBAC permissions matrix and access control");

    private final String title;
    private final String description;

    Permission(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
