# Testing Guide & Quality Gates

This guide outlines the testing philosophy, automated test execution, and quality benchmarks enforced for the **Hospital Emergency Patient Priority Treatment Management System**.

---

## 1. Testing Frameworks & Tooling

- **Core Engine**: JUnit 5 (Jupiter API & Engine).
- **Mocking & Spies**: Mockito 5.10.
- **Reporting**: Maven Surefire Plugin 3.2.5.
- **Database Simulation**: Embedded in-memory H2 with MySQL syntax emulation mode.

---

## 2. Test Execution Commands

```cmd
# Execute all 38 tests with Surefire summary
mvn clean test

# Execute single test class
mvn test -Dtest=EndToEndHospitalWorkflowTest

# Run tests with detailed failure stack traces
mvn test -e -X
```

---

## 3. Continuous Integration & Quality Gates

On every GitHub Pull Request and commit to `main`, GitHub Actions CI executes:
1. Compilation check (`mvn compile`).
2. Execution of all 38 unit, integration, and security tests.
3. Verification of zero test failures, zero errors, and zero unhandled exceptions.
4. Packaging check (`mvn package -DskipTests=false`).
