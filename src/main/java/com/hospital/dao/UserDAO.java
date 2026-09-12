package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities.
 */
public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, full_name, role, phone, email, active, created_at " +
                     "FROM users WHERE username = ? AND active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username: " + username, e);
        }
        return null;
    }

    public User findById(int userId) {
        String sql = "SELECT user_id, username, password_hash, full_name, role, phone, email, active, created_at " +
                     "FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID: " + userId, e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, role, phone, email, active, created_at " +
                     "FROM users ORDER BY role, full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all users", e);
        }
        return list;
    }

    public List<Doctor> getAvailableDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, role, phone, email, active, created_at " +
                     "FROM users WHERE role = 'DOCTOR' AND active = TRUE ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = mapRowToUser(rs);
                if (u instanceof Doctor) {
                    list.add((Doctor) u);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving doctors", e);
        }
        return list;
    }

    public List<Nurse> getAvailableNurses() {
        List<Nurse> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, role, phone, email, active, created_at " +
                     "FROM users WHERE role = 'NURSE' AND active = TRUE ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = mapRowToUser(rs);
                if (u instanceof Nurse) {
                    list.add((Nurse) u);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving nurses", e);
        }
        return list;
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, phone, email, active, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getEmail());
            stmt.setBoolean(7, user.isActive());
            stmt.setTimestamp(8, Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user: " + user.getUsername(), e);
        }
        return false;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, role = ?, phone = ?, email = ?, active = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getEmail());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user ID: " + user.getUserId(), e);
        }
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password for user ID: " + userId, e);
        }
    }

    public boolean deleteUser(int userId) {
        // Soft delete for audit integrity
        String sql = "UPDATE users SET active = FALSE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deactivating user ID: " + userId, e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        Role role = Role.fromString(rs.getString("role"));
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        boolean active = rs.getBoolean("active");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        User user;
        if (role == Role.DOCTOR) {
            user = new Doctor(id, username, passwordHash, fullName, phone, email, active, createdAt, "Emergency Physician", "Emergency Medicine");
        } else if (role == Role.NURSE) {
            user = new Nurse(id, username, passwordHash, fullName, phone, email, active, createdAt, "General Shift", "Triage Station 1");
        } else {
            user = new Admin(id, username, passwordHash, fullName, phone, email, active, createdAt);
        }
        return user;
    }
}
