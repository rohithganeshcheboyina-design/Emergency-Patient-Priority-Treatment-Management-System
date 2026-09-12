package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.EmergencyLevel;
import com.hospital.model.PatientStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for analytical dashboards and statistical reports.
 */
public class ReportDAO {

    public Map<String, Integer> getDashboardSummaryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT " +
                     "  COUNT(*) AS total_patients, " +
                     "  SUM(CASE WHEN emergency_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count, " +
                     "  SUM(CASE WHEN emergency_level = 'HIGH' THEN 1 ELSE 0 END) AS high_count, " +
                     "  SUM(CASE WHEN emergency_level = 'MEDIUM' THEN 1 ELSE 0 END) AS medium_count, " +
                     "  SUM(CASE WHEN emergency_level = 'LOW' THEN 1 ELSE 0 END) AS low_count, " +
                     "  SUM(CASE WHEN current_status = 'WAITING' THEN 1 ELSE 0 END) AS waiting_count, " +
                     "  SUM(CASE WHEN current_status = 'CALLED' THEN 1 ELSE 0 END) AS called_count, " +
                     "  SUM(CASE WHEN current_status = 'IN_TREATMENT' THEN 1 ELSE 0 END) AS in_treatment_count, " +
                     "  SUM(CASE WHEN current_status = 'TREATED' THEN 1 ELSE 0 END) AS treated_count, " +
                     "  SUM(CASE WHEN current_status = 'DISCHARGED' THEN 1 ELSE 0 END) AS discharged_count " +
                     "FROM patients";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                counts.put("total", rs.getInt("total_patients"));
                counts.put("critical", rs.getInt("critical_count"));
                counts.put("high", rs.getInt("high_count"));
                counts.put("medium", rs.getInt("medium_count"));
                counts.put("low", rs.getInt("low_count"));
                counts.put("waiting", rs.getInt("waiting_count"));
                counts.put("called", rs.getInt("called_count"));
                counts.put("in_treatment", rs.getInt("in_treatment_count"));
                counts.put("treated", rs.getInt("treated_count"));
                counts.put("discharged", rs.getInt("discharged_count"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving dashboard summary counts", e);
        }
        return counts;
    }

    public Map<String, Integer> getEmergencyLevelDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (EmergencyLevel level : EmergencyLevel.values()) {
            map.put(level.getDisplayName(), 0);
        }
        String sql = "SELECT emergency_level, COUNT(*) AS cnt FROM patients GROUP BY emergency_level";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String levelStr = rs.getString("emergency_level");
                try {
                    EmergencyLevel level = EmergencyLevel.fromString(levelStr);
                    map.put(level.getDisplayName(), rs.getInt("cnt"));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving emergency level distribution", e);
        }
        return map;
    }

    public Map<String, Integer> getStatusDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (PatientStatus status : PatientStatus.values()) {
            map.put(status.getDisplayName(), 0);
        }
        String sql = "SELECT current_status, COUNT(*) AS cnt FROM patients GROUP BY current_status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String statusStr = rs.getString("current_status");
                try {
                    PatientStatus status = PatientStatus.fromString(statusStr);
                    map.put(status.getDisplayName(), rs.getInt("cnt"));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving patient status distribution", e);
        }
        return map;
    }

    public Map<String, Integer> getDailyRegistrations(int days) {
        Map<String, Integer> map = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            map.put(today.minusDays(i).toString(), 0);
        }

        String sql = "SELECT CAST(arrival_time AS DATE) AS reg_date, COUNT(*) AS cnt " +
                     "FROM patients WHERE arrival_time >= CURRENT_DATE - INTERVAL ? DAY " +
                     "GROUP BY CAST(arrival_time AS DATE) ORDER BY reg_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("reg_date");
                    if (date != null && map.containsKey(date)) {
                        map.put(date, rs.getInt("cnt"));
                    }
                }
            }
        } catch (SQLException e) {
            // Fallback gracefully without throwing to keep chart functional on partial date support
            System.err.println("[ReportDAO] Note: Date aggregation fallback: " + e.getMessage());
        }
        return map;
    }

    public List<String[]> getPatientExportData() {
        List<String[]> rows = new ArrayList<>();
        String[] headers = new String[] {
            "Patient Code", "Token", "Full Name", "Age", "Gender", "Phone",
            "Emergency Level", "Priority", "Arrival Time", "Assigned Doctor", "Status"
        };
        rows.add(headers);

        String sql = "SELECT p.patient_code, p.token_number, p.full_name, p.age, p.gender, p.phone, " +
                     "       p.emergency_level, p.priority, p.arrival_time, u.full_name AS doctor_name, p.current_status " +
                     "FROM patients p " +
                     "LEFT JOIN users u ON p.assigned_doctor_id = u.user_id " +
                     "ORDER BY p.patient_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[] {
                    rs.getString("patient_code"),
                    rs.getString("token_number"),
                    rs.getString("full_name"),
                    String.valueOf(rs.getInt("age")),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("emergency_level"),
                    String.valueOf(rs.getInt("priority")),
                    rs.getString("arrival_time"),
                    rs.getString("doctor_name") != null ? rs.getString("doctor_name") : "Unassigned",
                    rs.getString("current_status")
                });
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error exporting patient data", e);
        }
        return rows;
    }
}
