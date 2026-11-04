package com.college.parking.model;

import java.time.LocalDateTime;

/**
 * Model representing a registered college member (Student or Staff).
 * Demonstrates OOP Encapsulation with private fields and public accessors.
 */
public class User {

    private int userId;
    private String fullName;
    private String collegeId;
    private String department;
    private String phone;
    private UserType userType;
    private LocalDateTime createdAt;

    public User() {}

    public User(String fullName, String collegeId, String department, String phone, UserType userType) {
        this.fullName = fullName;
        this.collegeId = collegeId;
        this.department = department;
        this.phone = phone;
        this.userType = userType;
    }

    public User(int userId, String fullName, String collegeId, String department, String phone, UserType userType, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.collegeId = collegeId;
        this.department = department;
        this.phone = phone;
        this.userType = userType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(String collegeId) {
        this.collegeId = collegeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + collegeId + " - " + userType + ")";
    }
}
