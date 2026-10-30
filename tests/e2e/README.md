# End-to-End & Security Testing Suite

The E2E test suite simulates full real-world hospital workflows from patient registration through clinical treatment to discharge, alongside automated security penetration testing.

## Key Test Classes

### 1. `EndToEndHospitalWorkflowTest`
- **Location**: `src/test/java/com/hospital/EndToEndHospitalWorkflowTest.java`
- **Simulated Workflow**:
  1. **Registration**: Patient arrives at ED and is registered with baseline symptoms.
  2. **Triage Assessment**: Nurse records heart rate, blood pressure, SpO2, and pain score; system calculates Priority 1 (RED).
  3. **Queue Sorting**: Patient is placed at the head of the emergency queue.
  4. **Doctor Assignment**: Attending physician calls patient into treatment room.
  5. **Bed Allocation**: Critical ICU/Emergency bed is reserved and occupied.
  6. **Treatment & Diagnosis**: Doctor records diagnosis and prescribes acute medications.
  7. **Vitals Monitoring**: Nurse records subsequent observations.
  8. **Discharge & Audit**: Patient is discharged; bed is released; audit log is verified.

### 2. `SecurityAndInjectionTest`
- **Location**: `src/test/java/com/hospital/SecurityAndInjectionTest.java`
- **Penetration Attack Tests**:
  1. SQL Injection on username login: `' OR '1'='1`
  2. SQL Injection on password field: `' OR 1=1 --`
  3. SQL Injection on search filter: `1; DROP TABLE users;--`
  4. Cross-Site Scripting (XSS) payload sanitization in patient notes: `<script>alert('xss')</script>`
  5. Unauthorized role escalation attempts.
  6. Session hijacking / expired token rejection.
  7. Null byte & buffer boundary test strings.
