package com.college.parking.model;

/**
 * Data Transfer Object (DTO) holding aggregated statistics for the Dashboard.
 * Loaded from MySQL queries rather than hardcoded.
 */
public class DashboardStats {

    private int totalSlots;
    private int availableSlots;
    private int occupiedSlots;
    private int maintenanceSlots;
    private int currentlyParkedCount;
    private int totalSessionsCount;
    private int todaySessionsCount;

    public DashboardStats() {}

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(int occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public int getMaintenanceSlots() {
        return maintenanceSlots;
    }

    public void setMaintenanceSlots(int maintenanceSlots) {
        this.maintenanceSlots = maintenanceSlots;
    }

    public int getCurrentlyParkedCount() {
        return currentlyParkedCount;
    }

    public void setCurrentlyParkedCount(int currentlyParkedCount) {
        this.currentlyParkedCount = currentlyParkedCount;
    }

    public int getTotalSessionsCount() {
        return totalSessionsCount;
    }

    public void setTotalSessionsCount(int totalSessionsCount) {
        this.totalSessionsCount = totalSessionsCount;
    }

    public int getTodaySessionsCount() {
        return todaySessionsCount;
    }

    public void setTodaySessionsCount(int todaySessionsCount) {
        this.todaySessionsCount = todaySessionsCount;
    }

    public double getOccupancyPercentage() {
        if (totalSlots == 0) return 0.0;
        return (double) occupiedSlots / totalSlots * 100.0;
    }
}
