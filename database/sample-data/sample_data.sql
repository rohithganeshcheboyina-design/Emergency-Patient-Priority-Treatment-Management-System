-- ========================================================
-- HOSPITAL EMERGENCY PATIENT PRIORITY MANAGEMENT SYSTEM
-- Sample Realistic Demo Data
-- ========================================================

USE hospital_emergency_system;

-- Clear previous sample data in order of foreign key dependencies
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE activity_logs;
TRUNCATE TABLE appointments;
TRUNCATE TABLE beds;
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
-- patient / patient123
-- --------------------------------------------------------
INSERT INTO users (user_id, username, password_hash, full_name, role, phone, email, active, created_at) VALUES
(1, 'admin', '$2a$10$bSiFCM3YO/T2wKx0.Hj.9u2AyZsrwIZQOcVVXSaiReRbVqSUZ9NMy', 'Hospital Administrator', 'ADMIN', '555-0100', 'admin@hospital.org', 1, NOW()),
(2, 'doctor', '$2a$10$Vt1MxUEF9UvFYBsH90Up9ubqFods0ADArDrNHRLgokeYmmpCxfYcK', 'Dr. Robert Vance, MD', 'DOCTOR', '555-0101', 'dr.vance@hospital.org', 1, NOW()),
(3, 'doctor2', '$2a$10$Vt1MxUEF9UvFYBsH90Up9ubqFods0ADArDrNHRLgokeYmmpCxfYcK', 'Dr. Sarah Jenkins, MD', 'DOCTOR', '555-0102', 'dr.jenkins@hospital.org', 1, NOW()),
(4, 'nurse', '$2a$10$xYvox9A4yphN40fDGg0PROAiLd2N0xOJw.BfC1vt1Xbr.Ee/MBYEq', 'Nurse Clara Barton, RN', 'NURSE', '555-0103', 'nurse.clara@hospital.org', 1, NOW()),
(5, 'nurse2', '$2a$10$xYvox9A4yphN40fDGg0PROAiLd2N0xOJw.BfC1vt1Xbr.Ee/MBYEq', 'Nurse David Miller, RN', 'NURSE', '555-0104', 'nurse.david@hospital.org', 1, NOW()),
(6, 'patient', '$2a$10$Vt1MxUEF9UvFYBsH90Up9ubqFods0ADArDrNHRLgokeYmmpCxfYcK', 'Rahul Sharma', 'PATIENT', '555-0201', 'rahul.sharma@example.com', 1, NOW());

