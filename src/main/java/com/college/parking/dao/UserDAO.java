package com.college.parking.dao;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.model.User;
import com.college.parking.model.UserType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entities.
 * Handles CRUD operations on the 'users' table using PreparedStatement and try-with-resources.
 */
public class UserDAO {

    /**
     * Inserts a new user within an existing transaction connection.
     */
    public int insert(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO users (full_name, college_id, department, phone, user_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getCollegeId());
            ps.setString(3, user.getDepartment());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getUserType().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    user.setUserId(id);
                    return id;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Inserts a new user with auto-managed connection.
     */
    public int insert(User user) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return insert(user, conn);
        }
    }

    /**
     * Updates an existing user's information.
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, college_id = ?, department = ?, phone = ?, user_type = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getCollegeId());
            ps.setString(3, user.getDepartment());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getUserType().name());
            ps.setInt(6, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a user by ID (cascades to vehicles via foreign key constraint).
     */
    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by unique College ID.
     */
    public Optional<User> findByCollegeId(String collegeId) throws SQLException {
        String sql = "SELECT * FROM users WHERE college_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a college ID is already registered.
     */
    public boolean existsByCollegeId(String collegeId, int excludeUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE college_id = ? AND user_id != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collegeId);
            ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all users ordered by creation date descending.
     */
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setCollegeId(rs.getString("college_id"));
        u.setDepartment(rs.getString("department"));
        u.setPhone(rs.getString("phone"));
        u.setUserType(UserType.fromString(rs.getString("user_type")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}
