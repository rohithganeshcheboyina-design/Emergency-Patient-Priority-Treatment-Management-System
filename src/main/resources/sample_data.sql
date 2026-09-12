-- ========================================================
-- HOSPITAL EMERGENCY PATIENT PRIORITY MANAGEMENT SYSTEM
-- Sample Realistic Demo Data
-- ========================================================

USE hospital_emergency_system;

-- Clear previous sample data in order of foreign key dependencies
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE activity_logs;
TRUNCATE TABLE patient_status_history;
TRUNCATE TABLE treatments;
TRUNCATE TABLE emergency_assessments;
TRUNCATE TABLE patients;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------------------
-- 1. Insert Demo Users
-- Passwords:
-- admin / admin123
-- doctor / doctor123
-- doctor2 / doctor123
-- nurse / nurse123
-- nurse2 / nurse123
-- --------------------------------------------------------
INSERT INTO users (user_id, username, password_hash, full_name, role, phone, email, active, created_at) VALUES
(1, 'admin', '$2a$10$bSiFCM3YO/T2wKx0.Hj.9u2AyZsrwIZQOcVVXSaiReRbVqSUZ9NMy', 'Hospital Administrator', 'ADMIN', '555-0100', 'admin@hospital.org', 1, NOW()),
(2, 'doctor', '$2a$10$Vt1MxUEF9UvFYBsH90Up9ubqFods0ADArDrNHRLgokeYmmpCxfYcK', 'Dr. Robert Vance, MD', 'DOCTOR', '555-0101', 'dr.vance@hospital.org', 1, NOW()),
(3, 'doctor2', '$2a$10$Vt1MxUEF9UvFYBsH90Up9ubqFods0ADArDrNHRLgokeYmmpCxfYcK', 'Dr. Sarah Jenkins, MD', 'DOCTOR', '555-0102', 'dr.jenkins@hospital.org', 1, NOW()),
(4, 'nurse', '$2a$10$xYvox9A4yphN40fDGg0PROAiLd2N0xOJw.BfC1vt1Xbr.Ee/MBYEq', 'Nurse Clara Barton, RN', 'NURSE', '555-0103', 'nurse.clara@hospital.org', 1, NOW()),
(5, 'nurse2', '$2a$10$xYvox9A4yphN40fDGg0PROAiLd2N0xOJw.BfC1vt1Xbr.Ee/MBYEq', 'Nurse David Miller, RN', 'NURSE', '555-0104', 'nurse.david@hospital.org', 1, NOW());

