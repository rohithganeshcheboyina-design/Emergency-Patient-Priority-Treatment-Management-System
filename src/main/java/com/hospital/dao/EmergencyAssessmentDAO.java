package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.EmergencyAssessment;
import com.hospital.model.EmergencyLevel;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Emergency Triage Assessments.
 */
public class EmergencyAssessmentDAO {

    public boolean createAssessment(EmergencyAssessment assessment) {
        String sql = "INSERT INTO emergency_assessments (patient_id, emergency_level, priority, " +
                     "symptoms, assessed_by, assessed_at, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, assessment.getPatientId());
            stmt.setString(2, assessment.getEmergencyLevel().name());
            stmt.setInt(3, assessment.getPriority());
            stmt.setString(4, assessment.getSymptoms());
            stmt.setInt(5, assessment.getAssessedBy());
            stmt.setTimestamp(6, Timestamp.valueOf(assessment.getAssessedAt() != null ? assessment.getAssessedAt() : LocalDateTime.now()));
            stmt.setString(7, assessment.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        assessment.setAssessmentId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error recording emergency assessment for patient: " + assessment.getPatientId(), e);
        }
        return false;
    }

    public List<EmergencyAssessment> getAssessmentsByPatientId(int patientId) {
        List<EmergencyAssessment> list = new ArrayList<>();
        String sql = "SELECT a.assessment_id, a.patient_id, a.emergency_level, a.priority, " +
                     "       a.symptoms, a.assessed_by, u.full_name AS assessor_name, a.assessed_at, a.notes " +
                     "FROM emergency_assessments a " +
                     "JOIN users u ON a.assessed_by = u.user_id " +
                     "WHERE a.patient_id = ? ORDER BY a.assessed_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmergencyAssessment ea = new EmergencyAssessment();
                    ea.setAssessmentId(rs.getInt("assessment_id"));
                    ea.setPatientId(rs.getInt("patient_id"));
                    ea.setEmergencyLevel(EmergencyLevel.fromString(rs.getString("emergency_level")));
                    ea.setPriority(rs.getInt("priority"));
                    ea.setSymptoms(rs.getString("symptoms"));
                    ea.setAssessedBy(rs.getInt("assessed_by"));
                    ea.setAssessorName(rs.getString("assessor_name"));
                    Timestamp ts = rs.getTimestamp("assessed_at");
                    ea.setAssessedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
                    ea.setNotes(rs.getString("notes"));
                    list.add(ea);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving assessments for patient ID: " + patientId, e);
        }
        return list;
    }
}
