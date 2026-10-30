package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for scheduled non-emergency consultations and appointments.
 */
public class AppointmentDAO {

    private static final String SELECT_BASE =
            "SELECT a.appointment_id, a.patient_id, p.full_name AS patient_name, p.patient_code, " +
            "       a.doctor_id, u.full_name AS doctor_name, a.appointment_time, a.specialty, " +
            "       a.status, a.reason, a.notes, a.created_at " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN users u ON a.doctor_id = u.user_id ";

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.appointment_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToAppointment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving appointments", e);
        }
        return list;
    }

    public List<Appointment> getUpcomingAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.status IN ('SCHEDULED', 'CONFIRMED') AND a.appointment_time >= ? " +
                     "ORDER BY a.appointment_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving upcoming appointments", e);
        }
        return list;
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.patient_id = ? ORDER BY a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving patient appointments: " + patientId, e);
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.doctor_id = ? ORDER BY a.appointment_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving doctor appointments: " + doctorId, e);
        }
        return list;
    }

    public boolean createAppointment(Appointment a) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_time, specialty, status, reason, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, a.getPatientId());
            stmt.setInt(2, a.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(a.getAppointmentTime()));
            stmt.setString(4, a.getSpecialty());
            stmt.setString(5, a.getStatus() != null ? a.getStatus().name() : AppointmentStatus.SCHEDULED.name());
            stmt.setString(6, a.getReason());
            stmt.setString(7, a.getNotes());
            stmt.setTimestamp(8, Timestamp.valueOf(a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.now()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        a.setAppointmentId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error creating appointment", e);
        }
        return false;
    }

    public boolean cancelAppointment(int appointmentId, String reason) {
        String sql = "UPDATE appointments SET status = 'CANCELLED', notes = CONCAT(COALESCE(notes, ''), ' [Cancelled: ', ?, ']') WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reason != null ? reason : "Cancelled by user");
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error cancelling appointment: " + appointmentId, e);
        }
    }

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setPatientCode(rs.getString("patient_code"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));

        Timestamp ts = rs.getTimestamp("appointment_time");
        a.setAppointmentTime(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        a.setSpecialty(rs.getString("specialty"));
        a.setStatus(AppointmentStatus.fromString(rs.getString("status")));
        a.setReason(rs.getString("reason"));
        a.setNotes(rs.getString("notes"));

        Timestamp cts = rs.getTimestamp("created_at");
        a.setCreatedAt(cts != null ? cts.toLocalDateTime() : LocalDateTime.now());
        return a;
    }
}
