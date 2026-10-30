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
 * Data Access Object for Patient management, vitals monitoring, and priority queue persistence.
 */
public class PatientDAO {

    private static final String SELECT_BASE =
            "SELECT p.patient_id, p.patient_code, p.token_number, p.full_name, p.age, p.gender, " +
            "       p.phone, p.address, p.blood_group, p.allergies, p.emergency_contact, " +
            "       p.vital_bp, p.vital_heart_rate, p.vital_temp, p.vital_spo2, p.vital_respiratory_rate, " +
            "       p.symptoms, p.emergency_level, p.priority, " +
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
                     "address, blood_group, allergies, emergency_contact, vital_bp, vital_heart_rate, " +
                     "vital_temp, vital_spo2, vital_respiratory_rate, symptoms, emergency_level, priority, " +
                     "arrival_time, assigned_doctor_id, current_status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setString(9, patient.getAllergies());
            stmt.setString(10, patient.getEmergencyContact());
            stmt.setString(11, patient.getVitalBp());

            if (patient.getVitalHeartRate() != null) stmt.setInt(12, patient.getVitalHeartRate());
            else stmt.setNull(12, Types.INTEGER);

            if (patient.getVitalTemperature() != null) stmt.setDouble(13, patient.getVitalTemperature());
            else stmt.setNull(13, Types.DOUBLE);

            if (patient.getVitalSpo2() != null) stmt.setInt(14, patient.getVitalSpo2());
            else stmt.setNull(14, Types.INTEGER);

            if (patient.getVitalRespiratoryRate() != null) stmt.setInt(15, patient.getVitalRespiratoryRate());
            else stmt.setNull(15, Types.INTEGER);

            stmt.setString(16, patient.getSymptoms());
            stmt.setString(17, patient.getEmergencyLevel().name());
            stmt.setInt(18, patient.getPriority());
            stmt.setTimestamp(19, Timestamp.valueOf(patient.getArrivalTime()));

            if (patient.getAssignedDoctorId() != null && patient.getAssignedDoctorId() > 0) {
                stmt.setInt(20, patient.getAssignedDoctorId());
            } else {
                stmt.setNull(20, Types.INTEGER);
            }

            stmt.setString(21, patient.getCurrentStatus() != null ? patient.getCurrentStatus().name() : "WAITING");
            stmt.setTimestamp(22, Timestamp.valueOf(patient.getCreatedAt() != null ? patient.getCreatedAt() : LocalDateTime.now()));
            stmt.setTimestamp(23, Timestamp.valueOf(patient.getUpdatedAt() != null ? patient.getUpdatedAt() : LocalDateTime.now()));

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
                     "blood_group = ?, allergies = ?, emergency_contact = ?, vital_bp = ?, vital_heart_rate = ?, " +
                     "vital_temp = ?, vital_spo2 = ?, vital_respiratory_rate = ?, symptoms = ?, emergency_level = ?, " +
                     "priority = ?, assigned_doctor_id = ?, current_status = ?, updated_at = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getFullName());
            stmt.setInt(2, patient.getAge());
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getBloodGroup());
            stmt.setString(7, patient.getAllergies());
            stmt.setString(8, patient.getEmergencyContact());
            stmt.setString(9, patient.getVitalBp());

            if (patient.getVitalHeartRate() != null) stmt.setInt(10, patient.getVitalHeartRate());
            else stmt.setNull(10, Types.INTEGER);

            if (patient.getVitalTemperature() != null) stmt.setDouble(11, patient.getVitalTemperature());
            else stmt.setNull(11, Types.DOUBLE);

            if (patient.getVitalSpo2() != null) stmt.setInt(12, patient.getVitalSpo2());
            else stmt.setNull(12, Types.INTEGER);

            if (patient.getVitalRespiratoryRate() != null) stmt.setInt(13, patient.getVitalRespiratoryRate());
            else stmt.setNull(13, Types.INTEGER);

            stmt.setString(14, patient.getSymptoms());
            stmt.setString(15, patient.getEmergencyLevel().name());
            stmt.setInt(16, patient.getEmergencyLevel().getPriority());

            if (patient.getAssignedDoctorId() != null && patient.getAssignedDoctorId() > 0) {
                stmt.setInt(17, patient.getAssignedDoctorId());
            } else {
                stmt.setNull(17, Types.INTEGER);
            }

            stmt.setString(18, patient.getCurrentStatus().name());
            stmt.setTimestamp(19, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(20, patient.getPatientId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating patient ID: " + patient.getPatientId(), e);
        }
    }

    public boolean updateVitals(int patientId, String bp, Integer hr, Double temp, Integer spo2, Integer rr,
                                String symptoms, EmergencyLevel level) {
        StringBuilder sql = new StringBuilder("UPDATE patients SET updated_at = ?");
        if (bp != null) sql.append(", vital_bp = ?");
        if (hr != null) sql.append(", vital_heart_rate = ?");
        if (temp != null) sql.append(", vital_temp = ?");
        if (spo2 != null) sql.append(", vital_spo2 = ?");
        if (rr != null) sql.append(", vital_respiratory_rate = ?");
        if (symptoms != null) sql.append(", symptoms = ?");
        if (level != null) sql.append(", emergency_level = ?, priority = ?");
        sql.append(" WHERE patient_id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            stmt.setTimestamp(idx++, Timestamp.valueOf(LocalDateTime.now()));
            if (bp != null) stmt.setString(idx++, bp);
            if (hr != null) stmt.setInt(idx++, hr);
            if (temp != null) stmt.setDouble(idx++, temp);
            if (spo2 != null) stmt.setInt(idx++, spo2);
            if (rr != null) stmt.setInt(idx++, rr);
            if (symptoms != null) stmt.setString(idx++, symptoms);
            if (level != null) {
                stmt.setString(idx++, level.name());
                stmt.setInt(idx++, level.getPriority());
            }
            stmt.setInt(idx, patientId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating vitals for patient ID: " + patientId, e);
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

        // Extended vitals & demographics
        try {
            p.setAllergies(rs.getString("allergies"));
            p.setEmergencyContact(rs.getString("emergency_contact"));
            p.setVitalBp(rs.getString("vital_bp"));
            int hr = rs.getInt("vital_heart_rate");
            if (!rs.wasNull()) p.setVitalHeartRate(hr);
            double temp = rs.getDouble("vital_temp");
            if (!rs.wasNull()) p.setVitalTemperature(temp);
            int spo2 = rs.getInt("vital_spo2");
            if (!rs.wasNull()) p.setVitalSpo2(spo2);
            int rr = rs.getInt("vital_respiratory_rate");
            if (!rs.wasNull()) p.setVitalRespiratoryRate(rr);
        } catch (SQLException ignored) {
            // In case columns not present in legacy schema query
        }

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