-- --------------------------------------------------------
-- 2. Insert Realistic Emergency Patients
-- Priority Levels: 1 = CRITICAL, 2 = HIGH, 3 = MEDIUM, 4 = LOW
-- Statuses: WAITING, CALLED, IN_TREATMENT, TREATED, DISCHARGED
-- --------------------------------------------------------
INSERT INTO patients (patient_id, patient_code, token_number, full_name, age, gender, phone, address, blood_group, allergies, emergency_contact, vital_bp, vital_heart_rate, vital_temp, vital_spo2, vital_respiratory_rate, symptoms, emergency_level, priority, arrival_time, assigned_doctor_id, current_status, created_at) VALUES
(1, 'P0001', 'ER-001', 'Rahul Sharma', 54, 'Male', '555-0201', '42 MG Road, Metro District', 'O+', 'Penicillin', 'Pooja Sharma (Wife) - 555-0299', '165/100', 110, 98.6, 92, 24, 'Sudden crushing chest pain radiating to left arm, acute diaphoresis and dyspnea', 'CRITICAL', 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE), 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(2, 'P0002', 'ER-002', 'Anjali Gupta', 29, 'Female', '555-0202', '15 Lake View Colony', 'B+', 'None known', 'Sunil Gupta (Father) - 555-0298', '85/55', 135, 97.8, 88, 30, 'High velocity motorcycle collision, suspected flail chest, unconscious with hypotensive shock', 'CRITICAL', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(3, 'P0003', 'ER-003', 'Vikram Malhotra', 45, 'Male', '555-0203', '88 Park Street Avenue', 'A+', 'NSAIDs (Aspirin)', 'Anita Malhotra - 555-0297', '140/90', 102, 99.1, 96, 20, 'Open compound tibial fracture with active hemorrhage, severe pain score 9/10', 'HIGH', 2, DATE_SUB(NOW(), INTERVAL 40 MINUTE), 3, 'WAITING', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(4, 'P0004', 'ER-004', 'Kiran Patel', 38, 'Female', '555-0204', '104 Hillside Greens', 'AB+', 'Sulfa drugs', 'Ramesh Patel - 555-0296', '130/85', 115, 98.4, 88, 28, 'Acute severe asthma exacerbation, refractory wheezing, pulse oximetry 88% on room air', 'HIGH', 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(5, 'P0005', 'ER-005', 'Suresh Nair', 62, 'Male', '555-0205', '77 Riverbank Road', 'O-', 'None', 'Lakshmi Nair - 555-0295', '125/80', 88, 98.2, 97, 18, 'Deep laceration on right forearm from industrial machinery, uncontrolled venous bleeding', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 55 MINUTE), 3, 'WAITING', DATE_SUB(NOW(), INTERVAL 55 MINUTE)),
(6, 'P0006', 'ER-006', 'Priya Deshmukh', 23, 'Female', '555-0206', '12 University Enclave', 'A-', 'Latex', 'Dr. K. Deshmukh - 555-0294', '118/75', 94, 100.8, 98, 18, 'Acute migratory right lower quadrant abdominal pain, positive McBurney point tenderness, nausea', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 50 MINUTE), NULL, 'WAITING', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(7, 'P0007', 'ER-007', 'Amitabh Sen', 31, 'Male', '555-0207', '5 Riverside Gardens', 'B-', 'None', 'Rina Sen - 555-0293', '120/78', 76, 98.6, 99, 16, 'Inversion injury to left lateral ankle with edema and ecchymosis, able to bear partial weight', 'LOW', 4, DATE_SUB(NOW(), INTERVAL 70 MINUTE), NULL, 'WAITING', DATE_SUB(NOW(), INTERVAL 70 MINUTE)),
(8, 'P0008', 'ER-008', 'Sunita Reddy', 48, 'Female', '555-0209', '34 Rosewood Boulevard', 'O+', 'Iodine', 'M. Reddy - 555-0292', '128/82', 80, 98.7, 98, 16, 'Superficial kitchen burn on dorsal hand with mild blistering, intact sensation', 'LOW', 4, DATE_SUB(NOW(), INTERVAL 65 MINUTE), NULL, 'WAITING', DATE_SUB(NOW(), INTERVAL 65 MINUTE)),
(9, 'P0009', 'ER-009', 'Rohan Mehra', 19, 'Male', '555-0210', '9 North Campus Road', 'A+', 'Shellfish', 'Ajay Mehra - 555-0291', '122/80', 85, 98.6, 98, 18, 'Acute generalized urticaria with mild pruritus following shellfish ingestion, no airway compromise', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 2, 'CALLED', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(10, 'P0010', 'ER-010', 'Meera Joshi', 52, 'Female', '555-0211', '66 Central Ridge', 'O+', 'None', 'S. Joshi - 555-0290', '185/105', 105, 98.8, 97, 20, 'Severe recurrent epistaxis not responding to direct compression, hypertensive reading 185/105', 'HIGH', 2, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 2, 'IN_TREATMENT', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
(11, 'P0011', 'ER-011', 'Devendra Singh', 67, 'Male', '555-0212', '18 Station Road', 'B+', 'Penicillin', 'Gurpreet Singh - 555-0289', '135/85', 96, 102.2, 94, 22, 'Acute febrile episode with productive cough, bibasilar crackles, confirmed bacterial pneumonia', 'MEDIUM', 3, DATE_SUB(NOW(), INTERVAL 120 MINUTE), 3, 'TREATED', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(12, 'P0012', 'ER-012', 'Pooja Verma', 27, 'Female', '555-0213', '21 Palm Meadows', 'A+', 'None', 'Amit Verma - 555-0288', '115/70', 72, 98.4, 99, 14, 'Mild tension headache, photophobia ruled out, stable vitals, responded to analgesics', 'LOW', 4, DATE_SUB(NOW(), INTERVAL 180 MINUTE), 3, 'DISCHARGED', DATE_SUB(NOW(), INTERVAL 180 MINUTE));

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
INSERT INTO treatments (treatment_id, patient_id, doctor_id, start_time, end_time, treatment_status, diagnosis, medications, notes) VALUES
(1, 10, 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 'IN_PROGRESS', 'Hypertensive Emergency with Epistaxis', 'Amlodipine 5mg, Labetalol 20mg IV, Oxymetazoline nasal spray', 'Anterior nasal pack inserted with lidocaine spray. BP monitored every 10 min.'),
(2, 11, 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'COMPLETED', 'Community-Acquired Bacterial Pneumonia', 'IV Ceftriaxone 1g, Azithromycin 500mg, Albuterol nebulization', 'IV antibiotic administered with nebulized bronchodilators. Vitals stabilized.'),
(3, 12, 3, DATE_SUB(NOW(), INTERVAL 150 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'COMPLETED', 'Tension-Type Cephalea', 'Oral Paracetamol 1000mg, Oral hydration 500ml', 'Oral analgesics given with rest. Symptoms resolved completely. Discharged home.');

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
-- 6. Insert Hospital Bed Inventory & Status
-- --------------------------------------------------------
INSERT INTO beds (bed_id, bed_number, bed_type, status, assigned_patient_id, assigned_at, notes) VALUES
(1, 'ER-BAY-01', 'EMERGENCY', 'OCCUPIED', 10, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'Occupied for epistaxis treatment'),
(2, 'ER-BAY-02', 'EMERGENCY', 'AVAILABLE', NULL, NULL, 'Cleaned and sanitized, oxygen ports tested'),
(3, 'ER-BAY-03', 'EMERGENCY', 'AVAILABLE', NULL, NULL, 'Equipped with bedside vitals monitor'),
(4, 'ICU-BED-01', 'ICU', 'OCCUPIED', 2, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'Mechanical ventilator connected'),
(5, 'ICU-BED-02', 'ICU', 'AVAILABLE', NULL, NULL, 'Ready for critical admissions'),
(6, 'TR-RESUS-01', 'TRAUMA_BAY', 'AVAILABLE', NULL, NULL, 'Direct trauma suite with full crash cart'),
(7, 'TR-RESUS-02', 'TRAUMA_BAY', 'AVAILABLE', NULL, NULL, 'Sterile surgical lighting ready'),
(8, 'OBS-UNIT-01', 'OBSERVATION', 'OCCUPIED', 11, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'Post-pneumonia stabilization'),
(9, 'OBS-UNIT-02', 'OBSERVATION', 'AVAILABLE', NULL, NULL, '6-hour short stay unit bed'),
(10, 'GEN-WARD-01', 'GENERAL_WARD', 'AVAILABLE', NULL, NULL, 'Step-down recovery bed'),
(11, 'GEN-WARD-02', 'GENERAL_WARD', 'AVAILABLE', NULL, NULL, 'Step-down recovery bed'),
(12, 'GEN-WARD-03', 'GENERAL_WARD', 'AVAILABLE', NULL, NULL, 'Step-down recovery bed');

-- --------------------------------------------------------
-- 7. Insert Scheduled Appointments
-- --------------------------------------------------------
INSERT INTO appointments (appointment_id, patient_id, doctor_id, appointment_time, specialty, status, reason, notes, created_at) VALUES
(1, 1, 2, DATE_ADD(NOW(), INTERVAL 2 DAY), 'Cardiology Follow-up', 'SCHEDULED', 'Post-acute coronary evaluation and stress test review', 'Re-check troponin-I and ECG before consultation', NOW()),
(2, 3, 3, DATE_ADD(NOW(), INTERVAL 5 DAY), 'Orthopedic Surgery', 'SCHEDULED', 'Tibia fracture post-reduction cast check', 'Weight bearing evaluation', NOW()),
(3, 11, 3, DATE_ADD(NOW(), INTERVAL 7 DAY), 'Pulmonology', 'SCHEDULED', 'Pneumonia resolution chest X-ray follow-up', 'Repeat spirometry and CBC', NOW());

-- --------------------------------------------------------
-- 8. Insert System Activity Logs
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
