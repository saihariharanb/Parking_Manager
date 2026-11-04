package com.college.parking.model;

import java.time.LocalDateTime;

/**
 * Model representing an administrator with login access.
 */
public class Admin {

    private int adminId;
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;

    public Admin() {}

    public Admin(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Admin(int adminId, String username, String passwordHash, LocalDateTime createdAt) {
        this.adminId = adminId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
