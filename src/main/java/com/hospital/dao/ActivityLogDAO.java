package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.ActivityLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for recording and querying system operational activity logs.
 */
public class ActivityLogDAO {

    public boolean log(Integer userId, String action, String entityType, Integer entityId, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action, entity_type, entity_id, timestamp, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null && userId > 0) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, entityType);
            if (entityId != null && entityId > 0) {
                stmt.setInt(4, entityId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(6, description);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ActivityLogDAO] Warning: Failed to record activity log: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getRecentLogs(int limit) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT l.log_id, l.user_id, u.username, l.action, l.entity_type, l.entity_id, " +
                     "       l.timestamp, l.description " +
                     "FROM activity_logs l " +
                     "LEFT JOIN users u ON l.user_id = u.user_id " +
                     "ORDER BY l.timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving recent activity logs", e);
        }
        return list;
    }

    public List<ActivityLog> getAllLogs() {
        return getRecentLogs(200);
    }

    private ActivityLog mapRowToLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            log.setUserId(uid);
        }
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        int eid = rs.getInt("entity_id");
        if (!rs.wasNull()) {
            log.setEntityId(eid);
        }
        Timestamp ts = rs.getTimestamp("timestamp");
        log.setTimestamp(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        log.setDescription(rs.getString("description"));
        return log;
    }
}
