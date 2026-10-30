# Database Documentation & Setup Guide

This directory contains the relational database definition, automated migration scripts, and realistic demo datasets for the **Hospital Emergency Patient Priority Management System**.

## Directory Structure

```text
database/
├── schema/
│   └── schema.sql              # Master DDL creating database, tables, PKs, FKs, indexes
├── migrations/
│   └── V1__initial_schema.sql  # Baseline versioned SQL migration script
├── sample-data/
│   └── sample_data.sql         # Seed data with demo accounts, beds, appointments, patients
└── README.md                   # This documentation
```

---

## Table Structures

1. **`users`**: System user accounts (Admin, Doctor, Nurse, Patient) with BCrypt-hashed credentials and role-based permissions.
2. **`patients`**: Emergency department patient records with demographics, arrival timestamp, triage priority (RED 1 - GREEN 4), and lifecycle status.
3. **`beds`**: Hospital bed registry with room number, bed type (ICU, EMERGENCY, GENERAL, TRAUMA), and availability tracking.
4. **`appointments`**: Scheduled consultation, follow-up, and emergency triage slots mapped to doctors and patients.
5. **`emergency_assessments`**: Clinical vital signs and audit logs of triage assessments (heart rate, blood pressure, SpO2, pain score).
6. **`treatments`**: Clinical interventions, medications, dosages, and notes recorded by attending physicians.
7. **`patient_status_history`**: Immutable audit trail capturing every status transition timestamp, prior status, and actor.
8. **`activity_logs`**: System-wide operational events (`LOGIN`, `LOGOUT`, `PATIENT_REGISTERED`, `TRIAGE_EVALUATED`, `BED_ALLOCATED`, etc.).

---

## MySQL Setup Instructions

### Option 1: MySQL Command Line (Recommended)
Open Command Prompt (CMD) in the project root:

```cmd
mysql -u root -p < database/schema/schema.sql
mysql -u root -p < database/sample-data/sample_data.sql
```

### Option 2: MySQL Workbench
1. Open MySQL Workbench and connect to your local MySQL instance.
2. Go to **File -> Open SQL Script...** and select `database/schema/schema.sql`.
3. Click the **Execute (Lightning Bolt)** icon.
4. Go to **File -> Open SQL Script...** and select `database/sample-data/sample_data.sql`.
5. Click **Execute**.

### Option 3: phpMyAdmin / XAMPP
1. Open `http://localhost/phpmyadmin` in your web browser.
2. Click the **Import** tab.
3. Choose `database/schema/schema.sql` and click **Import**.
4. Repeat for `database/sample-data/sample_data.sql`.

---

## Seed Accounts & Default Credentials

| Username | Password | Role | Full Name | Department / Specialization |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ADMIN` | Hospital Administrator | IT / Administration |
| `doctor` | `doctor123` | `DOCTOR` | Dr. Robert Vance, MD | Emergency Medicine |
| `doctor2` | `doctor123` | `DOCTOR` | Dr. Sarah Jenkins, MD | Trauma Surgery |
| `nurse` | `nurse123` | `NURSE` | Nurse Clara Barton, RN | Emergency Triage |
| `nurse2` | `nurse123` | `NURSE` | Nurse David Miller, RN | Acute Care |
| `patient` | `patient123` | `PATIENT` | John Doe | Outpatient Services |
