package com.college.parking.dao;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for ParkingSession entities.
 * Handles recording entry, exit, history queries, and report aggregations.
 */
public class ParkingSessionDAO {

    /**
     * Inserts a new parking session within a transaction connection.
     */
    public int insert(ParkingSession session, Connection conn) throws SQLException {
        String sql = "INSERT INTO parking_sessions (vehicle_id, slot_id, entry_time, session_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getVehicleId());
            ps.setInt(2, session.getSlotId());
            ps.setTimestamp(3, Timestamp.valueOf(session.getEntryTime()));
            ps.setString(4, session.getSessionStatus().name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating parking session failed, no rows affected.");
            }
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    int id = gk.getInt(1);
                    session.setSessionId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Marks an active session as COMPLETED with exit_time within a transaction connection.
     */
    public boolean completeSession(int sessionId, LocalDateTime exitTime, Connection conn) throws SQLException {
        String sql = "UPDATE parking_sessions SET exit_time = ?, session_status = 'COMPLETED' WHERE session_id = ? AND session_status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(exitTime));
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds active session for a vehicle within a transaction connection (using FOR UPDATE to prevent race conditions).
     */
    public Optional<ParkingSession> findActiveSessionByVehicleIdForUpdate(int vehicleId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM parking_sessions WHERE vehicle_id = ? AND session_status = 'ACTIVE' LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSession(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds active session for a vehicle by registration number.
     */
    public Optional<ParkingSession> findActiveSessionByRegistration(String regNo) throws SQLException {
        String sql = "SELECT s.*, v.registration_number, v.vehicle_type, ps.slot_number " +
                     "FROM parking_sessions s " +
                     "JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                     "JOIN parking_slots ps ON s.slot_id = ps.slot_id " +
                     "WHERE UPPER(v.registration_number) = UPPER(?) AND s.session_status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ParkingSession s = mapResultSetToSession(rs);
                    Vehicle v = new Vehicle();
                    v.setVehicleId(s.getVehicleId());
                    v.setRegistrationNumber(rs.getString("registration_number"));
                    v.setVehicleType(VehicleType.fromString(rs.getString("vehicle_type")));
                    s.setVehicle(v);

                    ParkingSlot slot = new ParkingSlot();
                    slot.setSlotId(s.getSlotId());
                    slot.setSlotNumber(rs.getString("slot_number"));
                    s.setSlot(slot);

                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds active session occupying a specific slot.
     */
    public Optional<ParkingSession> findActiveSessionBySlotId(int slotId) throws SQLException {
        String sql = "SELECT * FROM parking_sessions WHERE slot_id = ? AND session_status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSession(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves recent parking activity for the Dashboard view.
     */
    public List<ParkingActivityView> findRecentActivity(int limit) throws SQLException {
        List<ParkingActivityView> list = new ArrayList<>();
        String sql = "SELECT s.session_id, v.registration_number, u.full_name, u.college_id, u.department, " +
                     "       v.vehicle_type, u.user_type, ps.slot_number, s.entry_time, s.exit_time, s.session_status " +
                     "FROM parking_sessions s " +
                     "JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "JOIN parking_slots ps ON s.slot_id = ps.slot_id " +
                     "ORDER BY s.session_id DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToActivityView(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches history with filters for date, vehicle type, search query (registration, name, college id, dept).
     */
    public List<ParkingActivityView> searchHistory(String query, VehicleType vehicleType, String dateStr, SessionStatus status) throws SQLException {
        List<ParkingActivityView> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT s.session_id, v.registration_number, u.full_name, u.college_id, u.department, " +
            "       v.vehicle_type, u.user_type, ps.slot_number, s.entry_time, s.exit_time, s.session_status " +
            "FROM parking_sessions s " +
            "JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
            "JOIN users u ON v.user_id = u.user_id " +
            "JOIN parking_slots ps ON s.slot_id = ps.slot_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String term = "%" + query.trim() + "%";
            sql.append("AND (v.registration_number LIKE ? OR u.full_name LIKE ? OR u.college_id LIKE ? OR u.department LIKE ?) ");
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (vehicleType != null) {
            sql.append("AND v.vehicle_type = ? ");
            params.add(vehicleType.name());
        }

        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append("AND DATE(s.entry_time) = ? ");
            params.add(dateStr.trim());
        }

        if (status != null) {
            sql.append("AND s.session_status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY s.session_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToActivityView(rs));
                }
            }
        }
        return list;
    }

    /**
     * Counts currently active parking sessions.
     */
    public int countActiveSessions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_sessions WHERE session_status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Counts total parking sessions recorded.
     */
    public int countTotalSessions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_sessions";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Counts sessions registered today.
     */
    public int countTodaySessions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_sessions WHERE DATE(entry_time) = CURDATE()";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Group count of parked vehicles by vehicle type (Bike, Scooter, Car).
     * Used for Reports screen.
     */
    public Map<String, Integer> getVehicleTypeBreakdown() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT v.vehicle_type, COUNT(s.session_id) AS total " +
                     "FROM parking_sessions s " +
                     "JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                     "GROUP BY v.vehicle_type";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("vehicle_type"), rs.getInt("total"));
            }
        }
        return map;
    }

    /**
     * Top most utilized parking slots.
     */
    public Map<String, Integer> getMostUtilizedSlots(int limit) throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT ps.slot_number, COUNT(s.session_id) AS usage_count " +
                     "FROM parking_slots ps " +
                     "LEFT JOIN parking_sessions s ON ps.slot_id = s.slot_id " +
                     "GROUP BY ps.slot_id, ps.slot_number " +
                     "ORDER BY usage_count DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("slot_number"), rs.getInt("usage_count"));
                }
            }
        }
        return map;
    }

    private ParkingSession mapResultSetToSession(ResultSet rs) throws SQLException {
        ParkingSession s = new ParkingSession();
        s.setSessionId(rs.getInt("session_id"));
        s.setVehicleId(rs.getInt("vehicle_id"));
        s.setSlotId(rs.getInt("slot_id"));
        Timestamp entryTs = rs.getTimestamp("entry_time");
        if (entryTs != null) {
            s.setEntryTime(entryTs.toLocalDateTime());
        }
        Timestamp exitTs = rs.getTimestamp("exit_time");
        if (exitTs != null) {
            s.setExitTime(exitTs.toLocalDateTime());
        }
        s.setSessionStatus(SessionStatus.fromString(rs.getString("session_status")));
        return s;
    }

    private ParkingActivityView mapResultSetToActivityView(ResultSet rs) throws SQLException {
        ParkingActivityView v = new ParkingActivityView();
        v.setSessionId(rs.getInt("session_id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setOwnerName(rs.getString("full_name"));
        v.setCollegeId(rs.getString("college_id"));
        v.setDepartment(rs.getString("department"));
        v.setVehicleType(VehicleType.fromString(rs.getString("vehicle_type")));
        v.setUserType(UserType.fromString(rs.getString("user_type")));
        v.setSlotNumber(rs.getString("slot_number"));
        Timestamp entryTs = rs.getTimestamp("entry_time");
        if (entryTs != null) {
            v.setEntryTime(entryTs.toLocalDateTime());
        }
        Timestamp exitTs = rs.getTimestamp("exit_time");
        if (exitTs != null) {
            v.setExitTime(exitTs.toLocalDateTime());
        }
        v.setSessionStatus(SessionStatus.fromString(rs.getString("session_status")));
        return v;
    }
}
