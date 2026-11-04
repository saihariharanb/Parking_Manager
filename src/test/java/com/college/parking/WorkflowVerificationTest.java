package com.college.parking;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.model.*;
import com.college.parking.service.AuthenticationService;
import com.college.parking.service.ParkingService;
import com.college.parking.service.RegistrationService;

import java.util.List;
import java.util.UUID;

/**
 * Headless test to rigorously verify all database workflows:
 * 1. Database connection
 * 2. Admin authentication (SHA-256 hash)
 * 3. Student & Vehicle registration
 * 4. Duplicate checks (College ID & Plate)
 * 5. Transactional Entry & Automatic Slot allocation
 * 6. Duplicate Entry prevention
 * 7. Transactional Exit & Slot release
 * 8. Dashboard statistics & Reports
 */
public class WorkflowVerificationTest {

    public static void main(String[] args) {
        System.out.println(">>> STARTING PARKING MANAGER WORKFLOW VERIFICATION TEST <<<\n");

        try {
            // 1. Test Database Connectivity
            System.out.print("[1/8] Testing MySQL Database Connection... ");
            if (!DatabaseConfig.testConnection()) {
                throw new RuntimeException("FAILED: Database connection failed!");
            }
            System.out.println("PASSED ✓");

            // 2. Test Admin Login
            System.out.print("[2/8] Testing Admin Login (admin / admin123)... ");
            AuthenticationService authService = new AuthenticationService();
            boolean loginSuccess = authService.login("admin", "admin123");
            boolean wrongPasswordRejected = !authService.login("admin", "wrong_pass");
            if (!loginSuccess || !wrongPasswordRejected) {
                throw new RuntimeException("FAILED: Admin authentication logic failed!");
            }
            System.out.println("PASSED ✓");

            // 3. Test Member & Vehicle Registration
            System.out.print("[3/8] Testing Student & Vehicle Registration... ");
            RegistrationService regService = new RegistrationService();
            String uniqueId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String testCollegeId = "TST" + uniqueId;
            String testPlate = "KA-05-TEST-" + uniqueId;

            Vehicle v = regService.registerNewVehicle(
                "Test Student", testCollegeId, "Computer Science", "9876500000",
                UserType.STUDENT, testPlate, VehicleType.CAR
            );
            if (v.getVehicleId() <= 0 || !v.getRegistrationNumber().equalsIgnoreCase(testPlate)) {
                throw new RuntimeException("FAILED: Registration failed!");
            }
            System.out.println("PASSED ✓ (Registered: " + testPlate + ")");

            // 4. Test Duplicate Prevention
            System.out.print("[4/8] Testing Duplicate College ID & Plate Prevention... ");
            try {
                regService.registerNewVehicle("Another Person", testCollegeId, "Civil", "9999900000", UserType.STUDENT, "KA-01-OTHER", VehicleType.BIKE);
                throw new RuntimeException("FAILED: System allowed duplicate College ID!");
            } catch (IllegalArgumentException expected) {
                // Expected
            }
            try {
                regService.registerNewVehicle("Different Person", "NEW" + uniqueId, "Civil", "9999900000", UserType.STUDENT, testPlate, VehicleType.BIKE);
                throw new RuntimeException("FAILED: System allowed duplicate Vehicle Registration!");
            } catch (IllegalArgumentException expected) {
                // Expected
            }
            System.out.println("PASSED ✓ (Duplicate checks enforced)");

            // 5. Test Transactional Vehicle Entry & Auto Slot Allocation
            System.out.print("[5/8] Testing Vehicle Entry & Automatic Slot Assignment... ");
            ParkingService parkingService = new ParkingService();
            ParkingService.EntryResult entryResult = parkingService.processVehicleEntry(testPlate);
            if (entryResult.getSlotNumber() == null || entryResult.getSlotNumber().isEmpty()) {
                throw new RuntimeException("FAILED: No slot assigned on entry!");
            }
            System.out.println("PASSED ✓ (Assigned Slot: " + entryResult.getSlotNumber() + ")");

            // 6. Test Double Entry Prevention
            System.out.print("[6/8] Testing Double Entry Prevention... ");
            try {
                parkingService.processVehicleEntry(testPlate);
                throw new RuntimeException("FAILED: Vehicle entered twice without exit!");
            } catch (IllegalArgumentException expected) {
                // Expected
            }
            System.out.println("PASSED ✓ (Double entry blocked)");

            // 7. Test Transactional Vehicle Exit & Slot Release
            System.out.print("[7/8] Testing Vehicle Exit & Slot Release... ");
            ParkingService.ExitResult exitResult = parkingService.processVehicleExit(testPlate);
            if (!exitResult.getSlotNumber().equals(entryResult.getSlotNumber())) {
                throw new RuntimeException("FAILED: Released slot does not match assigned slot!");
            }
            System.out.println("PASSED ✓ (Freed Slot: " + exitResult.getSlotNumber() + ", Duration: " + exitResult.getDurationFormatted() + ")");

            // 8. Test Dashboard Stats & History Search
            System.out.print("[8/8] Testing Dashboard Stats & History Search... ");
            DashboardStats stats = parkingService.getDashboardStats();
            List<ParkingActivityView> history = parkingService.searchHistory(testPlate, null, null, null);
            if (stats.getTotalSlots() <= 0 || history.isEmpty()) {
                throw new RuntimeException("FAILED: Stats or History query returned empty!");
            }
            System.out.println("PASSED ✓ (History recorded, Total Slots: " + stats.getTotalSlots() + ", Sessions: " + stats.getTotalSessionsCount() + ")");

            System.out.println("\n=======================================================");
            System.out.println(">>> ALL 8 VERIFICATION TESTS PASSED SUCCESSFULLY! <<<");
            System.out.println("=======================================================");

        } catch (Exception e) {
            System.err.println("\nTEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
