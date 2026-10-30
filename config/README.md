# Configuration Guide

This directory holds configuration templates and documentation for configuring the **Hospital Emergency Patient Priority Treatment Management System**.

## Configuration Templates

| File | Description |
| :--- | :--- |
| `config.properties.example` | Standalone Java / JavaFX property configuration template for database and triage thresholds. |
| `application.properties.example` | Standard enterprise property specification detailing connection pools, driver classes, and environment profiles. |

---

## Configuration Resolution Order

The application's `ConfigReader` dynamically resolves configuration parameters in the following priority order:

1. **External Folder**: `config/config.properties` (Highest precedence — convenient for staging/production deployments).
2. **Current Working Directory**: `config.properties` in the root folder.
3. **Classpath Resource**: `/config.properties` inside the packaged JAR / `src/main/resources`.
4. **Resilient In-Memory Fallback**: If no database connection can be established, the system automatically spins up an embedded in-memory H2 database in MySQL compatibility mode with full sample data.

---

## Environment Variables Support

For cloud, containerized, and CI/CD pipelines, values can also be provided through `.env` or system environment variables:

| Parameter | Environment Variable | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `db.host` | `DB_HOST` | `localhost` | MySQL database hostname |
| `db.port` | `DB_PORT` | `3306` | MySQL port |
| `db.name` | `DB_NAME` | `hospital_emergency_system` | Target database name |
| `db.username` | `DB_USER` | `root` | Database username |
| `db.password` | `DB_PASS` | *(empty / prompt)* | Database password |
| `app.emergency.critical.wait_threshold_min` | `CRITICAL_WAIT_MIN` | `15` | Escalation alert limit for Priority 1 |

---

## Quick Setup

To configure local credentials:

```cmd
copy config\config.properties.example config\config.properties
```

Edit `config\config.properties` with your MySQL password and save.
