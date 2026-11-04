package com.college.parking.model;

import java.time.LocalDateTime;

/**
 * Model representing an active or completed parking session.
 */
public class ParkingSession {

    private int sessionId;
    private int vehicleId;
    private int slotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private SessionStatus sessionStatus;

    // Optional associated model objects
    private Vehicle vehicle;
    private ParkingSlot slot;

    public ParkingSession() {}

    public ParkingSession(int vehicleId, int slotId, LocalDateTime entryTime, SessionStatus sessionStatus) {
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.sessionStatus = sessionStatus;
    }

    public ParkingSession(int sessionId, int vehicleId, int slotId, LocalDateTime entryTime, LocalDateTime exitTime, SessionStatus sessionStatus) {
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.sessionStatus = sessionStatus;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public void setSlot(ParkingSlot slot) {
        this.slot = slot;
    }

    public boolean isActive() {
        return sessionStatus == SessionStatus.ACTIVE;
    }
}
