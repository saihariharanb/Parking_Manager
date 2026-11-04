package com.college.parking.app;

import com.college.parking.config.DatabaseConfig;
import com.college.parking.ui.LoginFrame;
import com.college.parking.ui.theme.UITheme;

import javax.swing.*;

/**
 * Main application entrypoint for College Parking Manager.
 * Initializes modern UI theme, validates database connectivity,
 * and launches the Administrator Login Screen.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize modern Look & Feel
        UITheme.initializeTheme();

        // 2. Test database connectivity asynchronously / log status
        System.out.println("=================================================");
        System.out.println("   Starting College Parking Manager (v1.0.0)");
        System.out.println("=================================================");
        System.out.println("Connecting to MySQL Database at: " + DatabaseConfig.getUrl());

        boolean dbOk = DatabaseConfig.testConnection();
        if (dbOk) {
            System.out.println("✓ MySQL Database connection established successfully!");
        } else {
            System.err.println("⚠ Warning: Could not connect to MySQL database.");
            System.err.println("  Please ensure MySQL Server is running and check 'application.properties'.");
        }

        // 3. Launch UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
