package com.college.parking.model;

import java.time.LocalDateTime;

/**
 * Model representing a physical parking bay/slot.
 */
public class ParkingSlot {

    private int slotId;
    private String slotNumber;
    private SlotStatus status;
    private LocalDateTime createdAt;

    // Transient helper fields for dashboard display
    private String currentVehicleReg;
    private LocalDateTime occupiedSince;

    public ParkingSlot() {}

    public ParkingSlot(String slotNumber, SlotStatus status) {
        this.slotNumber = slotNumber;
        this.status = status;
    }

    public ParkingSlot(int slotId, String slotNumber, SlotStatus status, LocalDateTime createdAt) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrentVehicleReg() {
        return currentVehicleReg;
    }

    public void setCurrentVehicleReg(String currentVehicleReg) {
        this.currentVehicleReg = currentVehicleReg;
    }

    public LocalDateTime getOccupiedSince() {
        return occupiedSince;
    }

    public void setOccupiedSince(LocalDateTime occupiedSince) {
        this.occupiedSince = occupiedSince;
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == SlotStatus.OCCUPIED;
    }

    @Override
    public String toString() {
        return slotNumber + " (" + status + ")";
    }
}
