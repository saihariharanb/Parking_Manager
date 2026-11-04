package com.college.parking.service;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.dao.ParkingSessionDAO;
import com.college.parking.dao.UserDAO;
import com.college.parking.dao.VehicleDAO;
import com.college.parking.model.User;
import com.college.parking.model.UserType;
import com.college.parking.model.Vehicle;
import com.college.parking.model.VehicleType;
import com.college.parking.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing user and vehicle registrations.
 * Coordinates UserDAO and VehicleDAO within transactions.
 */
public class RegistrationService {

    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;
    private final ParkingSessionDAO sessionDAO;

    public RegistrationService() {
        this.userDAO = new UserDAO();
        this.vehicleDAO = new VehicleDAO();
        this.sessionDAO = new ParkingSessionDAO();
    }

    public RegistrationService(UserDAO userDAO, VehicleDAO vehicleDAO, ParkingSessionDAO sessionDAO) {
        this.userDAO = userDAO;
        this.vehicleDAO = vehicleDAO;
        this.sessionDAO = sessionDAO;
    }

    /**
     * Registers a new user and their vehicle in a single database transaction.
     *
     * @param fullName full name of member
     * @param collegeId student/staff ID
     * @param department academic department
     * @param phone 10-digit phone number
     * @param userType Student or Staff
     * @param regNo vehicle registration plate number
     * @param vehicleType Bike, Scooter, or Car
     * @return the newly created Vehicle object populated with User details
     * @throws IllegalArgumentException on validation failure or duplicates
     * @throws SQLException on database error
     */
    public Vehicle registerNewVehicle(String fullName, String collegeId, String department, 
                                      String phone, UserType userType, String regNo, VehicleType vehicleType) throws SQLException {
        // 1. Validation
        if (!ValidationUtil.isValidName(fullName)) {
            throw new IllegalArgumentException("Full name must be at least 2 characters long.");
        }
        if (!ValidationUtil.isValidCollegeId(collegeId)) {
            throw new IllegalArgumentException("College ID is required (3-20 alphanumeric characters).");
        }
        if (ValidationUtil.isBlank(department)) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone number must be a valid 10-digit number.");
        }
        if (!ValidationUtil.isValidRegistrationNumber(regNo)) {
            throw new IllegalArgumentException("Vehicle registration number is invalid (e.g. KA-01-AB-1234).");
        }

        String cleanReg = ValidationUtil.cleanRegistrationNumber(regNo);
        String cleanCollegeId = collegeId.trim().toUpperCase();

        // 2. Check for duplicate College ID
        if (userDAO.existsByCollegeId(cleanCollegeId, 0)) {
            throw new IllegalArgumentException("A member with College ID [" + cleanCollegeId + "] is already registered.");
        }

        // 3. Check for duplicate Vehicle Registration Number
        if (vehicleDAO.existsByRegistrationNumber(cleanReg, 0)) {
            throw new IllegalArgumentException("Vehicle with Registration Number [" + cleanReg + "] is already registered.");
        }

        // 4. Perform Transactional Insertion
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // Insert User
            User user = new User(fullName.trim(), cleanCollegeId, department.trim(), phone.trim(), userType);
            int userId = userDAO.insert(user, conn);

            // Insert Vehicle
            Vehicle vehicle = new Vehicle(userId, cleanReg, vehicleType);
            int vehicleId = vehicleDAO.insert(vehicle, conn);

            conn.commit(); // Commit Transaction

            vehicle.setOwner(user);
            return vehicle;

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
     * Updates an existing registered vehicle and owner info.
     */
    public void updateRegistration(int vehicleId, int userId, String fullName, String collegeId,
                                   String department, String phone, UserType userType,
                                   String regNo, VehicleType vehicleType) throws SQLException {
        if (!ValidationUtil.isValidName(fullName)) {
            throw new IllegalArgumentException("Full name must be at least 2 characters long.");
        }
        if (!ValidationUtil.isValidCollegeId(collegeId)) {
            throw new IllegalArgumentException("College ID is required.");
        }
        if (ValidationUtil.isBlank(department)) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone number must be a valid 10-digit number.");
        }
        if (!ValidationUtil.isValidRegistrationNumber(regNo)) {
            throw new IllegalArgumentException("Vehicle registration number is invalid.");
        }

        String cleanReg = ValidationUtil.cleanRegistrationNumber(regNo);
        String cleanCollegeId = collegeId.trim().toUpperCase();

        // Check for duplicates excluding current records
        if (userDAO.existsByCollegeId(cleanCollegeId, userId)) {
            throw new IllegalArgumentException("College ID [" + cleanCollegeId + "] belongs to another registered member.");
        }
        if (vehicleDAO.existsByRegistrationNumber(cleanReg, vehicleId)) {
            throw new IllegalArgumentException("Registration Number [" + cleanReg + "] belongs to another vehicle.");
        }

        // Update User
        User user = new User(userId, fullName.trim(), cleanCollegeId, department.trim(), phone.trim(), userType, null);
        userDAO.update(user);

        // Update Vehicle
        Vehicle vehicle = new Vehicle(vehicleId, userId, cleanReg, vehicleType, null);
        vehicleDAO.update(vehicle);
    }

    /**
     * Deletes a vehicle registration.
     * If the vehicle is currently parked, deletion is rejected.
     */
    public void deleteRegistration(int vehicleId, int userId) throws SQLException {
        // Disallow deletion if vehicle is currently inside parking lot
        Optional<Vehicle> vOpt = vehicleDAO.findAllWithOwners().stream().filter(v -> v.getVehicleId() == vehicleId).findFirst();
        if (vOpt.isPresent()) {
            if (sessionDAO.findActiveSessionByRegistration(vOpt.get().getRegistrationNumber()).isPresent()) {
                throw new IllegalStateException("Cannot delete vehicle [" + vOpt.get().getRegistrationNumber() + 
                        "] because it is CURRENTLY PARKED inside! It must exit first.");
            }
        }

        // Deleting user cascades to vehicle
        userDAO.delete(userId);
    }

    /**
     * Retrieves all registered vehicles with owner details.
     */
    public List<Vehicle> getAllRegisteredVehicles() throws SQLException {
        return vehicleDAO.findAllWithOwners();
    }

    /**
     * Searches registered vehicles by search keyword.
     */
    public List<Vehicle> searchRegisteredVehicles(String query) throws SQLException {
        return vehicleDAO.searchVehicles(query);
    }
}
