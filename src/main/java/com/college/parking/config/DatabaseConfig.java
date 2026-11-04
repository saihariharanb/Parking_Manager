package com.college.parking.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConfig manages JDBC database connection configuration.
 * Reads connection properties from application.properties to avoid hardcoding credentials.
 * Provides connections to MySQL for all DAO operations.
 */
public class DatabaseConfig {

    private static String url;
    private static String user;
    private static String password;
    private static int timeout = 5;
    private static boolean initialized = false;

    // Static initialization block
    static {
        loadConfiguration();
    }

    /**
     * Loads database configuration properties from the classpath.
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/college_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                user = props.getProperty("db.user", "root");
                password = props.getProperty("db.password", "root");
                String timeoutStr = props.getProperty("db.timeout", "5");
                try {
                    timeout = Integer.parseInt(timeoutStr);
                } catch (NumberFormatException ignored) {}
            } else {
                // Fallback default properties
                url = "jdbc:mysql://localhost:3306/college_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                user = "root";
                password = "root";
            }

            // Register MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.setLoginTimeout(timeout);
            initialized = true;
        } catch (IOException e) {
            System.err.println("Warning: Could not read application.properties, using defaults. Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database Connection from DriverManager.
     * Callers must close the Connection using try-with-resources.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests whether the database connection is currently alive and functional.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(timeout);
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
