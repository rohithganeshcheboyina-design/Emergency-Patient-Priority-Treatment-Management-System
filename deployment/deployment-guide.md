# Deployment & Operations Guide

This guide covers deployment, containerization, local execution, and production database provisioning for the **Hospital Emergency Patient Priority Treatment Management System**.

---

## 1. Prerequisites

- **Java Development Kit (JDK)**: OpenJDK 21 or Eclipse Temurin 21.
- **Apache Maven**: 3.9+ (or use bundled Maven wrapper).
- **Relational Database**: MySQL 8.0+ (Optional: Embedded H2 fallback automatically activates if MySQL is unavailable).
- **Container Engine** (Optional): Docker Engine 24+ & Docker Compose v2+.

---

## 2. Windows Local Deployment (CMD / PowerShell)

### Step 1: Clone Repository
```cmd
git clone https://github.com/rohithganeshcheboyina-design/Emergency-Patient-Priority-Treatment-Management-System.git
cd Emergency-Patient-Priority-Treatment-Management-System
```

### Step 2: Database Setup (MySQL)
Ensure your MySQL service is active, then execute:

```cmd
mysql -u root -p < database\schema\schema.sql
mysql -u root -p < database\sample-data\sample_data.sql
```

*(Note: If MySQL is not running, the application will automatically boot an in-memory database with the full schema and seed data.)*

### Step 3: Configure Credentials
Copy configuration template:
```cmd
copy config\config.properties.example config\config.properties
```
Edit `config\config.properties` if your MySQL password differs from empty/root.

### Step 4: Build and Launch
```cmd
mvn clean package -DskipTests
mvn javafx:run
```

---

## 3. Docker Deployment (Database & phpMyAdmin)

To quickly spin up a isolated MySQL 8 container with automatic schema & seed data loading:

```cmd
cd deployment\docker
docker compose up -d
```

- **MySQL Port**: `localhost:3306` (Root password: `root`)
- **phpMyAdmin Web UI**: `http://localhost:8080` (Server: `mysql`, User: `root`, Password: `root`)

After the database container starts healthy, run the application from the project root:
```cmd
mvn javafx:run
```

---

## 4. Standalone Executable Fat JAR Deployment

To build a standalone redistributable JAR file:

```cmd
mvn clean package
```

Run the generated JAR directly with Java 21:
```cmd
java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml -jar target\hospital-emergency-system-1.0.0.jar
```

Or execute via Maven Launcher:
```cmd
java -cp target\hospital-emergency-system-1.0.0.jar com.hospital.MainLauncher
```

---

## 5. Continuous Integration (CI)

The repository includes a GitHub Actions workflow in `.github/workflows/ci.yml`. On every pull request and push to `main`:
1. Checks out code on Ubuntu.
2. Sets up Eclipse Temurin JDK 21.
3. Caches Maven local repository dependencies.
4. Runs `mvn clean test` across all 11 test suites (38 unit, integration, and security tests).
5. Compiles production artifacts and verifies build integrity.
