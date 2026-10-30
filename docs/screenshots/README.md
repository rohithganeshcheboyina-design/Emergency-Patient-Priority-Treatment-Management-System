# Application Screenshots & UI Gallery

This directory houses interface screenshots, UI navigation mockups, and visual workflow diagrams for the **Hospital Emergency Patient Priority Treatment Management System**.

---

## 1. Role-Based UI Dashboard Previews

### 1.1 Secure Authentication (`Login.fxml`)
- Role-based login supporting `ADMIN`, `DOCTOR`, `NURSE`, and `PATIENT`.
- BCrypt credential verification with password show/hide toggle.
- Enter-key submission and automated error shake animations.

```text
+-------------------------------------------------------------+
|          Emergency Patient Priority Treatment System        |
|                                                             |
|   Username: [ admin               ]                         |
|   Password: [ **********          ] [Show]                  |
|                                                             |
|   [ Login Button ]                                          |
|                                                             |
|   Demo Logins: admin, doctor, nurse, patient (pw: ...123)   |
+-------------------------------------------------------------+
```

---

### 1.2 Live Emergency Queue (`LiveQueue.fxml`)
- Real-time priority-ordered queue visualization.
- Color-coded badges: **RED (P1)**, **ORANGE (P2)**, **YELLOW (P3)**, **GREEN (P4)**.
- Elapsed wait timers with flashing escalation indicators when exceeding safety limits.
- Quick action buttons: **Call Next**, **Re-Triage**, **View Vitals**, **Assign Doctor**.

```text
+-----------------------------------------------------------------------------------------+
| Live Emergency Queue (Auto-refresh: 10s)                                [ Register + ]  |
+----+--------------------+----------+------------+----------+-----------+---------------+
| ID | Patient Name       | Priority | Status     | Bed      | Wait Time | Action        |
+----+--------------------+----------+------------+----------+-----------+---------------+
| 01 | Marcus Thorne (48) | [P1 RED] | WAITING    | ICU-01   | 00:04:12  | [Call Doctor] |
| 02 | Elena Rostova (31) | [P2 ORG] | WAITING    | ER-02    | 00:14:50  | [Call Doctor] |
| 03 | David Chen (22)    | [P3 YEL] | CALLED     | GEN-04   | 00:32:10  | [In Treatment]|
| 04 | Sarah Jenkins (19) | [P4 GRN] | WAITING    | GEN-07   | 00:45:00  | [Call Doctor] |
+----+--------------------+----------+------------+----------+-----------+---------------+
```

---

### 1.3 Doctor Clinical Workspace (`DoctorDashboard.fxml` & `PatientDetail.fxml`)
- Active patient queue with chief complaints and vitals summary.
- Comprehensive patient clinical history, previous treatments, and allergies.
- Prescription recording, vital sign charting, diagnostic notes, and discharge actions.

---

### 1.4 Nurse Triage & Vitals Station (`NurseDashboard.fxml` & `PatientRegistration.fxml`)
- Rapid triage intake form: Glasgow Coma Scale, BP, HR, SpO2, Temperature, Pain (1-10).
- Immediate calculated triage suggestion with mandatory clinical nurse confirmation.
- Ongoing vital sign observation log entry.

---

### 1.5 Hospital Bed Management (`BedManagement.fxml`)
- Interactive ward overview: ICU, Trauma, Emergency, General.
- Real-time status cards: `AVAILABLE` (Green), `OCCUPIED` (Red), `MAINTENANCE` (Yellow).
- One-click patient bed transfer and discharge clearing.

---

### 1.6 System Administration & Audit Logs (`AdminDashboard.fxml` & `ActivityLogs.fxml`)
- User creation and RBAC permission toggling.
- High-level ED throughput statistics and wait-time analytics.
- Tamper-evident activity logs capturing every clinical and administrative event.

---

## 2. Capturing & Uploading Live Screenshots

To add real application screenshots to this directory:
1. Run the application: `mvn javafx:run`
2. Take screenshots of key screens using Windows Snipping Tool (`Win + Shift + S`).
3. Save images in PNG format:
   - `login-screen.png`
   - `live-queue.png`
   - `triage-vitals.png`
   - `doctor-workspace.png`
   - `bed-management.png`
   - `admin-analytics.png`
4. Place files in this `docs/screenshots/` folder and commit.
