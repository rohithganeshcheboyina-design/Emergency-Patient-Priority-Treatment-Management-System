# Emergency Patient Priority Treatment Management System (HE-PPMS)

## Project Audit, RBAC Implementation & Production Readiness Report

### Executive Summary

In accordance with strict preservation rules, the existing **Hospital Emergency Patient Priority Treatment Management System** has been audited, completed, hardened, and verified. No existing working features were removed or broken; all existing architecture and design conventions were preserved.

The system is a high-performance **Java 17 enterprise application** utilizing **JavaFX 21** with a clean healthcare design system (`main.css`), layered architecture (**Controller → Service → DAO → Database**), dual-mode **MySQL / H2** fallback, granular **Role-Based Access Control (RBAC)** across 4 roles, and an automated **JUnit 5 test suite** with 26 passing test cases.

---

## 1. Role-Based Access Control (RBAC) Implementation

A centralized, granular permission model (`com.hospital.model.Permission`) with 26 distinct permissions governs both UI element visibility and backend service execution via `SessionManager`.

### Role Permissions Matrix

| Module / Permission | ADMIN | DOCTOR | NURSE | PATIENT |
| :--- | :---: | :---: | :---: | :---: |
| **Admin Dashboard** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Doctor Dashboard** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Nurse Dashboard** | ✖ RESTRICTED | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED |
| **Patient Portal** | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED | ✔ ALLOWED |
| **Register Patients (Intake)** | ✖ RESTRICTED | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED |
| **View Emergency Queue** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **View All Patients** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Patient Details & Symptoms** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Update Vitals & Nursing Notes** | ✖ RESTRICTED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Call Patient to Triage Bay** | ✖ RESTRICTED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Start Clinical Treatment** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Record Diagnosis & Medications**| ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Complete Clinical Treatment** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Discharge Patient** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Bed Management & Occupancy** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Assign / Release Beds** | ✖ RESTRICTED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Appointment Scheduling** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED (Own) |
| **Reports & Statistics (CSV)** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Audit Activity Logs** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **User & Staff Management** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **System Settings** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Role Authorization Matrix View**| ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED |

### Frontend & Backend Guarding

1. **Frontend**:
   - Dynamic sidebar navigation filtering in `MainLayoutController.setupUserSessionAndPermissions()`.
   - Dynamic button hiding/disabling in `LiveQueueController`, `PatientManagementController`, `PatientDetailController`, `DoctorDashboardController`, and `TreatmentController`.
   - Access denied notifications in `MainLayoutController.loadView()`.
2. **Backend**:
   - `SessionManager.requirePermission(Permission)` guards business logic in `PatientService`, `TreatmentService`, `BedService`, `AppointmentService`, and `AuthenticationService`.
   - Attempted direct bypass results in an immediate `UnauthorizedException`.

---

## 2. Completed Modules & Clinical Capabilities

### A. Patient Registration & Intake
- Added **Known Allergies** (`txtAllergies`) and **Emergency Contact & Relationship** (`txtEmergencyContact`).
- Real-time visual priority badge dynamically changes color (Red/Orange/Yellow/Green) as triage severity is selected.
- Automatic generation of sequential Patient Codes (`P0001`, `P0002`...) and Emergency Tokens (`ER-001`, `ER-002`...).
- Instant enqueuing into active priority queue with non-blocking toast notifications for critical triage alerts.

### B. Emergency Triage & Vital Signs Monitoring
- Vital signs tracking on patient records:
  - **Blood Pressure** (e.g., `165/100 mmHg`)
  - **Heart Rate** (e.g., `110 bpm`)
  - **Body Temperature** (e.g., `98.6 °F`)
  - **Oxygen Saturation** (e.g., `92% SpO2`)
  - **Respiratory Rate** (e.g., `24 /min`)
- Modal dialog in `PatientDetailController.handleUpdateVitals()` allowing nurses and doctors to update vitals and symptoms reassessments at any time, updating the queue priority dynamically.

### C. Live Emergency Queue
- Strict 3-tier queue ordering:
  1. **Primary**: Severity level (`1=CRITICAL`, `2=HIGH`, `3=MEDIUM`, `4=LOW`).
  2. **Secondary**: Earlier arrival time (FIFO within priority).
  3. **Tie-breaker**: Deterministic patient ID.
- Real-time wait-time calculation with threshold warnings (e.g., Critical wait $>15$ min triggers visual alert).
- Row highlighting for critical patients and double-click navigation to full clinical profile.
- Action buttons dynamically adapt to role: Doctor sees **Treat** and **Call**; Nurse sees **Call**; Admin sees view-only.

