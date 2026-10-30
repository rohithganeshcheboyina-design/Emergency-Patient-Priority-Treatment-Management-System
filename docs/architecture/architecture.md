# System Architecture Document

## Hospital Emergency Patient Priority Management System (HE-PPMS)

### 1. High-Level Architecture Overview

The **Hospital Emergency Patient Priority Management System** follows a clean, decoupled **Layered MVC (Model-View-Controller) + Service + Data Access Object (DAO)** architectural pattern.

```mermaid
graph TD
    subgraph Presentation Layer
        FXML[JavaFX Views / FXML Layouts]
        CSS[Theme CSS Stylesheets]
        CTRL[UI Controllers: Login, Dashboards, Queue, Patients, Beds, Appointments, RoleAuth]
    end

    subgraph Service & Business Logic Layer
        AUTH[AuthenticationService & SessionManager]
        PAT[PatientService]
        TRT[TreatmentService]
        BED[BedService]
        APT[AppointmentService]
        RPT[ReportService & Analytics]
        PQM[PriorityQueueManager - Core DSA Engine]
    end

    subgraph Data Access Layer
        UDAO[UserDAO]
        PDAO[PatientDAO]
        EDAO[EmergencyAssessmentDAO]
        TDAO[TreatmentDAO]
        BDAO[BedDAO]
        ADAO[AppointmentDAO]
        HDAO[PatientStatusHistoryDAO]
        LDAO[ActivityLogDAO]
        RDAO[ReportDAO]
    end

    subgraph Persistence Layer
        DBC[DatabaseConnection Factory]
        MYSQL[(MySQL Database 8.0+)]
        H2[(Embedded In-Memory Fallback)]
    end

    FXML --> CTRL
    CSS --> FXML
    CTRL --> AUTH
    CTRL --> PAT
    CTRL --> TRT
    CTRL --> BED
    CTRL --> APT
    CTRL --> RPT
    CTRL --> PQM

    PAT --> PDAO
    PAT --> EDAO
    PAT --> HDAO
    PAT --> LDAO
    PAT --> PQM

    TRT --> TDAO
    TRT --> PDAO
    TRT --> HDAO
    TRT --> LDAO
    TRT --> PQM

    BED --> BDAO
    BED --> LDAO

    APT --> ADAO
    APT --> LDAO

    PDAO --> DBC
    UDAO --> DBC
    EDAO --> DBC
    TDAO --> DBC
    BDAO --> DBC
    ADAO --> DBC
    HDAO --> DBC
    LDAO --> DBC
    RDAO --> DBC

    DBC -.->|Primary Target| MYSQL
    DBC -.->|Graceful Fallback| H2
```

---

### 2. Architectural Layers

#### A. Presentation Layer (JavaFX / FXML / CSS)
- **Decoupled Declarative Views**: User interfaces are designed in modular FXML files with separation of layout and presentation styles.
- **Role-Adaptive Layout (`MainLayout.fxml`)**: A shell hosting a responsive left sidebar, header with live clock, and a dynamic content `StackPane`. Menu items and operations dynamically adapt based on whether the logged-in user is an **Admin**, **Doctor**, or **Nurse**.
- **Modern Healthcare Theme (`main.css`)**: Professional clinical color palette featuring dedicated triage severity indicators (Critical Red, High Orange, Medium Amber, Low Green), rounded cards, clean data tables, and in-app non-blocking alert toasts.

#### B. Controller Layer
- **`BaseController`**: Common abstract base class providing access to parent layout navigation, session status, time formatters, and style badge helpers.
- **Controllers**:
  - `LoginController`: Authenticates credentials against the database, validates non-empty inputs, verifies selected roles, and controls session bootstrap.
  - `AdminDashboardController`: Aggregates real-time statistics and renders interactive JavaFX charts (`PieChart`, `BarChart`).
  - `DoctorDashboardController`: Displays department KPIs and features the **NEXT PATIENT** triage card driven by the Priority Queue with direct links to patient details.
  - `NurseDashboardController`: Triage workstation with quick patient registration shortcuts, live queue preview, and vital sign monitoring.
  - `PatientDashboardController`: Self-service portal for patients to view active triage status, vital signs, outpatient appointments, and historical treatment notes.
  - `PatientRegistrationController`: Validates patient demographics, captures symptoms, assesses severity level, records allergies and emergency contact, generates unique tokens, and automatically enqueues patients.
  - `LiveQueueController`: Real-time queue table with dynamic sorting, search, severity filters, wait-time threshold alerts, and double-click patient detail drill-down.
  - `PatientManagementController`: Directory table with multi-parameter filtering and full profile inspection.
  - `PatientDetailController`: Comprehensive patient timeline showing demographics, vital signs (BP, Pulse, SpO2, Temp, RR), allergies, emergency contact, clinical diagnosis, prescribed medications, and interactive vital update modal.
  - `TreatmentController`: Dedicated doctor workstation for recording clinical diagnosis, medications, care procedures, completion, and discharge.
  - `BedManagementController`: Real-time emergency ward and ICU bed capacity monitor with interactive patient bed allocation and release.
  - `AppointmentManagementController`: Outpatient booking desk with doctor specialty filtering and cancellation management.
  - `RoleAuthorizationController`: Visual RBAC matrix displaying all 26 system permissions mapped across Admin, Doctor, Nurse, and Patient with green checkmarks (`✔ ALLOWED`) and red cross marks (`✖ RESTRICTED`).
  - `ReportsController`: Analytical reporting engine with multi-report generation and CSV export.
  - `ActivityLogsController`: Immutable audit log inspector.
  - `UserManagementController`: Administrative staff CRUD and role assignments.
  - `SettingsController`: Operational configuration for overdue thresholds and refresh cadence.
  - `AboutController`: System specifications and medical disclaimer.

