package com.college.parking.dao;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.model.ParkingSlot;
import com.college.parking.model.SlotStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for ParkingSlot entities.
 * Manages physical parking slot state and concurrency locking.
 */
public class ParkingSlotDAO {

    /**
     * Inserts a new parking slot.
     */
    public int insert(ParkingSlot slot) throws SQLException {
        String sql = "INSERT INTO parking_slots (slot_number, status) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, slot.getSlotNumber().trim().toUpperCase());
            ps.setString(2, slot.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating slot failed, no rows affected.");
            }
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    int id = gk.getInt(1);
                    slot.setSlotId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Finds a slot by ID.
     */
    public Optional<ParkingSlot> findById(int slotId) throws SQLException {
        String sql = "SELECT * FROM parking_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a slot by slot number (e.g. A01).
     */
    public Optional<ParkingSlot> findBySlotNumber(String slotNumber) throws SQLException {
        String sql = "SELECT * FROM parking_slots WHERE UPPER(slot_number) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a slot number already exists.
     */
    public boolean existsBySlotNumber(String slotNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE UPPER(slot_number) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves an available parking slot with pessimistic locking (FOR UPDATE)
     * within a database transaction to prevent race conditions during concurrent entry.
     */
    public Optional<ParkingSlot> findAvailableSlotForUpdate(Connection conn) throws SQLException {
        String sql = "SELECT * FROM parking_slots WHERE status = 'AVAILABLE' ORDER BY slot_number ASC LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapResultSetToSlot(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Updates slot status within an existing transaction connection.
     */
    public boolean updateStatus(int slotId, SlotStatus newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, slotId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates slot status with an auto-managed connection.
     */
    public boolean updateStatus(int slotId, SlotStatus newStatus) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return updateStatus(slotId, newStatus, conn);
        }
    }

    /**
     * Retrieves all parking slots with the active occupant vehicle's registration number (if any).
     * Powers the visual parking layout on the Dashboard and Slot Management screen.
     */
    public List<ParkingSlot> findAllWithCurrentOccupant() throws SQLException {
        List<ParkingSlot> list = new ArrayList<>();
        String sql = "SELECT ps.slot_id, ps.slot_number, ps.status, ps.created_at, " +
                     "       v.registration_number AS active_reg_no, sess.entry_time AS occupied_since " +
                     "FROM parking_slots ps " +
                     "LEFT JOIN parking_sessions sess ON ps.slot_id = sess.slot_id AND sess.session_status = 'ACTIVE' " +
                     "LEFT JOIN vehicles v ON sess.vehicle_id = v.vehicle_id " +
                     "ORDER BY ps.slot_number ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ParkingSlot slot = mapResultSetToSlot(rs);
                slot.setCurrentVehicleReg(rs.getString("active_reg_no"));
                Timestamp ts = rs.getTimestamp("occupied_since");
                if (ts != null) {
                    slot.setOccupiedSince(ts.toLocalDateTime());
                }
                list.add(slot);
            }
        }
        return list;
    }

    /**
     * Counts slots by status.
     */
    public int countByStatus(SlotStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE status = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Counts all slots.
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private ParkingSlot mapResultSetToSlot(ResultSet rs) throws SQLException {
        ParkingSlot s = new ParkingSlot();
        s.setSlotId(rs.getInt("slot_id"));
        s.setSlotNumber(rs.getString("slot_number"));
        s.setStatus(SlotStatus.fromString(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            s.setCreatedAt(ts.toLocalDateTime());
        }
        return s;
    }
}