### D. Treatment & Physician Station
- Extended treatment records with clinical **Diagnosis** and **Prescribed Medications/Rx Orders**.
- Quick action card in `TreatmentManagement.fxml` allows doctors to input diagnosis, medication orders, and clinical notes, complete care, or discharge patients.
- Full status transition audit history preserved in `patient_status_history`.

### E. Bed & Ward Capacity Management
- Tracks 12 realistic bed units across **Emergency Bays**, **ICU Beds**, **Trauma Resuscitation Bays**, **Observation Unit**, and **General Ward Beds**.
- Interactive assignment dialog with patient selection and release confirmation.
- Filter by bed type and occupancy status (`AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`).

### F. Outpatient Consultations & Appointment Desk
- Outpatient appointment scheduling with doctor availability filters.
- **Critical Safety Guard**: Scheduled appointments operate independently and are strictly isolated from emergency priority treatment.
- Patient Portal integration allowing patients to review appointments and cancel if necessary.

### G. Patient Self-Service Portal
- Dedicated `PatientDashboardController` for patient users (`Role.PATIENT`).
- Displays personal emergency status notice, scheduled consultations, and verified discharge medical summaries.
- One-click consultation booking and cancellation.

### H. Reports, Audits & Settings
- Real-time departmental KPI metrics and JavaFX charts (Pie Chart for severity distribution, Bar Chart for status and average wait times).
- Export to formatted CSV file via file dialog (`ReportService.exportPatientsToCSV`).
- Activity logging (`activity_logs`) tracking logins, registrations, triage changes, treatments, and discharges.
- Configurable operational wait-time thresholds and auto-refresh intervals.

---

## 3. Database & Connectivity Architecture

- **Primary**: MySQL Server via Connector/J 8.3 (`hospital_emergency_system` database).
- **Graceful Fallback**: Automatic, zero-configuration in-memory H2 database (`MODE=MySQL`) when MySQL is unreachable.
- Fixed dialect translation in `DatabaseConnection.java` so `DATE_SUB` / `DATE_ADD` with `INTERVAL` and `SET FOREIGN_KEY_CHECKS` / `TRUNCATE TABLE` operate seamlessly on both MySQL and H2 without any data truncation or foreign key violations.

---

## 4. Automated Testing Suite

The automated test suite runs via standard Maven (`.\mvnw.cmd test`):

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.hospital.AppointmentServiceTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.AuthenticationServiceTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.BedManagementTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.PatientValidationTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.PriorityQueueManagerTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.RoleAuthorizationTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.StatusWorkflowTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
Running com.hospital.TreatmentServiceTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

Results:
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 5. Build, Packaging & CI/CD Pipeline

1. **Executable JAR with Dependencies**:
   `mvn package -DskipTests` produces:
   - `target/hospital-emergency-system-1.0.0.jar`
   - `target/hospital-emergency-system-1.0.0-jar-with-dependencies.jar` (16.6 MB self-contained executable)
2. **GitHub Actions CI/CD**:
   Created `.github/workflows/ci.yml` to automatically build, test, and verify the project across branches and pull requests using Java 17.

---

## 6. How to Run the Application

### Command to Run in Command Prompt / Terminal

Navigate to the project folder:
```cmd
cd "c:\Users\rohit\OneDrive\Desktop\project java"
```

To run using Maven JavaFX runner:
```cmd
mvn javafx:run
```
*(or using the bundled Maven wrapper: `.\mvnw.cmd javafx:run`)*

To run using the compiled fat JAR:
```cmd
java -jar target\hospital-emergency-system-1.0.0-jar-with-dependencies.jar
```

### Demo Login Credentials

| Role | Username | Password | Default Landing Dashboard |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Admin Oversight Dashboard & Analytics |
| **Doctor** | `doctor` | `doctor123` | Doctor Clinical Dashboard & Next Patient Card |
| **Doctor 2** | `doctor2` | `doctor123` | Doctor Clinical Dashboard |
| **Nurse** | `nurse` | `nurse123` | Nurse Triage Intake & Active Queue Preview |
| **Nurse 2** | `nurse2` | `nurse123` | Nurse Triage Intake & Active Queue Preview |
| **Patient** | `patient` | `doctor123` | Patient Health Portal & Emergency Care Tracker |

*(The login screen also features 4 one-click demo fill buttons for quick role switching).*