#### C. Service & Business Logic Layer
- **`PriorityQueueManager` (Core DSA)**:
  - Manages the emergency queue using strict multi-tier comparison:
    1. **Primary Sort**: Emergency Level (`1 = CRITICAL`, `2 = HIGH`, `3 = MEDIUM`, `4 = LOW`).
    2. **Secondary Sort**: Arrival Timestamp (earliest arrival first).
    3. **Tie-Breaker**: Deterministic patient identifier.
  - Supports dynamic re-sorting via `updatePriority()` when a patient's vital signs deteriorate or change.
  - Re-heaps dynamically when new patients arrive, severity is re-assessed, treatment starts, or discharge occurs.
- **`PatientService`**:
  - Enforces field validation rules (name, age range, valid phone, non-empty symptoms).
  - Handles the patient status workflow state machine.
  - Manages vital signs updates and triggers queue re-sorting on triage severity changes.
- **`TreatmentService`**:
  - Manages clinical intervention lifecycles (`IN_PROGRESS` -> `COMPLETED` -> `DISCHARGED`).
  - Records clinical diagnosis and prescribed medication orders.
  - Strictly enforces that only authorized doctors can start, update, or complete treatments.
- **`BedService`**:
  - Tracks bed capacity, availability, cleaning status, and ward vs. ICU allocation.
- **`AppointmentService`**:
  - Schedules outpatient consultations while keeping them cleanly isolated from the emergency queue.
- **`AuthenticationService` & `SessionManager`**:
  - Verifies BCrypt salted hashes and enforces Role-Based Access Control (RBAC) with 26 granular `Permission` definitions.

#### D. Data Access Object (DAO) Layer
- Uses standard **JDBC `PreparedStatement`** for 100% parameterization against SQL injection vulnerabilities.
- Handles clean connection lifecycles via `try-with-resources`.
- Translates relational rows into rich object graphs.

#### E. Persistence Layer & Dual-Mode Connectivity
- **Primary**: MySQL Server 8.0+ via `mysql-connector-j`.
- **Graceful Fallback**: If MySQL is offline or during automated unit testing, `DatabaseConnection` automatically detects connectivity failure and boots an embedded H2 database in MySQL compatibility mode, executing `schema.sql` and `sample_data.sql` seamlessly.

---

### 3. Patient Workflow State Machine

```mermaid
stateDiagram-v2
    [*] --> WAITING : Nurse registers patient at triage
    WAITING --> CALLED : Doctor calls patient to examination bay
    CALLED --> IN_TREATMENT : Clinical treatment / procedure started
    WAITING --> IN_TREATMENT : Direct intake for critical polytrauma/arrest
    IN_TREATMENT --> TREATED : Medication / stabilization complete
    TREATED --> DISCHARGED : Discharge summary handed to patient
    WAITING --> CANCELLED : Patient leaves before triage evaluation
    CALLED --> CANCELLED : Patient leaves triage bay
    DISCHARGED --> [*]
    CANCELLED --> [*]
```

---

### 4. Priority Queue Time Complexity Analysis

| Operation | Description | Time Complexity |
| :--- | :--- | :--- |
| **`enqueue(Patient)`** | Inserts patient into priority queue and reorders positions | $O(n \log n)$ / $O(\log n)$ |
| **`peek()`** | Inspects highest-priority patient at head of queue (Doctor "Next Patient") | $O(1)$ |
| **`dequeue()`** | Removes top patient when treatment commences | $O(1)$ |
| **`remove(patientId)`** | Removes patient when discharged or admitted to surgery | $O(n)$ |
| **`filter(...)`** | Searches and filters queue by severity/status/keyword | $O(n)$ |
