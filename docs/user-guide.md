# User Guide & Operational Manual

## Hospital Emergency Patient Priority Management System (HE-PPMS)

### 1. Introduction
The **Hospital Emergency Patient Priority Management System** provides hospital emergency department personnel with a robust, priority-driven clinical queue. This guide details operational workflows for all three authorized roles: **Admin**, **Doctor**, and **Nurse**.

---

### 2. Login & Credentials

1. Launch the application.
2. In the **Staff Sign In** screen, enter your authorized username and password.
3. You may select your role from the dropdown or leave it as `Any / Auto-Detect`.
4. Click **Sign In to Portal**.

#### Demo Accounts Table
| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Department oversight, staff administration, audit logs, reports |
| **Doctor** | `doctor` | `doctor123` | Clinical intake, NEXT PATIENT triage intake, treatments, discharge |
| **Doctor 2** | `doctor2` | `doctor123` | Trauma specialist clinical workstation |
| **Nurse** | `nurse` | `nurse123` | Triage intake, patient registration, priority assignment |
| **Nurse 2** | `nurse2` | `nurse123` | Emergency ward intake |

> [!TIP]
> Use the **Quick Demo Credentials** buttons at the bottom of the login screen to auto-fill credentials with one click.

---

### 3. Nurse Workflow (Intake & Triage)

#### Step 1: Registering an Incoming Emergency Patient
1. Click **➕ Patient Registration** from the sidebar or click **➕ Register Incoming Patient** on the Nurse Dashboard.
2. The system automatically populates the **Patient Code** (e.g. `P0013`) and **Emergency Token** (e.g. `ER-013`).
3. Enter patient demographics:
   - **Full Name** (Required)
   - **Age** (Years) and **Gender** (Required)
   - **Phone Number** (Required)
   - **Blood Group** and **Address** (Optional)
   - **Assign Doctor** (Optional, selected from active doctors)
4. Under **Emergency Severity & Triage Assessment**:
   - Describe chief complaints and observations in **Symptoms Description**.
   - Select the **Emergency Severity Level**:
     - `CRITICAL` (Priority 1) — Immediate life-threatening condition (Cardiorespiratory arrest, massive hemorrhage).
     - `HIGH` (Priority 2) — Severe trauma, compound fractures, respiratory compromise.
     - `MEDIUM` (Priority 3) — Urgent conditions (Deep lacerations, acute abdomen).
     - `LOW` (Priority 4) — Non-urgent, stable conditions (Minor sprains, superficial wounds).
5. Click **➕ Register & Enqueue Patient**.
6. The patient is instantly saved to the database and entered into the active **Priority Queue**. If the patient is `CRITICAL`, a high-priority in-app alert is broadcast across the department.

#### Step 2: Monitoring the Live Emergency Queue
1. Click **⚡ Live Emergency Queue** in the sidebar.
2. Review the live sorted queue:
   - Priority 1 cases are placed at the top, followed by Priority 2, Priority 3, and Priority 4.
   - Patients with identical priority are strictly ordered by earliest arrival time.
   - Any patient waiting beyond department thresholds displays an alert indicator: `⚠️ XXm (ALERT)`.
3. When a bed or triage bay opens, click **Call** to transition the patient from `WAITING` to `CALLED`.

---

### 4. Doctor Workflow (Clinical Care & Treatment)

#### Step 1: Evaluating the "Next Patient"
1. Log in as a Doctor (`doctor` / `doctor123`).
2. The **Doctor Dashboard** displays the **NEXT PATIENT (Triage Priority Head)** card.
3. This card automatically surfaces the highest priority patient from the priority queue, displaying their token, demographics, vital symptoms, arrival time, and elapsed wait time.
4. Click **🩺 Start Treatment**:
   - The patient's status transitions to `IN_TREATMENT`.
   - The patient is automatically removed from the waiting queue so the next patient advances to Position #1.
   - An active clinical treatment session is opened.

#### Step 2: Treatment & Discharge
1. In the **Patients Currently in Treatment** table:
   - Click **Complete** when diagnostic or therapeutic procedures are finished (status moves to `TREATED`).
   - Click **Discharge** when vitals are stable and outpatient instructions are issued (status moves to `DISCHARGED`).
2. Alternatively, open the **🩺 Treatment Station** from the sidebar to review active treatments and update clinical care notes.

---

### 5. Admin Workflow (Management, Analytics & Auditing)

#### Step 1: Department Overview & Live Charts
1. Log in as Admin (`admin` / `admin123`).
2. The **Admin Dashboard** displays real-time statistics directly from the database:
   - Active priority counts (Critical, High, Medium, Low)
   - Operational status breakdown (Waiting, Called, In Treatment, Treated, Discharged)
   - Interactive JavaFX charts for emergency distribution and average wait times.

#### Step 2: Staff & User Account Management
1. Click **👤 Staff Management** in the sidebar.
2. View all registered doctors, nurses, and administrators.
3. To add new staff, fill in the legal name, username, temporary password, and role, then click **➕ Add Staff Member**.
4. To deactivate a departing staff member, click **Deactivate** in their table row.

#### Step 3: Reports & CSV Export
1. Click **📈 Reports & Analytics** in the sidebar.
2. Select report type:
   - *All Patients Master Report*
   - *Daily Emergency Intake Report*
   - *Emergency Priority Severity Report*
   - *Patient Status Workflow Report*
   - *Waiting Time Analysis Report*
3. Apply severity or status filters.
4. Click **📥 Export CSV Report** to save a spreadsheet file to your local computer.

#### Step 4: Activity Logs & Auditing
1. Click **📋 Activity Logs** in the sidebar.
2. Review the chronological audit log of all system logins, registrations, triage assignments, and treatments.
3. Filter by action type (`LOGIN`, `PATIENT_REGISTERED`, `TREATMENT_STARTED`, etc.) or search keywords.

---

### 6. System Settings

1. Click **⚙️ System Settings** in the sidebar.
2. Adjust waiting-time overdue alert thresholds:
   - *Critical (min)*
   - *High (min)*
   - *Medium (min)*
   - *Low (min)*
3. Adjust the **Live Queue Auto-Refresh Interval** (default: 10 seconds).
4. Click **💾 Save Configuration**.
