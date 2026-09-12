-- ========================================================
-- HOSPITAL EMERGENCY PATIENT PRIORITY MANAGEMENT SYSTEM
-- Database Schema (MySQL 8.0+)
-- ========================================================

CREATE DATABASE IF NOT EXISTS hospital_emergency_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hospital_emergency_system;

-- --------------------------------------------------------
-- 1. Table: users
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'DOCTOR', 'NURSE') NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 2. Table: patients
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL UNIQUE,
    token_number VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    blood_group VARCHAR(10),
    symptoms TEXT NOT NULL,
    emergency_level VARCHAR(20) NOT NULL,
    priority INT NOT NULL,
    arrival_time DATETIME NOT NULL,
    assigned_doctor_id INT NULL,
    current_status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patients_doctor FOREIGN KEY (assigned_doctor_id) 
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_priority_arrival (priority, arrival_time),
    INDEX idx_current_status (current_status),
    INDEX idx_patient_code (patient_code),
    INDEX idx_token_number (token_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 3. Table: emergency_assessments
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS emergency_assessments (
    assessment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    emergency_level VARCHAR(20) NOT NULL,
    priority INT NOT NULL,
    symptoms TEXT NOT NULL,
    assessed_by INT NOT NULL,
    assessed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_assessments_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_assessments_user FOREIGN KEY (assessed_by) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_assessment_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 4. Table: treatments
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS treatments (
    treatment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    treatment_status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    notes TEXT,
    CONSTRAINT fk_treatments_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_treatments_doctor FOREIGN KEY (doctor_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_treatment_patient (patient_id),
    INDEX idx_treatment_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 5. Table: patient_status_history
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    old_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by INT NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks TEXT,
    CONSTRAINT fk_history_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_history_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 6. Table: activity_logs
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NULL,
    entity_id INT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_logs_timestamp (timestamp),
    INDEX idx_logs_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
