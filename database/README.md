# Database Documentation & Setup Guide

This directory contains the relational database definition and realistic demo datasets for the **Hospital Emergency Patient Priority Management System**.

## Files
- `schema.sql`: Full Data Definition Language (DDL) creating the `hospital_emergency_system` database, tables, primary keys, foreign keys, indexes, and constraints.
- `sample_data.sql`: Realistic demo data with 5 users (Admin, 2 Doctors, 2 Nurses) and 12 emergency patients across all 4 triage priority levels (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) and operational states (`WAITING`, `CALLED`, `IN_TREATMENT`, `TREATED`, `DISCHARGED`).

## Table Structures

1. **`users`**: System user accounts (Admin, Doctor, Nurse) with BCrypt-hashed credentials.
2. **`patients`**: Emergency department patient records with demographic data, arrival timestamp, emergency level, priority (1–4), and workflow status.
3. **`emergency_assessments`**: Audit log of triage assessments conducted by nurses or doctors.
4. **`treatments`**: Clinical interventions and procedures initiated by doctors.
5. **`patient_status_history`**: Audit trail capturing every status transition, timestamp, and responsible user.
6. **`activity_logs`**: System-wide operational events (`LOGIN`, `LOGOUT`, `PATIENT_REGISTERED`, `TREATMENT_STARTED`, etc.).

## MySQL Setup Instructions

### Option 1: MySQL Command Line (Recommended)
Open PowerShell or Command Prompt:

```powershell
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```

### Option 2: MySQL Workbench
1. Open MySQL Workbench and connect to your local MySQL instance.
2. Go to **File -> Open SQL Script...** and select `database/schema.sql`.
3. Click the **Execute (Lightning Bolt)** icon.
4. Go to **File -> Open SQL Script...** and select `database/sample_data.sql`.
5. Click **Execute**.

### Option 3: phpMyAdmin / XAMPP
1. Open `http://localhost/phpmyadmin` in your web browser.
2. Click the **Import** tab.
3. Choose `database/schema.sql` and click **Import**.
4. Repeat for `database/sample_data.sql`.

## Demo Accounts

| Username | Password | Role | Full Name |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | ADMIN | Hospital Administrator |
| `doctor` | `doctor123` | DOCTOR | Dr. Robert Vance, MD |
| `doctor2` | `doctor123` | DOCTOR | Dr. Sarah Jenkins, MD |
| `nurse` | `nurse123` | NURSE | Nurse Clara Barton, RN |
| `nurse2` | `nurse123` | NURSE | Nurse David Miller, RN |
