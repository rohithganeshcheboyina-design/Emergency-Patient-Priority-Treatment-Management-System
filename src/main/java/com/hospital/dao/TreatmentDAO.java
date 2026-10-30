package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.Treatment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for clinical treatments, diagnosis, medication orders, and procedures.
 */
public class TreatmentDAO {

    public boolean createTreatment(Treatment treatment) {
        String sql = "INSERT INTO treatments (patient_id, doctor_id, start_time, end_time, treatment_status, diagnosis, medications, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, treatment.getPatientId());
            stmt.setInt(2, treatment.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(treatment.getStartTime() != null ? treatment.getStartTime() : LocalDateTime.now()));
            if (treatment.getEndTime() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(treatment.getEndTime()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setString(5, treatment.getTreatmentStatus() != null ? treatment.getTreatmentStatus() : "IN_PROGRESS");
            stmt.setString(6, treatment.getDiagnosis());
            stmt.setString(7, treatment.getMedications());
            stmt.setString(8, treatment.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        treatment.setTreatmentId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating treatment record for patient: " + treatment.getPatientId(), e);
        }
        return false;
    }

    public boolean updateTreatment(Treatment treatment) {
        String sql = "UPDATE treatments SET end_time = ?, treatment_status = ?, diagnosis = ?, medications = ?, notes = ? WHERE treatment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (treatment.getEndTime() != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(treatment.getEndTime()));
            } else {
                stmt.setNull(1, Types.TIMESTAMP);
            }
            stmt.setString(2, treatment.getTreatmentStatus());
            stmt.setString(3, treatment.getDiagnosis());
            stmt.setString(4, treatment.getMedications());
            stmt.setString(5, treatment.getNotes());
            stmt.setInt(6, treatment.getTreatmentId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating treatment ID: " + treatment.getTreatmentId(), e);
        }
    }

    public Treatment getActiveTreatmentForPatient(int patientId) {
        String sql = "SELECT t.treatment_id, t.patient_id, t.doctor_id, u.full_name AS doctor_name, " +
                     "       t.start_time, t.end_time, t.treatment_status, t.diagnosis, t.medications, t.notes " +
                     "FROM treatments t " +
                     "JOIN users u ON t.doctor_id = u.user_id " +
                     "WHERE t.patient_id = ? AND t.treatment_status = 'IN_PROGRESS' ORDER BY t.treatment_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTreatment(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving active treatment for patient: " + patientId, e);
        }
        return null;
    }

    public List<Treatment> getTreatmentsByPatientId(int patientId) {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT t.treatment_id, t.patient_id, t.doctor_id, u.full_name AS doctor_name, " +
                     "       t.start_time, t.end_time, t.treatment_status, t.diagnosis, t.medications, t.notes " +
                     "FROM treatments t " +
                     "JOIN users u ON t.doctor_id = u.user_id " +
                     "WHERE t.patient_id = ? ORDER BY t.start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTreatment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving treatments for patient: " + patientId, e);
        }
        return list;
    }

    private Treatment mapRowToTreatment(ResultSet rs) throws SQLException {
        Treatment t = new Treatment();
        t.setTreatmentId(rs.getInt("treatment_id"));
        t.setPatientId(rs.getInt("patient_id"));
        t.setDoctorId(rs.getInt("doctor_id"));
        t.setDoctorName(rs.getString("doctor_name"));

        Timestamp st = rs.getTimestamp("start_time");
        t.setStartTime(st != null ? st.toLocalDateTime() : LocalDateTime.now());

        Timestamp et = rs.getTimestamp("end_time");
        t.setEndTime(et != null ? et.toLocalDateTime() : null);

        t.setTreatmentStatus(rs.getString("treatment_status"));

        try {
            t.setDiagnosis(rs.getString("diagnosis"));
            t.setMedications(rs.getString("medications"));
        } catch (SQLException ignored) {}

        t.setNotes(rs.getString("notes"));
        return t;
    }
}
