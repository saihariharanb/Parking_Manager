package com.college.parking.model;

import java.time.LocalDateTime;

/**
 * Model representing a vehicle registered in the college parking system.
 * Contains user relationship and vehicle categorization.
 */
public class Vehicle {

    private int vehicleId;
    private int userId;
    private String registrationNumber;
    private VehicleType vehicleType;
    private LocalDateTime createdAt;

    // Optional navigation/association object for User details
    private User owner;

    public Vehicle() {}

    public Vehicle(int userId, String registrationNumber, VehicleType vehicleType) {
        this.userId = userId;
        this.registrationNumber = registrationNumber;
        this.vehicleType = vehicleType;
    }

    public Vehicle(int vehicleId, int userId, String registrationNumber, VehicleType vehicleType, LocalDateTime createdAt) {
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.registrationNumber = registrationNumber;
        this.vehicleType = vehicleType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return registrationNumber + " (" + vehicleType + ")";
    }
}
