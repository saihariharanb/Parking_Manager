package com.college.parking.service;

import com.college.parking.dao.AdminDAO;
import com.college.parking.model.Admin;
import com.college.parking.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service handling administrator authentication and credentials validation.
 */
public class AuthenticationService {

    private final AdminDAO adminDAO;
    private static Admin currentAdmin = null;

    public AuthenticationService() {
        this.adminDAO = new AdminDAO();
    }

    public AuthenticationService(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    /**
     * Authenticates an administrator with the given username and plaintext password.
     * Verifies the SHA-256 hash against the database record.
     *
     * @param username administrator username
     * @param plainPassword plaintext password
     * @return true if credentials are valid, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean login(String username, String plainPassword) throws SQLException {
        if (username == null || plainPassword == null || username.trim().isEmpty()) {
            return false;
        }

        Optional<Admin> adminOpt = adminDAO.findByUsername(username.trim());
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (PasswordUtil.verifyPassword(plainPassword, admin.getPasswordHash())) {
                currentAdmin = admin;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the current administrator.
     */
    public void logout() {
        currentAdmin = null;
    }

    /**
     * Returns the currently logged in administrator or null.
     */
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public static boolean isLoggedIn() {
        return currentAdmin != null;
    }
}
