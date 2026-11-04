package com.college.parking.dao;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.model.Admin;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object for Admin entities.
 */
public class AdminDAO {

    /**
     * Finds an administrator by username.
     */
    public Optional<Admin> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setAdminId(rs.getInt("admin_id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setPasswordHash(rs.getString("password_hash"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        admin.setCreatedAt(ts.toLocalDateTime());
                    }
                    return Optional.of(admin);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts an admin.
     */
    public boolean insert(Admin admin) throws SQLException {
        String sql = "INSERT INTO admins (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPasswordHash());
            return ps.executeUpdate() > 0;
        }
    }
}
