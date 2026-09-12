package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.PatientStatus;
import com.hospital.model.PatientStatusHistory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for patient workflow status audit trail.
 */
public class PatientStatusHistoryDAO {

    public boolean recordStatusChange(int patientId, PatientStatus oldStatus, PatientStatus newStatus,
                                      int changedBy, String remarks) {
        String sql = "INSERT INTO patient_status_history (patient_id, old_status, new_status, changed_by, changed_at, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, patientId);
            if (oldStatus != null) {
                stmt.setString(2, oldStatus.name());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }
            stmt.setString(3, newStatus.name());
            stmt.setInt(4, changedBy);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(6, remarks);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error recording status history for patient ID: " + patientId, e);
        }
    }

    public List<PatientStatusHistory> getHistoryByPatientId(int patientId) {
        List<PatientStatusHistory> list = new ArrayList<>();
        String sql = "SELECT h.history_id, h.patient_id, h.old_status, h.new_status, " +
                     "       h.changed_by, u.full_name AS changed_by_name, h.changed_at, h.remarks " +
                     "FROM patient_status_history h " +
                     "JOIN users u ON h.changed_by = u.user_id " +
                     "WHERE h.patient_id = ? ORDER BY h.changed_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientStatusHistory psh = new PatientStatusHistory();
                    psh.setHistoryId(rs.getInt("history_id"));
                    psh.setPatientId(rs.getInt("patient_id"));
                    String oldS = rs.getString("old_status");
                    psh.setOldStatus(oldS != null ? PatientStatus.fromString(oldS) : null);
                    psh.setNewStatus(PatientStatus.fromString(rs.getString("new_status")));
                    psh.setChangedBy(rs.getInt("changed_by"));
                    psh.setChangedByName(rs.getString("changed_by_name"));
                    Timestamp ts = rs.getTimestamp("changed_at");
                    psh.setChangedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
                    psh.setRemarks(rs.getString("remarks"));
                    list.add(psh);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving status history for patient ID: " + patientId, e);
        }
        return list;
    }
}
