package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Patient management and priority queue persistence.
 */
public class PatientDAO {

    private static final String SELECT_BASE =
            "SELECT p.patient_id, p.patient_code, p.token_number, p.full_name, p.age, p.gender, " +
            "       p.phone, p.address, p.blood_group, p.symptoms, p.emergency_level, p.priority, " +
            "       p.arrival_time, p.assigned_doctor_id, u.full_name AS assigned_doctor_name, " +
            "       p.current_status, p.created_at, p.updated_at " +
            "FROM patients p " +
            "LEFT JOIN users u ON p.assigned_doctor_id = u.user_id ";

    public Patient findById(int patientId) {
        String sql = SELECT_BASE + "WHERE p.patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPatient(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding patient by ID: " + patientId, e);
        }
        return null;
    }

    public Patient findByCode(String patientCode) {
        String sql = SELECT_BASE + "WHERE p.patient_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPatient(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding patient by code: " + patientCode, e);
        }
        return null;
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY p.patient_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToPatient(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all patients", e);
        }
        return list;
    }

    /**
     * Retrieves active patients in the queue ordered by Emergency Priority ASC and Arrival Time ASC.
     */
    public List<Patient> getActiveQueuePatients() {
        List<Patient> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.current_status IN ('WAITING', 'CALLED') " +
                     "ORDER BY p.priority ASC, p.arrival_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToPatient(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving active queue patients", e);
        }
        return list;
    }

    public boolean createPatient(Patient patient) {
        if (patient.getPatientCode() == null || patient.getPatientCode().isEmpty()) {
            patient.setPatientCode(getNextPatientCode());
        }
        if (patient.getTokenNumber() == null || patient.getTokenNumber().isEmpty()) {
            patient.setTokenNumber(getNextTokenNumber());
        }
        if (patient.getArrivalTime() == null) {
            patient.setArrivalTime(LocalDateTime.now());
        }
        if (patient.getEmergencyLevel() != null) {
            patient.setPriority(patient.getEmergencyLevel().getPriority());
        }

        String sql = "INSERT INTO patients (patient_code, token_number, full_name, age, gender, phone, " +
                     "address, blood_group, symptoms, emergency_level, priority, arrival_time, " +
                     "assigned_doctor_id, current_status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getPatientCode());
            stmt.setString(2, patient.getTokenNumber());
            stmt.setString(3, patient.getFullName());
            stmt.setInt(4, patient.getAge());
            stmt.setString(5, patient.getGender());
            stmt.setString(6, patient.getPhone());
            stmt.setString(7, patient.getAddress());
            stmt.setString(8, patient.getBloodGroup());
            stmt.setString(9, patient.getSymptoms());
            stmt.setString(10, patient.getEmergencyLevel().name());
            stmt.setInt(11, patient.getPriority());
            stmt.setTimestamp(12, Timestamp.valueOf(patient.getArrivalTime()));

            if (patient.getAssignedDoctorId() != null && patient.getAssignedDoctorId() > 0) {
                stmt.setInt(13, patient.getAssignedDoctorId());
            } else {
                stmt.setNull(13, Types.INTEGER);
            }

            stmt.setString(14, patient.getCurrentStatus() != null ? patient.getCurrentStatus().name() : "WAITING");
            stmt.setTimestamp(15, Timestamp.valueOf(patient.getCreatedAt() != null ? patient.getCreatedAt() : LocalDateTime.now()));
            stmt.setTimestamp(16, Timestamp.valueOf(patient.getUpdatedAt() != null ? patient.getUpdatedAt() : LocalDateTime.now()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        patient.setPatientId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating patient: " + patient.getFullName(), e);
        }
        return false;
    }

    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET full_name = ?, age = ?, gender = ?, phone = ?, address = ?, " +
                     "blood_group = ?, symptoms = ?, emergency_level = ?, priority = ?, " +
                     "assigned_doctor_id = ?, current_status = ?, updated_at = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getFullName());
            stmt.setInt(2, patient.getAge());
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getBloodGroup());
            stmt.setString(7, patient.getSymptoms());
            stmt.setString(8, patient.getEmergencyLevel().name());
            stmt.setInt(9, patient.getEmergencyLevel().getPriority());

            if (patient.getAssignedDoctorId() != null && patient.getAssignedDoctorId() > 0) {
                stmt.setInt(10, patient.getAssignedDoctorId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }

            stmt.setString(11, patient.getCurrentStatus().name());
            stmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(13, patient.getPatientId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating patient ID: " + patient.getPatientId(), e);
        }
    }

    public boolean updateStatus(int patientId, PatientStatus newStatus, Integer doctorId) {
        StringBuilder sb = new StringBuilder("UPDATE patients SET current_status = ?, updated_at = ?");
        if (doctorId != null && doctorId > 0) {
            sb.append(", assigned_doctor_id = ?");
        }
        sb.append(" WHERE patient_id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            stmt.setString(1, newStatus.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            int idx = 3;
            if (doctorId != null && doctorId > 0) {
                stmt.setInt(idx++, doctorId);
            }
            stmt.setInt(idx, patientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating status for patient ID: " + patientId, e);
        }
    }

    public synchronized String getNextPatientCode() {
        String sql = "SELECT MAX(patient_id) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            return String.format("P%04d", nextId);
        } catch (SQLException e) {
            return "P" + System.currentTimeMillis() % 10000;
        }
    }

    public synchronized String getNextTokenNumber() {
        String sql = "SELECT MAX(patient_id) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            return String.format("ER-%03d", nextId);
        } catch (SQLException e) {
            return "ER-" + (int)(Math.random() * 900 + 100);
        }
    }

    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setTokenNumber(rs.getString("token_number"));
        p.setFullName(rs.getString("full_name"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setSymptoms(rs.getString("symptoms"));
        p.setEmergencyLevel(EmergencyLevel.fromString(rs.getString("emergency_level")));
        p.setPriority(rs.getInt("priority"));

        Timestamp arrTs = rs.getTimestamp("arrival_time");
        p.setArrivalTime(arrTs != null ? arrTs.toLocalDateTime() : LocalDateTime.now());

        int docId = rs.getInt("assigned_doctor_id");
        if (!rs.wasNull()) {
            p.setAssignedDoctorId(docId);
        }
        p.setAssignedDoctorName(rs.getString("assigned_doctor_name"));
        p.setCurrentStatus(PatientStatus.fromString(rs.getString("current_status")));

        Timestamp crTs = rs.getTimestamp("created_at");
        p.setCreatedAt(crTs != null ? crTs.toLocalDateTime() : LocalDateTime.now());
        Timestamp upTs = rs.getTimestamp("updated_at");
        p.setUpdatedAt(upTs != null ? upTs.toLocalDateTime() : LocalDateTime.now());

        return p;
    }
}
