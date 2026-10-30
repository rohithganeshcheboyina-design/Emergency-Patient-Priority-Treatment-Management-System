package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.Bed;
import com.hospital.model.BedStatus;
import com.hospital.model.BedType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for hospital emergency bed management and allocations.
 */
public class BedDAO {

    private static final String SELECT_BASE =
            "SELECT b.bed_id, b.bed_number, b.bed_type, b.status, b.assigned_patient_id, " +
            "       p.full_name AS patient_name, p.patient_code, b.assigned_at, b.notes " +
            "FROM beds b " +
            "LEFT JOIN patients p ON b.assigned_patient_id = p.patient_id ";

    public List<Bed> getAllBeds() {
        List<Bed> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY b.bed_type ASC, b.bed_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToBed(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving hospital beds", e);
        }
        return list;
    }

    public List<Bed> getAvailableBeds() {
        List<Bed> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE b.status = 'AVAILABLE' ORDER BY b.bed_type ASC, b.bed_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToBed(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving available beds", e);
        }
        return list;
    }

    public Bed findById(int bedId) {
        String sql = SELECT_BASE + "WHERE b.bed_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bedId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBed(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding bed by ID: " + bedId, e);
        }
        return null;
    }

    public boolean createBed(Bed bed) {
        String sql = "INSERT INTO beds (bed_number, bed_type, status, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bed.getBedNumber());
            stmt.setString(2, bed.getBedType().name());
            stmt.setString(3, bed.getStatus() != null ? bed.getStatus().name() : BedStatus.AVAILABLE.name());
            stmt.setString(4, bed.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        bed.setBedId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating bed " + bed.getBedNumber(), e);
        }
        return false;
    }

    public boolean updateBed(Bed bed) {
        String sql = "UPDATE beds SET bed_number = ?, bed_type = ?, status = ?, notes = ? WHERE bed_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bed.getBedNumber());
            stmt.setString(2, bed.getBedType().name());
            stmt.setString(3, bed.getStatus().name());
            stmt.setString(4, bed.getNotes());
            stmt.setInt(5, bed.getBedId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating bed " + bed.getBedId(), e);
        }
    }

    public boolean assignBed(int bedId, int patientId, String notes) {
        String sql = "UPDATE beds SET status = 'OCCUPIED', assigned_patient_id = ?, assigned_at = ?, notes = ? WHERE bed_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, notes);
            stmt.setInt(4, bedId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error assigning bed " + bedId + " to patient " + patientId, e);
        }
    }

    public boolean releaseBed(int bedId) {
        String sql = "UPDATE beds SET status = 'AVAILABLE', assigned_patient_id = NULL, assigned_at = NULL, notes = 'Sanitized and ready' WHERE bed_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bedId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error releasing bed " + bedId, e);
        }
    }

    public Map<String, Integer> getBedStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("available", 0);
        stats.put("occupied", 0);
        stats.put("emergency", 0);
        stats.put("icu", 0);

        String sql = "SELECT status, bed_type, COUNT(*) as cnt FROM beds GROUP BY status, bed_type";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int total = 0;
            int avail = 0;
            int occ = 0;
            int emg = 0;
            int icu = 0;

            while (rs.next()) {
                String st = rs.getString("status");
                String tp = rs.getString("bed_type");
                int count = rs.getInt("cnt");
                total += count;

                if ("AVAILABLE".equalsIgnoreCase(st)) avail += count;
                if ("OCCUPIED".equalsIgnoreCase(st)) occ += count;
                if ("EMERGENCY".equalsIgnoreCase(tp)) emg += count;
                if ("ICU".equalsIgnoreCase(tp)) icu += count;
            }

            stats.put("total", total);
            stats.put("available", avail);
            stats.put("occupied", occ);
            stats.put("emergency", emg);
            stats.put("icu", icu);
        } catch (SQLException e) {
            // fallback defaults
        }
        return stats;
    }

    private Bed mapRowToBed(ResultSet rs) throws SQLException {
        Bed b = new Bed();
        b.setBedId(rs.getInt("bed_id"));
        b.setBedNumber(rs.getString("bed_number"));
        b.setBedType(BedType.fromString(rs.getString("bed_type")));
        b.setStatus(BedStatus.fromString(rs.getString("status")));

        int pId = rs.getInt("assigned_patient_id");
        if (!rs.wasNull()) {
            b.setAssignedPatientId(pId);
            b.setAssignedPatientName(rs.getString("patient_name"));
            b.setAssignedPatientCode(rs.getString("patient_code"));
        }

        Timestamp ts = rs.getTimestamp("assigned_at");
        b.setAssignedAt(ts != null ? ts.toLocalDateTime() : null);
        b.setNotes(rs.getString("notes"));
        return b;
    }
}
