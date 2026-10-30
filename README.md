# Hospital Emergency Patient Priority Management System (HE-PPMS)

[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-green.svg)](https://openjfx.io/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0%2B-orange.svg)](https://www.mysql.com/)
[![Build Tool](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Academic%20Evaluation-lightgrey.svg)]()

An enterprise-grade, academic and real-world inspired desktop application designed to streamline emergency triage, dynamically maintain an active priority queue, coordinate multi-role healthcare operations (Admin, Doctor, Nurse), and generate live departmental analytics.

---

## 📋 Table of Contents
1. [Medical Disclaimer](#-medical-disclaimer)
2. [Problem Statement & The Core Challenge](#-problem-statement--the-core-challenge)
3. [Priority Queue (DSA Core Engine)](#-priority-queue-dsa-core-engine)
4. [Key Features & Highlights](#-key-features--highlights)
5. [User Roles & Access Control](#-user-roles--access-control)
6. [System Architecture & Design Patterns](#-system-architecture--design-patterns)
7. [Database Design](#-database-design)
8. [Installation & Setup Guide](#-installation--setup-guide)
9. [How to Run](#-how-to-run)
10. [Demo Credentials](#-demo-credentials)
11. [Patient Status Workflow](#-patient-status-workflow)
12. [Automated Testing Suite](#-automated-testing-suite)
13. [Future Enhancements & Limitations](#-future-enhancements--limitations)

---

## ⚠️ Medical Disclaimer

> **IMPORTANT DISCLAIMER**  
> This software is an academic triage and emergency queue management project developed to demonstrate software engineering, relational database persistence, Object-Oriented Programming (OOP), and Data Structures & Algorithms (DSA) priority scheduling concepts.  
> Emergency classification does not constitute medical diagnosis and does not replace professional medical judgment. Emergency levels and clinical decisions must always be confirmed by authorized healthcare staff.

---

## 🏥 Problem Statement & The Core Challenge

In standard non-urgent healthcare clinics, a First-In-First-Out (FIFO) queue ordering is typically adequate. However, in a **Hospital Emergency Department (ED)**, FIFO scheduling can result in catastrophic clinical delays.

A critically injured polytrauma or cardiac arrest patient may arrive at 10:05 AM, well after a patient with a minor ankle sprain who arrived at 09:55 AM. If treated strictly in chronological order, life-threatening delays occur.

Therefore, the Emergency Department must maintain a **Dynamic Priority Queue** where:
1. **Primary Sort Rule**: Lower priority numbers are attended to first (`1 = CRITICAL` precedes `2 = HIGH`, which precedes `3 = MEDIUM`, which precedes `4 = LOW`).
2. **Secondary Sort Rule**: If two patients share the same emergency severity, the patient who arrived earlier is served first (FIFO within priority tier).

---

## ⚡ Priority Queue (DSA Core Engine)

The core algorithm is implemented in a dedicated service manager: [`PriorityQueueManager`](src/main/java/com/hospital/service/PriorityQueueManager.java).

### Priority Tiers:
| Level | Priority Code | Badge Color | Clinical Indication |
| :--- | :---: | :--- | :--- |
| **CRITICAL** | `1` | 🔴 Red (`#EF4444`) | Immediate life-threatening trauma, cardiac arrest, respiratory collapse |
| **HIGH** | `2` | 🟠 Orange (`#F97316`) | Severe trauma, active hemorrhage, compound fractures, severe dyspnea |
| **MEDIUM** | `3` | 🟡 Yellow (`#F59E0B`) | Urgent condition, deep lacerations, suspected appendicitis, high fever |
| **LOW** | `4` | 🟢 Green (`#10B981`) | Non-urgent, stable condition, minor sprains, mild abrasions |

### Exact Problem Specification Scenario:
Consider four patients arriving in the emergency department:
- **Patient A**: Critical (P1) at 10:05
- **Patient B**: High (P2) at 10:01
- **Patient C**: Critical (P1) at 10:10
- **Patient D**: Medium (P3) at 09:55

#### Deterministic Queue Order:
1. **Position #1**: Patient A → `CRITICAL` (P1, 10:05)
2. **Position #2**: Patient C → `CRITICAL` (P1, 10:10)
3. **Position #3**: Patient B → `HIGH` (P2, 10:01)
4. **Position #4**: Patient D → `MEDIUM` (P3, 09:55)

The application calculates dynamic positions (`#1`, `#2`, `#3`...) and recalculates waiting times in real time.

---

## 🌟 Key Features & Highlights

- **Dynamic Live Emergency Queue**: Real-time table displaying Position, Token Number (`ER-001`), Patient Code (`P0001`), Name, Severity badge, Arrival Time, Elapsed Wait Time, and Quick Actions.
- **Overdue Waiting Time Alerts**: Live threshold monitoring flags patients waiting beyond configured thresholds (e.g. Critical $> 15$ min) with visual warning badges.
- **Doctor's "NEXT PATIENT" Card**: The doctor's dashboard automatically surfaces the patient at the head of the Priority Queue with one-click actions: `[Start Treatment]`, `[View Details]`, `[Complete Treatment]`, and `[Discharge]`.
- **Intake & Auto-Token System**: Nurses register incoming arrivals with auto-generated patient IDs (`P0001`, `P0002`...), emergency tokens (`ER-001`, `ER-002`...), allergies, and emergency contacts.
- **Vital Signs Monitoring & Dynamic Re-sorting**: Continuous vital signs capture (BP, Pulse, SpO2, Temperature, Respiratory Rate) with dynamic queue re-sorting if a patient's clinical condition changes.
- **Hospital Bed Inventory & Ward Capacity**: Real-time tracking of General Ward and ICU bed availability, occupancy, cleaning states, and interactive bed assignment/release modals.
- **Outpatient Appointment Scheduling**: Consultation booking desk with doctor specialty filtering, appointment status transitions, and strict safety isolation from the emergency queue.
- **Patient Self-Service Portal**: Dedicated patient dashboard to check live queue status, vital signs, upcoming appointments, and past treatment history.
- **Visual Role Authorization Matrix**: Dedicated UI view rendering all 26 system permissions across Admin, Doctor, Nurse, and Patient with green checkmarks (`✔ ALLOWED`) and red cross marks (`✖ RESTRICTED`).
- **Non-Blocking Toast Notifications**: When a Critical patient is registered, a department-wide non-blocking slide-down alert is triggered: *"CRITICAL PATIENT ADDED TO EMERGENCY QUEUE"*.
- **Admin Visual Analytics**: Dynamic JavaFX `PieChart` and `BarChart` components aggregating patient emergency severity, workflow distribution, and average waiting times.
- **Multi-Filter Reports & CSV Export**: Generate department reports and export structured patient records to CSV with one click.
- **Immutable Activity & Audit Logs**: Detailed audit logging of all system logins, triage registrations, priority evaluations, status transitions, and bed/appointment actions.
- **Dual Persistence Strategy**: Native MySQL 8.0+ JDBC connectivity with an automatic embedded in-memory database fallback (H2 in MySQL mode) for zero-setup demonstrations.

---

## 👥 User Roles & Access Control

Strict Role-Based Access Control (RBAC) is enforced at both the UI element level (hiding unauthorized views and buttons) and the backend service layer via `SessionManager.requirePermission()`:

| Privilege / Feature | Admin 👑 | Doctor 🩺 | Nurse 💉 | Patient 🧑‍⚕️ |
| :--- | :---: | :---: | :---: | :---: |
| **System Login & Logout** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED |
| **Role-Specific Dashboard** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED |
| **View Live Emergency Queue** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Patient Registration & Intake** | ✖ RESTRICTED | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED |
| **Triage Severity Assessment** | ✖ RESTRICTED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Update Patient Vital Signs** | ✖ RESTRICTED | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED |
| **Doctor "Next Patient" Priority Card** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Start Clinical Treatment** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Complete Treatment & Prescribe** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Discharge Patient** | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Manage Bed Allocations (Ward/ICU)** | ✔ ALLOWED | ✖ RESTRICTED | ✔ ALLOWED | ✖ RESTRICTED |
| **Schedule Outpatient Appointments** | ✔ ALLOWED | ✖ RESTRICTED | ✔ ALLOWED | ✔ ALLOWED |
| **Department Reports & CSV Export** | ✔ ALLOWED | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED |
| **Staff & User Account Management** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **System Activity & Audit Logs** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **System Settings & Thresholds** | ✔ ALLOWED | ✖ RESTRICTED | ✖ RESTRICTED | ✖ RESTRICTED |
| **View Role Authorization Matrix** | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED | ✔ ALLOWED |

> [!NOTE]
> The **Role Authorization** screen in the application renders this complete live matrix with green checkmark pills and red restriction badges, matching the visual requirements in the project specification.

---

## 🏗️ System Architecture & Design Patterns

The system implements a layered architectural design:
- **Presentation**: JavaFX 21 FXML views styled with modern CSS (`src/main/resources/css/main.css`).
- **Controllers**: Thin UI coordinators inheriting from `BaseController`.
- **Services**: Encapsulate business logic, input validation, and transaction coordination.
- **Data Access Objects (DAO)**: Abstract persistence using JDBC `PreparedStatement`.
- **Security**: BCrypt salted hashing (`PasswordUtil`) and session handling (`SessionManager`).

```
HospitalEmergencyPatientPriorityManagementSystem/
├── pom.xml                               # Maven project descriptor
├── README.md                             # Project documentation
├── .gitignore                            # Git exclusions
├── config.properties.example             # Configuration template
├── database/
│   ├── schema.sql                        # Full MySQL DDL
│   ├── sample_data.sql                   # Realistic demo dataset
│   └── README.md                         # Database setup guide
├── docs/
│   ├── architecture.md                   # System architecture deep-dive
│   ├── database.md                       # ERD and schema specification
│   └── user-guide.md                     # Operational workflow manuals
└── src/
    ├── main/
    │   ├── java/com/hospital/
    │   │   ├── Main.java                 # JavaFX Application entry
    │   │   ├── MainLauncher.java         # Shaded JAR wrapper
    │   │   ├── model/                    # OOP Entities: User, Patient, Treatment, etc.
    │   │   ├── dao/                      # JDBC Data Access Objects & DatabaseConnection
    │   │   ├── service/                  # Business Logic & PriorityQueueManager
    │   │   ├── controller/               # JavaFX UI Controllers
    │   │   ├── security/                 # BCrypt & SessionManager
    │   │   ├── exception/                # Domain-specific Custom Exceptions
    │   │   └── util/                     # ConfigReader, AlertHelper, NotificationManager
    │   └── resources/
    │       ├── config.properties         # Runtime configuration
    │       ├── schema.sql                # Embedded schema copy
    │       ├── sample_data.sql           # Embedded sample data copy
    │       ├── css/main.css              # Hospital design stylesheet
    │       └── fxml/                     # Modular FXML view definitions
    └── test/
        └── java/com/hospital/            # JUnit 5 Automated Test Suite
```

---

## 🗄️ Database Design

The relational database `hospital_emergency_system` consists of 6 tables in 3NF:
1. `users`: Healthcare staff accounts with BCrypt password hashes and role definitions.
2. `patients`: Demographic details, triage level, priority (1-4), arrival time, and workflow status.
3. `emergency_assessments`: Triage audit records capturing symptoms, severity level, and assessor ID.
4. `treatments`: Medical procedures, attending doctor, start/end timestamps, and care notes.
5. `patient_status_history`: Audit trail capturing every status transition, timestamp, and responsible user.
6. `activity_logs`: Operational event log (`LOGIN`, `PATIENT_REGISTERED`, `TREATMENT_STARTED`, etc.).

---

## ⚙️ Installation & Setup Guide

### Prerequisites
- **Java**: JDK 17 or higher (Verified compatible with Java 17, 21, 26).
- **Maven**: Version 3.8+ (A portable `mvnw.cmd` wrapper is included in the project root).
- **MySQL Server** (Optional for local persistent storage; if offline, embedded fallback mode activates automatically).

### Step 1: Database Setup (MySQL)
If running a local MySQL server (e.g. MySQL 8.0, XAMPP, or MySQL Workbench):
```powershell
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```

### Step 2: Database Configuration
Verify or edit `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/hospital_emergency_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=
```

> [!NOTE]
> If MySQL is not installed or the service is stopped, the application will automatically activate **Embedded In-Memory Demo Mode** so you can run, evaluate, and test the entire project without any database installation!

---

## 🚀 How to Run

### Method 1: Using the Maven Wrapper (Recommended)
From the project root directory:

```powershell
# Compile and run via JavaFX plugin
.\mvnw.cmd javafx:run
```

Or using standard Maven if installed on your PATH:
```powershell
mvn javafx:run
```

### Method 2: Running the Packaged Executable JAR
```powershell
# Package the executable fat JAR
.\mvnw.cmd package -DskipTests=true

# Launch the standalone application
java -jar target/hospital-emergency-system-1.0.0-jar-with-dependencies.jar
```

---

## 🔑 Demo Credentials

| Role | Username | Password | Full Name & Title / Scope |
| :--- | :--- | :--- | :--- |
| **Admin** 👑 | `admin` | `admin123` | Hospital Administrator (Full System Oversight) |
| **Doctor** 🩺 | `doctor` | `doctor123` | Dr. Robert Vance, MD (Emergency Medicine) |
| **Doctor 2** 🩺 | `doctor2` | `doctor123` | Dr. Sarah Jenkins, MD (Trauma Specialist) |
| **Nurse** 💉 | `nurse` | `nurse123` | Nurse Clara Barton, RN (Triage Intake) |
| **Nurse 2** 💉 | `nurse2` | `nurse123` | Nurse David Miller, RN (Triage Station 2) |
| **Patient** 🧑‍⚕️ | `patient` | `doctor123` | John Doe (Self-Service Patient Portal) |

---

## 🔄 Patient Status Workflow

```
REGISTERED
    ↓
 WAITING  ─── (Doctor calls patient) ───→  CALLED
    ↓                                        ↓
    └─────────── (Clinical Intake) ──────────┘
                         ↓
                    IN_TREATMENT
                         ↓
                      TREATED
                         ↓
                    DISCHARGED
```

---

## 🧪 Automated Testing Suite

The project includes a comprehensive automated JUnit 5 test suite with **26 passing tests (0 failures, 0 errors)** covering priority queue ordering, FIFO-within-priority, clinical validation, RBAC permissions, bed inventory, and outpatient appointments.

Run the test suite using:
```powershell
.\mvnw.cmd test
```

### Test Classes:
1. **`PriorityQueueManagerTest`**: Verifies Critical before High before Medium before Low; verifies arrival-time ordering for identical priorities; asserts the exact specification scenario (A, C, B, D).
2. **`PatientValidationTest`**: Verifies clinical validation rules (blank names, negative age, invalid phone, missing symptoms, null severity).
3. **`AuthenticationServiceTest`**: Verifies BCrypt salted hashing, authentication success, wrong credentials rejection, and role matching.
4. **`StatusWorkflowTest`**: Verifies valid transitions and asserts that invalid state jumps (e.g. `DISCHARGED` back to `IN_TREATMENT`) are strictly prevented.
5. **`RoleAuthorizationTest`**: Verifies granular RBAC enforcement across Admin, Doctor, Nurse, and Patient roles, ensuring unauthorized operations throw `UnauthorizedException`.
6. **`BedManagementTest`**: Tests bed inventory tracking, ward vs. ICU capacity metrics, patient bed allocation, and bed discharge/cleaning release.
7. **`AppointmentServiceTest`**: Validates outpatient booking with doctor scheduling, duplicate time slot prevention, cancellation, and isolation from emergency queue.
8. **`TreatmentServiceTest`**: Tests clinical workflow start, complete, and discharge by doctor, ensuring nurses and admins cannot start or complete treatments.

---

## 🔄 Continuous Integration (CI/CD)

Automated testing and compilation are configured via GitHub Actions in [`.github/workflows/ci.yml`](.github/workflows/ci.yml). Every push or pull request to `master` automatically triggers:
- JDK 17 environment setup.
- Clean Maven compilation.
- Execution of all 26 unit tests.
- Standalone shaded JAR packaging.

---

## 🔮 Future Enhancements & Limitations

### Future Enhancements:
- **SMS / Push Notifications**: Automated alerts to waiting patients when called to examination bays.
- **Multi-Device WebSocket Sync**: Live synchronized queue displays across waiting room TV monitors and triage desks.
- **Wearable Vitals Integration**: Real-time telemetry ingestion from bedside monitors.
- **Clinical Decision Support (CDS) Research**: Machine-learning assisted triage recommendations subject to clinical validation.

### Current Limitations:
- Single hospital emergency department operational scope.
- Triage priority is an operational queue scheduling classification and requires human medical verification.
