package com.college.parking.dao;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.model.User;
import com.college.parking.model.UserType;
import com.college.parking.model.Vehicle;
import com.college.parking.model.VehicleType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Vehicle entities.
 * Interacts with 'vehicles' and joins with 'users' for comprehensive queries.
 */
public class VehicleDAO {

    /**
     * Inserts a new vehicle within a transaction connection.
     */
    public int insert(Vehicle vehicle, Connection conn) throws SQLException {
        String sql = "INSERT INTO vehicles (user_id, registration_number, vehicle_type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vehicle.getUserId());
            ps.setString(2, vehicle.getRegistrationNumber());
            ps.setString(3, vehicle.getVehicleType().name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating vehicle failed, no rows affected.");
            }
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    int id = gk.getInt(1);
                    vehicle.setVehicleId(id);
                    return id;
                } else {
                    throw new SQLException("Creating vehicle failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Inserts a new vehicle with auto-managed connection.
     */
    public int insert(Vehicle vehicle) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return insert(vehicle, conn);
        }
    }

    /**
     * Updates an existing vehicle.
     */
    public boolean update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET registration_number = ?, vehicle_type = ? WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicle.getRegistrationNumber());
            ps.setString(2, vehicle.getVehicleType().name());
            ps.setInt(3, vehicle.getVehicleId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a vehicle by ID.
     */
    public boolean delete(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds a vehicle by registration number (case-insensitive search).
     */
    public Optional<Vehicle> findByRegistrationNumber(String regNo) throws SQLException {
        String sql = "SELECT v.*, u.full_name, u.college_id, u.department, u.phone, u.user_type, u.created_at AS u_created_at " +
                     "FROM vehicles v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "WHERE UPPER(v.registration_number) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicle(rs, true));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a registration number already exists, excluding a specific vehicleId for edit.
     */
    public boolean existsByRegistrationNumber(String regNo, int excludeVehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE UPPER(registration_number) = UPPER(?) AND vehicle_id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            ps.setInt(2, excludeVehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all registered vehicles joined with their owner details.
     */
    public List<Vehicle> findAllWithOwners() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.*, u.full_name, u.college_id, u.department, u.phone, u.user_type, u.created_at AS u_created_at " +
                     "FROM vehicles v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "ORDER BY v.vehicle_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToVehicle(rs, true));
            }
        }
        return list;
    }

    /**
     * Searches registered vehicles by query string matching reg no, name, college id, or dept.
     */
    public List<Vehicle> searchVehicles(String query) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String term = "%" + (query == null ? "" : query.trim()) + "%";
        String sql = "SELECT v.*, u.full_name, u.college_id, u.department, u.phone, u.user_type, u.created_at AS u_created_at " +
                     "FROM vehicles v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "WHERE v.registration_number LIKE ? " +
                     "   OR u.full_name LIKE ? " +
                     "   OR u.college_id LIKE ? " +
                     "   OR u.department LIKE ? " +
                     "ORDER BY v.vehicle_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);
            ps.setString(4, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVehicle(rs, true));
                }
            }
        }
        return list;
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs, boolean mapOwner) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setUserId(rs.getInt("user_id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setVehicleType(VehicleType.fromString(rs.getString("vehicle_type")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            v.setCreatedAt(ts.toLocalDateTime());
        }

        if (mapOwner) {
            User u = new User();
            u.setUserId(rs.getInt("user_id"));
            u.setFullName(rs.getString("full_name"));
            u.setCollegeId(rs.getString("college_id"));
            u.setDepartment(rs.getString("department"));
            u.setPhone(rs.getString("phone"));
            u.setUserType(UserType.fromString(rs.getString("user_type")));
            Timestamp uTs = rs.getTimestamp("u_created_at");
            if (uTs != null) {
                u.setCreatedAt(uTs.toLocalDateTime());
            }
            v.setOwner(u);
        }
        return v;
    }
}