-- --------------------------------------------------------
-- 2. Insert Realistic Emergency Patients
-- Covering all 4 Priority Levels:
-- 1 = CRITICAL, 2 = HIGH, 3 = MEDIUM, 4 = LOW
-- And different workflow states:
-- WAITING, CALLED, IN_TREATMENT, TREATED, DISCHARGED
-- --------------------------------------------------------
INSERT INTO patients (patient_id, patient_code, token_number, full_name, age, gender, phone, address, blood_group, symptoms, emergency_level, priority, arrival_time, assigned_doctor_id, current_status, created_at) VALUES
(1, 'P0001', 'ER-001', 'Rahul Sharma', 54, 'Male', '555-0201', '42 MG Road, Metro District', 'O+', 'Sudden crushing chest pain radiating to left arm, acute diaphoresis and dyspnea', 'CRITICAL', 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE), 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(2, 'P0002', 'ER-002', 'Anjali Gupta', 29, 'Female', '555-0202', '15 Lake View Colony', 'B+', 'High velocity motorcycle collision, suspected flail chest, unconscious with hypotensive shock', 'CRITICAL', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(3, 'P0003', 'ER-003', 'Vikram Malhotra', 45, 'Male', '555-0203', '88 Park Street Avenue', 'A+', 'Open compound tibial fracture with active hemorrhage, severe pain score 9/10', 'HIGH', 2, DATE_SUB(NOW(), INTERVAL 40 MINUTE), 3, 'WAITING', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(4, 'P0004', 'ER-004', 'Kiran Patel', 38, 'Female', '555-0204', '104 Hillside Greens', 'AB+', 'Acute severe asthma exacerbation, refractory wheezing, pulse oximetry 88% on room air', 'HIGH', 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(5, 'P0005', 'ER-005', 'Suresh Nair', 62, 'Male', '555-0205', '77 Riverbank Road', 'O-', 'Deep laceration on right forearm from industrial machinery, uncontrolled venous bleeding', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 55 MINUTE), 3, 'WAITING', DATE_SUB(NOW(), INTERVAL 55 MINUTE)),
(6, 'P0006', 'ER-006', 'Priya Deshmukh', 23, 'Female', '555-0206', '12 University Enclave', 'A-', 'Acute migratory right lower quadrant abdominal pain, positive McBurney point tenderness, nausea', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 50 MINUTE), NULL, 'WAITING', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(7, 'P0007', 'ER-007', 'Amitabh Sen', 31, 'Male', '555-0207', '5 Riverside Gardens', 'B-', 'Inversion injury to left lateral ankle with edema and ecchymosis, able to bear partial weight', 'LOW', 4, DATE_SUB(NOW(), INTERVAL 70 MINUTE), NULL, 'WAITING', DATE_SUB(NOW(), INTERVAL 70 MINUTE)),
(8, 'P0008', 'ER-008', 'Sunita Reddy', 48, 'Female', '555-0209', '34 Rosewood Boulevard', 'O+', 'Superficial kitchen burn on dorsal hand with mild blistering, intact sensation', 'LOW', 4, DATE_SUB(NOW(), INTERVAL 65 MINUTE), NULL, 'WAITING', DATE_SUB(NOW(), INTERVAL 65 MINUTE)),
(9, 'P0009', 'ER-009', 'Rohan Mehra', 19, 'Male', '555-0210', '9 North Campus Road', 'A+', 'Acute generalized urticaria with mild pruritus following shellfish ingestion, no airway compromise', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 2, 'CALLED', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(10, 'P0010', 'ER-010', 'Meera Joshi', 52, 'Female', '555-0211', '66 Central Ridge', 'O+', 'Severe recurrent epistaxis not responding to direct compression, hypertensive reading 185/105', 'HIGH', 2, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 2, 'IN_TREATMENT', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(11, 'P0011', 'ER-011', 'Devendra Singh', 67, 'Male', '555-0212', '18 Station Road', 'B+', 'Acute febrile episode with productive cough, bibasilar crackles, confirmed bacterial pneumonia', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 120 MINUTE), 3, 'TREATED', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(12, 'P0012', 'ER-012', 'Pooja Verma', 27, 'Female', '555-0213', '21 Palm Meadows', 'A+', 'Mild tension headache, photophobia ruled out, stable vitals, responded to analgesics', 'LOW', 4, DATE_SUB(NOW(), INTERVAL 180 MINUTE), 3, 'DISCHARGED', DATE_SUB(NOW(), INTERVAL 180 MINUTE));

-- --------------------------------------------------------
-- 3. Insert Triage Assessments
-- --------------------------------------------------------
INSERT INTO emergency_assessments (assessment_id, patient_id, emergency_level, priority, symptoms, assessed_by, assessed_at, notes) VALUES
(1, 1, 'CRITICAL', 1, 'Crushing substernal chest pain, diaphoresis, impending acute myocardial infarction', 4, DATE_SUB(NOW(), INTERVAL 24 MINUTE), 'ECG ordered immediately. Stat cardiology alert triggered.'),
(2, 2, 'CRITICAL', 1, 'Severe polytrauma, head injury, flail chest, hemorrhagic shock', 4, DATE_SUB(NOW(), INTERVAL 14 MINUTE), 'Trauma team activated. Intubation tray prepared.'),
(3, 3, 'HIGH', 2, 'Compound fracture tibia, open wound bleeding', 5, DATE_SUB(NOW(), INTERVAL 39 MINUTE), 'Direct pressure applied, IV access secured, ortho on call.'),
(4, 4, 'HIGH', 2, 'Status asthmaticus, wheezing, SpO2 88%', 4, DATE_SUB(NOW(), INTERVAL 29 MINUTE), 'Supplemental O2 4L via nasal cannula started.'),
(5, 5, 'MEDIUM', 3, 'Deep forearm laceration, venous bleeding', 5, DATE_SUB(NOW(), INTERVAL 54 MINUTE), 'Pressure dressing placed, tetanus prophylaxis evaluated.'),
(6, 6, 'MEDIUM', 3, 'RLQ abdominal pain, suspect appendicitis', 4, DATE_SUB(NOW(), INTERVAL 49 MINUTE), 'Abdominal ultrasound advised, NPO maintained.'),
(7, 7, 'LOW', 4, 'Lateral ankle sprain, swelling', 5, DATE_SUB(NOW(), INTERVAL 69 MINUTE), 'RICE protocol initiated. X-ray pending.'),
(8, 8, 'LOW', 4, 'Partial thickness burn dorsal hand <1%', 4, DATE_SUB(NOW(), INTERVAL 64 MINUTE), 'Cooled with saline, sterile non-adherent dressing.'),
(9, 10, 'HIGH', 2, 'Epistaxis with severe hypertension', 4, DATE_SUB(NOW(), INTERVAL 89 MINUTE), 'Anterior nasal pack placed, antihypertensive initiated.');

-- --------------------------------------------------------
-- 4. Insert Active/Completed Treatments
-- --------------------------------------------------------
INSERT INTO treatments (treatment_id, patient_id, doctor_id, start_time, end_time, treatment_status, notes) VALUES
(1, 10, 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 'IN_PROGRESS', 'Anterior nasal pack inserted with lidocaine spray. BP monitored every 10 min.'),
(2, 11, 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'COMPLETED', 'IV Ceftriaxone 1g administered with nebulized bronchodilators. Vitals stabilized.'),
(3, 12, 3, DATE_SUB(NOW(), INTERVAL 150 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'COMPLETED', 'Oral paracetamol 1000mg with hydration. Symptoms resolved completely. Discharged home.');

-- --------------------------------------------------------
-- 5. Insert Patient Status History (Audit Timeline)
-- --------------------------------------------------------
INSERT INTO patient_status_history (patient_id, old_status, new_status, changed_by, changed_at, remarks) VALUES
(1, NULL, 'WAITING', 4, DATE_SUB(NOW(), INTERVAL 25 MINUTE), 'Patient registered at triage counter'),
(2, NULL, 'WAITING', 4, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'Patient arrived via ambulance intake'),
(9, 'WAITING', 'CALLED', 2, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'Doctor called patient to Examination Bay 1'),
(10, 'WAITING', 'CALLED', 2, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 'Doctor called patient to Treatment Room 2'),
(10, 'CALLED', 'IN_TREATMENT', 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'Treatment started by Dr. Robert Vance'),
(11, 'WAITING', 'IN_TREATMENT', 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'Treatment initiated in Observation Ward'),
(11, 'IN_TREATMENT', 'TREATED', 3, DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'Medication course completed successfully'),
(12, 'WAITING', 'IN_TREATMENT', 3, DATE_SUB(NOW(), INTERVAL 150 MINUTE), 'Evaluation commenced'),
(12, 'IN_TREATMENT', 'TREATED', 3, DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'Observation complete with negative red flags'),
(12, 'TREATED', 'DISCHARGED', 3, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'Discharge summary handed to patient with oral prescriptions');

-- --------------------------------------------------------
-- 6. Insert System Activity Logs
-- --------------------------------------------------------
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, timestamp, description) VALUES
(1, 'LOGIN', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 240 MINUTE), 'Admin logged in successfully'),
(4, 'LOGIN', 'SYSTEM', 4, DATE_SUB(NOW(), INTERVAL 180 MINUTE), 'Nurse Clara Barton logged in for Morning Shift'),
(2, 'LOGIN', 'SYSTEM', 2, DATE_SUB(NOW(), INTERVAL 120 MINUTE), 'Dr. Robert Vance logged in to ER Bay'),
(4, 'PATIENT_REGISTERED', 'PATIENT', 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE), 'Registered Rahul Sharma (P0001) - Token ER-001 with CRITICAL emergency level'),
(4, 'PRIORITY_ASSIGNED', 'PATIENT', 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE), 'Assigned Priority 1 (CRITICAL) to patient P0001'),
(4, 'PATIENT_REGISTERED', 'PATIENT', 2, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'Registered Anjali Gupta (P0002) - Token ER-002 with CRITICAL emergency level'),
(2, 'TREATMENT_STARTED', 'TREATMENT', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'Started epistaxis treatment for Meera Joshi (P0010)'),
(3, 'TREATMENT_COMPLETED', 'TREATMENT', 2, DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'Completed pneumonia stabilization for Devendra Singh (P0011)'),
(3, 'PATIENT_DISCHARGED', 'PATIENT', 12, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'Discharged Pooja Verma (P0012) in stable condition');
