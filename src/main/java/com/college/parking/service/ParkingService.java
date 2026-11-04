package com.college.parking.service;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.dao.ParkingSessionDAO;
import com.college.parking.dao.ParkingSlotDAO;
import com.college.parking.dao.VehicleDAO;
import com.college.parking.model.*;
import com.college.parking.util.DateTimeUtil;
import com.college.parking.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing core parking operations:
 * - Transactional vehicle entry with automatic slot allocation.
 * - Transactional vehicle exit with slot release and duration tracking.
 * - Dashboard statistics aggregation.
 * - Safe parking slot configuration.
 */
public class ParkingService {

    private final ParkingSlotDAO slotDAO;
    private final ParkingSessionDAO sessionDAO;
    private final VehicleDAO vehicleDAO;

    public ParkingService() {
        this.slotDAO = new ParkingSlotDAO();
        this.sessionDAO = new ParkingSessionDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    public ParkingService(ParkingSlotDAO slotDAO, ParkingSessionDAO sessionDAO, VehicleDAO vehicleDAO) {
        this.slotDAO = slotDAO;
        this.sessionDAO = sessionDAO;
        this.vehicleDAO = vehicleDAO;
    }

    /**
     * Data carrier for vehicle entry transaction outcome.
     */
    public static class EntryResult {
        private final String slotNumber;
        private final String registrationNumber;
        private final String ownerName;
        private final String collegeId;
        private final UserType userType;
        private final LocalDateTime entryTime;

        public EntryResult(String slotNumber, String registrationNumber, String ownerName, String collegeId, UserType userType, LocalDateTime entryTime) {
            this.slotNumber = slotNumber;
            this.registrationNumber = registrationNumber;
            this.ownerName = ownerName;
            this.collegeId = collegeId;
            this.userType = userType;
            this.entryTime = entryTime;
        }

        public String getSlotNumber() { return slotNumber; }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getOwnerName() { return ownerName; }
        public String getCollegeId() { return collegeId; }
        public UserType getUserType() { return userType; }
        public LocalDateTime getEntryTime() { return entryTime; }
    }

    /**
     * Data carrier for vehicle exit transaction outcome.
     */
    public static class ExitResult {
        private final String slotNumber;
        private final String registrationNumber;
        private final String ownerName;
        private final LocalDateTime entryTime;
        private final LocalDateTime exitTime;
        private final String durationFormatted;

        public ExitResult(String slotNumber, String registrationNumber, String ownerName, LocalDateTime entryTime, LocalDateTime exitTime, String durationFormatted) {
            this.slotNumber = slotNumber;
            this.registrationNumber = registrationNumber;
            this.ownerName = ownerName;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.durationFormatted = durationFormatted;
        }

        public String getSlotNumber() { return slotNumber; }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getOwnerName() { return ownerName; }
        public LocalDateTime getEntryTime() { return entryTime; }
        public LocalDateTime getExitTime() { return exitTime; }
        public String getDurationFormatted() { return durationFormatted; }
    }

    /**
     * Processes vehicle entry using a multi-step ACID database transaction.
     * 1. Validates vehicle registration.
     * 2. Acquires row-level pessimistic lock to ensure vehicle is not already parked.
     * 3. Selects the first available parking slot with pessimistic lock (FOR UPDATE).
     * 4. Updates slot status to OCCUPIED.
     * 5. Inserts a new active parking_session record.
     * 6. Commits transaction; on any error, automatically rolls back.
     *
     * @param rawRegistrationNumber raw registration string entered by operator
     * @return EntryResult containing assigned slot and vehicle details
     * @throws IllegalArgumentException if vehicle is not registered or already inside
     * @throws IllegalStateException if parking lot is at full capacity
     * @throws SQLException on database transaction error
     */
    public EntryResult processVehicleEntry(String rawRegistrationNumber) throws SQLException {
        String regNo = ValidationUtil.cleanRegistrationNumber(rawRegistrationNumber);
        if (regNo.isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }

        // 1. Verify vehicle registration
        Optional<Vehicle> vehicleOpt = vehicleDAO.findByRegistrationNumber(regNo);
        if (vehicleOpt.isEmpty()) {
            throw new IllegalArgumentException("Vehicle [" + regNo + "] is not registered in the system.\nPlease register the student/staff vehicle first.");
        }
        Vehicle vehicle = vehicleOpt.get();

        // 2. Perform transactional entry
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // Check if vehicle already has an active session
            Optional<ParkingSession> activeSession = sessionDAO.findActiveSessionByVehicleIdForUpdate(vehicle.getVehicleId(), conn);
            if (activeSession.isPresent()) {
                conn.rollback();
                throw new IllegalArgumentException("Vehicle [" + regNo + "] is ALREADY parked in slot [" + 
                        getSlotNumberById(activeSession.get().getSlotId(), conn) + "]. Duplicate entry is not allowed!");
            }

            // Find first available parking slot with pessimistic lock
            Optional<ParkingSlot> availableSlotOpt = slotDAO.findAvailableSlotForUpdate(conn);
            if (availableSlotOpt.isEmpty()) {
                conn.rollback();
                throw new IllegalStateException("Parking Full! There are no available parking slots at this moment.");
            }
            ParkingSlot assignedSlot = availableSlotOpt.get();

            // Mark slot as OCCUPIED
            slotDAO.updateStatus(assignedSlot.getSlotId(), SlotStatus.OCCUPIED, conn);

            // Record parking session
            LocalDateTime entryTime = LocalDateTime.now();
            ParkingSession session = new ParkingSession(vehicle.getVehicleId(), assignedSlot.getSlotId(), entryTime, SessionStatus.ACTIVE);
            sessionDAO.insert(session, conn);

            // Commit transaction
            conn.commit();

            String ownerName = (vehicle.getOwner() != null) ? vehicle.getOwner().getFullName() : "N/A";
            String collegeId = (vehicle.getOwner() != null) ? vehicle.getOwner().getCollegeId() : "N/A";
            UserType userType = (vehicle.getOwner() != null) ? vehicle.getOwner().getUserType() : UserType.STUDENT;

            return new EntryResult(assignedSlot.getSlotNumber(), vehicle.getRegistrationNumber(), ownerName, collegeId, userType, entryTime);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Processes vehicle exit using an ACID database transaction.
     * 1. Finds active parking session for the vehicle.
     * 2. Sets exit timestamp and marks session as COMPLETED.
     * 3. Sets the allocated slot status back to AVAILABLE.
     * 4. Commits transaction; on any error, automatically rolls back.
     *
     * @param rawRegistrationNumber vehicle registration number
     * @return ExitResult containing exit duration and freed slot
     * @throws IllegalArgumentException if vehicle has no active parking session
     * @throws SQLException on database error
     */
    public ExitResult processVehicleExit(String rawRegistrationNumber) throws SQLException {
        String regNo = ValidationUtil.cleanRegistrationNumber(rawRegistrationNumber);
        if (regNo.isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }

        // Check if there is an active session
        Optional<ParkingSession> activeSessionOpt = sessionDAO.findActiveSessionByRegistration(regNo);
        if (activeSessionOpt.isEmpty()) {
            throw new IllegalArgumentException("No currently parked vehicle found with registration number: " + regNo);
        }
        ParkingSession session = activeSessionOpt.get();

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            LocalDateTime exitTime = LocalDateTime.now();

            // 1. Complete the parking session
            sessionDAO.completeSession(session.getSessionId(), exitTime, conn);

            // 2. Free the parking slot
            slotDAO.updateStatus(session.getSlotId(), SlotStatus.AVAILABLE, conn);

            // 3. Commit Transaction
            conn.commit();

            String slotNumber = (session.getSlot() != null) ? session.getSlot().getSlotNumber() : "N/A";
            String duration = DateTimeUtil.formatDuration(session.getEntryTime(), exitTime);
            String ownerName = "Registered Member";

            // Attempt to get owner name
            Optional<Vehicle> vOpt = vehicleDAO.findByRegistrationNumber(regNo);
            if (vOpt.isPresent() && vOpt.get().getOwner() != null) {
                ownerName = vOpt.get().getOwner().getFullName();
            }

            return new ExitResult(slotNumber, regNo, ownerName, session.getEntryTime(), exitTime, duration);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Loads aggregated dashboard metrics from the database.
     */
    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        stats.setTotalSlots(slotDAO.countAll());
        stats.setAvailableSlots(slotDAO.countByStatus(SlotStatus.AVAILABLE));
        stats.setOccupiedSlots(slotDAO.countByStatus(SlotStatus.OCCUPIED));
        stats.setMaintenanceSlots(slotDAO.countByStatus(SlotStatus.MAINTENANCE));
        stats.setCurrentlyParkedCount(sessionDAO.countActiveSessions());
        stats.setTotalSessionsCount(sessionDAO.countTotalSessions());
        stats.setTodaySessionsCount(sessionDAO.countTodaySessions());
        return stats;
    }

    /**
     * Retrieves all parking slots with occupant details for real-time visualization.
     */
    public List<ParkingSlot> getAllSlotsWithOccupants() throws SQLException {
        return slotDAO.findAllWithCurrentOccupant();
    }

    /**
     * Adds a new parking slot (e.g. C01, C02).
     */
    public void addNewSlot(String slotNumber) throws SQLException {
        if (ValidationUtil.isBlank(slotNumber)) {
            throw new IllegalArgumentException("Slot number cannot be empty.");
        }
        String cleanNumber = slotNumber.trim().toUpperCase();
        if (cleanNumber.length() < 2 || cleanNumber.length() > 10) {
            throw new IllegalArgumentException("Slot number must be between 2 and 10 characters (e.g. C01).");
        }
        if (slotDAO.existsBySlotNumber(cleanNumber)) {
            throw new IllegalArgumentException("Slot number [" + cleanNumber + "] already exists.");
        }

        ParkingSlot slot = new ParkingSlot(cleanNumber, SlotStatus.AVAILABLE);
        slotDAO.insert(slot);
    }

    /**
     * Updates slot status safely:
     * Disallows setting an occupied slot into maintenance or available while a car is inside.
     */
    public void updateSlotStatusSafely(int slotId, SlotStatus newStatus) throws SQLException {
        Optional<ParkingSlot> slotOpt = slotDAO.findById(slotId);
        if (slotOpt.isEmpty()) {
            throw new IllegalArgumentException("Slot not found.");
        }
        ParkingSlot currentSlot = slotOpt.get();

        // Check if slot has an active session
        Optional<ParkingSession> activeSession = sessionDAO.findActiveSessionBySlotId(slotId);
        if (activeSession.isPresent()) {
            throw new IllegalStateException("Slot [" + currentSlot.getSlotNumber() + 
                    "] is currently OCCUPIED by an active session! The parked vehicle must exit before changing status.");
        }

        slotDAO.updateStatus(slotId, newStatus);
    }

    /**
     * Retrieves recent parking activity for dashboard display.
     */
    public List<ParkingActivityView> getRecentActivity(int limit) throws SQLException {
        return sessionDAO.findRecentActivity(limit);
    }

    /**
     * Searches history and activity records with filters.
     */
    public List<ParkingActivityView> searchHistory(String query, VehicleType vehicleType, String dateStr, SessionStatus status) throws SQLException {
        return sessionDAO.searchHistory(query, vehicleType, dateStr, status);
    }

    /**
     * Vehicle type breakdown map for reports.
     */
    public Map<String, Integer> getVehicleTypeBreakdown() throws SQLException {
        return sessionDAO.getVehicleTypeBreakdown();
    }

    /**
     * Most utilized slots for reports.
     */
    public Map<String, Integer> getMostUtilizedSlots(int limit) throws SQLException {
        return sessionDAO.getMostUtilizedSlots(limit);
    }

    private String getSlotNumberById(int slotId, Connection conn) {
        String sql = "SELECT slot_number FROM parking_slots WHERE slot_id = ?";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException ignored) {}
        return "ID-" + slotId;
    }
}
