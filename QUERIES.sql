-- =====================================================================
-- College Parking Manager - Sample SQL Demonstration Queries
-- For College DBMS Viva and Lab Exam Demonstration
-- Database: college_parking
-- =====================================================================

USE college_parking;

-- =====================================================================
-- 1. BASIC CRUD OPERATIONS
-- =====================================================================

-- 1.1 CREATE (Insert a new member and vehicle)
INSERT INTO users (full_name, college_id, department, phone, user_type)
VALUES ('Vikram Malhotra', 'CS202399', 'Computer Science', '9123456789', 'STUDENT');

INSERT INTO vehicles (user_id, registration_number, vehicle_type)
VALUES (LAST_INSERT_ID(), 'KA-05-ZZ-9999', 'CAR');

-- 1.2 READ (Retrieve user details)
SELECT * FROM users WHERE college_id = 'CS202399';

-- 1.3 UPDATE (Update user phone number)
UPDATE users 
SET phone = '9876543211' 
WHERE college_id = 'CS202399';

-- 1.4 DELETE (Delete a vehicle registration)
-- Notice: Handled via foreign key constraints
DELETE FROM vehicles WHERE registration_number = 'KA-05-ZZ-9999';


-- =====================================================================
-- 2. COMPLEX JOIN QUERIES
-- =====================================================================

-- 2.1 INNER JOIN: Display currently parked vehicles with owner and slot details
SELECT 
    s.session_id,
    v.registration_number,
    u.full_name AS owner_name,
    u.college_id,
    u.department,
    u.user_type,
    v.vehicle_type,
    ps.slot_number,
    s.entry_time
FROM parking_sessions s
INNER JOIN vehicles v ON s.vehicle_id = v.vehicle_id
INNER JOIN users u ON v.user_id = u.user_id
INNER JOIN parking_slots ps ON s.slot_id = ps.slot_id
WHERE s.session_status = 'ACTIVE'
ORDER BY s.entry_time DESC;

-- 2.2 LEFT JOIN: List all parking slots and show which vehicle is currently occupying them (if any)
SELECT 
    ps.slot_id,
    ps.slot_number,
    ps.status,
    COALESCE(v.registration_number, 'None') AS parked_vehicle,
    COALESCE(u.full_name, 'Vacant') AS driver_name,
    s.entry_time
FROM parking_slots ps
LEFT JOIN parking_sessions s ON ps.slot_id = s.slot_id AND s.session_status = 'ACTIVE'
LEFT JOIN vehicles v ON s.vehicle_id = v.vehicle_id
LEFT JOIN users u ON v.user_id = u.user_id
ORDER BY ps.slot_number ASC;


-- =====================================================================
-- 3. AGGREGATE FUNCTIONS & GROUP BY QUERIES
-- =====================================================================

-- 3.1 COUNT: Slot status breakdown (Available vs Occupied vs Maintenance)
SELECT 
    status, 
    COUNT(*) AS total_slots
FROM parking_slots
GROUP BY status;

-- 3.2 GROUP BY with COUNT: Vehicle category breakdown of total parking sessions
SELECT 
    v.vehicle_type,
    COUNT(s.session_id) AS total_visits
FROM vehicles v
LEFT JOIN parking_sessions s ON v.vehicle_id = s.vehicle_id
GROUP BY v.vehicle_type
ORDER BY total_visits DESC;

-- 3.3 GROUP BY with COUNT & HAVING: Find top utilized parking slots with more than 0 visits
SELECT 
    ps.slot_number,
    COUNT(s.session_id) AS times_used
FROM parking_slots ps
JOIN parking_sessions s ON ps.slot_id = s.slot_id
GROUP BY ps.slot_id, ps.slot_number
HAVING times_used >= 1
ORDER BY times_used DESC;

-- 3.4 Aggregate DURATION: Average, Minimum, and Maximum parking duration in minutes
SELECT 
    ROUND(AVG(TIMESTAMPDIFF(MINUTE, entry_time, exit_time)), 1) AS avg_duration_mins,
    MIN(TIMESTAMPDIFF(MINUTE, entry_time, exit_time)) AS min_duration_mins,
    MAX(TIMESTAMPDIFF(MINUTE, entry_time, exit_time)) AS max_duration_mins
FROM parking_sessions
WHERE session_status = 'COMPLETED' AND exit_time IS NOT NULL;


-- =====================================================================
-- 4. CONCURRENCY & TRANSACTIONAL PESSIMISTIC LOCKING QUERY
-- =====================================================================

-- Find the first available parking slot and lock it for update:
-- (Used inside JDBC connection with setAutoCommit(false))
SELECT slot_id, slot_number, status 
FROM parking_slots 
WHERE status = 'AVAILABLE' 
ORDER BY slot_number ASC 
LIMIT 1 
FOR UPDATE;


-- =====================================================================
-- 5. DATE & TIME FILTERING (REPORTS)
-- =====================================================================

-- 5.1 Sessions recorded today
SELECT 
    COUNT(*) AS today_total_entries
FROM parking_sessions 
WHERE DATE(entry_time) = CURDATE();

-- 5.2 Sessions within a specific date range
SELECT 
    s.session_id,
    v.registration_number,
    s.entry_time,
    s.exit_time,
    s.session_status
FROM parking_sessions s
JOIN vehicles v ON s.vehicle_id = v.vehicle_id
WHERE s.entry_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY s.entry_time DESC;
