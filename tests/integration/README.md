# Integration Testing Suite

The integration test suite validates multi-component interactions between Services, DAOs, the Database layer, and Role-Based Access Control (RBAC).

## Key Test Classes

### 1. `RoleAuthorizationTest`
- **Location**: `src/test/java/com/hospital/RoleAuthorizationTest.java`
- **Tests**:
  1. `ADMIN` role access to User Management, Settings, Activity Logs, and System Config.
  2. `DOCTOR` role access to Patient Detail, Triage Overrides, Treatment Recording, and Bed Orders.
  3. `NURSE` role access to Vitals Entry, Observations, and Queue Status.
  4. `PATIENT` role restricted to personal records and appointment booking only.

### 2. `BedManagementTest`
- **Location**: `src/test/java/com/hospital/BedManagementTest.java`
- **Tests**:
  1. Bed allocation to incoming critical patient (`AVAILABLE` -> `OCCUPIED`).
  2. Bed release upon patient discharge (`OCCUPIED` -> `MAINTENANCE` / `AVAILABLE`).

### 3. `AppointmentServiceTest`
- **Location**: `src/test/java/com/hospital/AppointmentServiceTest.java`
- **Tests**:
  1. Appointment slot creation and doctor schedule association.
  2. Appointment status updates (`SCHEDULED` -> `COMPLETED` / `CANCELLED`).

### 4. `TreatmentServiceTest`
- **Location**: `src/test/java/com/hospital/TreatmentServiceTest.java`
- **Tests**:
  1. Recording diagnosis, prescription, dosage, and physician notes.
  2. Integration with patient history and activity log event triggers.
