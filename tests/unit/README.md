# Unit Testing Suite

The unit test suite verifies isolated algorithmic logic, business rules, triage scoring, and data validation rules without relying on external network dependencies.

## Key Test Classes

### 1. `PriorityQueueManagerTest`
- **Location**: `src/test/java/com/hospital/PriorityQueueManagerTest.java`
- **Tests**:
  1. Priority level sorting (RED 1 before ORANGE 2 before YELLOW 3 before GREEN 4).
  2. FIFO tie-breaking for equal priority levels (earlier arrival time served first).
  3. Dynamic queue re-ordering upon condition degradation.
  4. Wait-time aging escalation (patients waiting over threshold bumped to higher priority).
  5. Queue removal and state consistency.
  6. Empty queue and null handling resilience.

### 2. `PatientValidationTest`
- **Location**: `src/test/java/com/hospital/PatientValidationTest.java`
- **Tests**:
  1. Name validation (empty, null, numeric characters rejected).
  2. Age boundaries (negative, 0, >130 rejected).
  3. Phone format sanitization.
  4. Heart rate, blood pressure, SpO2 clinical range checks.
  5. Emergency level mapping.
  6. Duplicate entry detection.

### 3. `StatusWorkflowTest`
- **Location**: `src/test/java/com/hospital/StatusWorkflowTest.java`
- **Tests**:
  1. Permitted lifecycle transitions (`WAITING` -> `CALLED` -> `IN_TREATMENT` -> `TREATED` -> `DISCHARGED`).
  2. Illegal status jump rejection (e.g. `WAITING` directly to `DISCHARGED` without treatment).

### 4. `AuthenticationServiceTest`
- **Location**: `src/test/java/com/hospital/AuthenticationServiceTest.java`
- **Tests**:
  1. Valid login with BCrypt password verification.
  2. Rejection of invalid username or incorrect password.
  3. Session token creation and user context binding.
