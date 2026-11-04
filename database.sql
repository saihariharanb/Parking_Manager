-- =====================================================================
-- College Parking Manager - Database Setup Script
-- Database: college_parking
-- Compatible with MySQL 8.0+
-- =====================================================================

-- 1. Create and switch to the database
DROP DATABASE IF EXISTS college_parking;
CREATE DATABASE college_parking
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE college_parking;

-- =====================================================================
-- 2. Create Tables with Constraints, Indexes, and Foreign Keys
-- =====================================================================

-- Table: users (Students and Staff Members)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    college_id VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    user_type ENUM('STUDENT', 'STAFF') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_college_id UNIQUE (college_id)
) ENGINE=InnoDB;

-- Table: vehicles (Vehicles owned by registered users)
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    registration_number VARCHAR(30) NOT NULL,
    vehicle_type ENUM('BIKE', 'SCOOTER', 'CAR') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_vehicles_registration UNIQUE (registration_number),
    CONSTRAINT fk_vehicles_user FOREIGN KEY (user_id) 
        REFERENCES users (user_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table: parking_slots (Individual parking bays)
CREATE TABLE parking_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(20) NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_slots_slot_number UNIQUE (slot_number)
) ENGINE=InnoDB;

-- Table: parking_sessions (Active and Completed vehicle parking records)
CREATE TABLE parking_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    session_status ENUM('ACTIVE', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_sessions_vehicle FOREIGN KEY (vehicle_id) 
        REFERENCES vehicles (vehicle_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_sessions_slot FOREIGN KEY (slot_id) 
        REFERENCES parking_slots (slot_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table: admins (System administrators for login)
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(64) NOT NULL, -- SHA-256 hash (64 hex characters)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_admins_username UNIQUE (username)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. Indexes for High Performance and Integrity
-- =====================================================================
CREATE INDEX idx_users_name ON users (full_name);
CREATE INDEX idx_users_dept ON users (department);
CREATE INDEX idx_vehicles_reg ON vehicles (registration_number);
CREATE INDEX idx_slots_status ON parking_slots (status);
CREATE INDEX idx_sessions_vehicle_status ON parking_sessions (vehicle_id, session_status);
CREATE INDEX idx_sessions_slot_status ON parking_sessions (slot_id, session_status);
CREATE INDEX idx_sessions_entry ON parking_sessions (entry_time);

-- =====================================================================
-- 4. Seed Data: Administrator Account
-- Default credentials:
--   Username: admin
--   Password: admin123
-- SHA-256 for 'admin123' is:
-- 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- =====================================================================
INSERT INTO admins (username, password_hash) VALUES 
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- =====================================================================
-- 5. Seed Data: 20 Sample Parking Slots (Block A: A01-A10, Block B: B01-B10)
-- =====================================================================
INSERT INTO parking_slots (slot_number, status) VALUES
('A01', 'AVAILABLE'),
('A02', 'AVAILABLE'),
('A03', 'AVAILABLE'),
('A04', 'AVAILABLE'),
('A05', 'AVAILABLE'),
('A06', 'AVAILABLE'),
('A07', 'AVAILABLE'),
('A08', 'AVAILABLE'),
('A09', 'AVAILABLE'),
('A10', 'AVAILABLE'),
('B01', 'AVAILABLE'),
('B02', 'AVAILABLE'),
('B03', 'AVAILABLE'),
('B04', 'AVAILABLE'),
('B05', 'AVAILABLE'),
('B06', 'AVAILABLE'),
('B07', 'AVAILABLE'),
('B08', 'AVAILABLE'),
('B09', 'AVAILABLE'),
('B10', 'AVAILABLE');

-- =====================================================================
-- 6. Seed Data: Sample Users & Vehicles for Viva Demonstration
-- =====================================================================
INSERT INTO users (full_name, college_id, department, phone, user_type) VALUES
('Aarav Sharma', 'CS202301', 'Computer Science', '9876543210', 'STUDENT'),
('Priya Patel', 'EC202345', 'Electronics & Comm', '9812345678', 'STUDENT'),
('Dr. Ramesh Rao', 'STF1008', 'Information Science', '9745632180', 'STAFF'),
('Ananya Gupta', 'ME202312', 'Mechanical Engg', '9988776655', 'STUDENT'),
('Prof. Sneha Verma', 'STF1025', 'Civil Engg', '9123456780', 'STAFF');

INSERT INTO vehicles (user_id, registration_number, vehicle_type) VALUES
(1, 'KA-01-AB-1234', 'CAR'),
(2, 'KA-03-XY-9876', 'SCOOTER'),
(3, 'KA-05-MH-5555', 'CAR'),
(4, 'KA-04-ER-3412', 'BIKE'),
(5, 'KA-02-JK-7890', 'CAR');

-- =====================================================================
-- 7. Seed Data: A couple of initial parking sessions
-- Slot A01 occupied by KA-01-AB-1234 (Active)
-- Slot B01 occupied by KA-03-XY-9876 (Active)
-- A completed historical session for testing history & reports
-- =====================================================================
UPDATE parking_slots SET status = 'OCCUPIED' WHERE slot_number IN ('A01', 'B01');

INSERT INTO parking_sessions (vehicle_id, slot_id, entry_time, exit_time, session_status) VALUES
-- Active session 1
(1, (SELECT slot_id FROM parking_slots WHERE slot_number = 'A01'), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, 'ACTIVE'),
-- Active session 2
(2, (SELECT slot_id FROM parking_slots WHERE slot_number = 'B01'), DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, 'ACTIVE'),
-- Completed historical session
(3, (SELECT slot_id FROM parking_slots WHERE slot_number = 'A02'), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 20 HOUR), 'COMPLETED');
