-- ============================================================
--  BusGo Express — Complete MySQL Database Schema
--  Run this file in MySQL Workbench or mysql CLI:
--  mysql -u root -p < busgo_schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS busgo;
USE busgo;

-- ─── 1. ADMINS ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS admins (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- BCrypt hash
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── 2. USERS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(15)  NOT NULL,
    password        VARCHAR(255) NOT NULL,      -- BCrypt hash
    gender          ENUM('Male','Female','Other') DEFAULT 'Male',
    dob             DATE,
    emergency_contact VARCHAR(15),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active       BOOLEAN DEFAULT TRUE
);

-- ─── 3. BUSES ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS buses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    bus_number  VARCHAR(20)  NOT NULL UNIQUE,
    bus_name    VARCHAR(100) NOT NULL,
    bus_type    ENUM('AC Seater','Non-AC Seater','AC Sleeper','Non-AC Sleeper') NOT NULL,
    capacity    INT          NOT NULL DEFAULT 40,
    status      ENUM('Active','Maintenance','Retired') DEFAULT 'Active',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── 4. ROUTES ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS routes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    source          VARCHAR(100) NOT NULL,
    destination     VARCHAR(100) NOT NULL,
    distance_km     DECIMAL(8,2),
    duration_hrs    DECIMAL(5,2),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_route (source, destination)
);

-- ─── 5. SCHEDULES ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS schedules (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    bus_id          INT NOT NULL,
    route_id        INT NOT NULL,
    departure_time  TIME NOT NULL,
    arrival_time    TIME NOT NULL,
    run_days        VARCHAR(50) DEFAULT 'Daily',   -- Daily / Mon,Wed,Fri etc.
    price           DECIMAL(10,2) NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bus_id)   REFERENCES buses(id)  ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);

-- ─── 6. SEAT LAYOUTS ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS seat_layouts (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    bus_id      INT NOT NULL,
    seat_number VARCHAR(5) NOT NULL,            -- e.g. A1, B3
    seat_type   ENUM('Standard','Window','Elder','Disabled') DEFAULT 'Standard',
    FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE,
    UNIQUE KEY uq_seat (bus_id, seat_number)
);

-- ─── 7. BOOKINGS ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    booking_ref     VARCHAR(20) NOT NULL UNIQUE,  -- BK-00001
    user_id         INT NOT NULL,
    schedule_id     INT NOT NULL,
    journey_date    DATE NOT NULL,
    num_passengers  INT NOT NULL DEFAULT 1,
    subtotal        DECIMAL(10,2) NOT NULL,
    gst_amount      DECIMAL(10,2) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('Card','UPI','Net Banking','Cash') DEFAULT 'Card',
    status          ENUM('Confirmed','Pending','Cancelled','Completed') DEFAULT 'Confirmed',
    booked_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES users(id)     ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

-- ─── 8. PASSENGERS ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS passengers (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    booking_id  INT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    age         INT NOT NULL,
    gender      ENUM('Male','Female','Other') NOT NULL,
    seat_number VARCHAR(5) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- ─── 9. BOOKING CANCELLATIONS ────────────────────────────────
CREATE TABLE IF NOT EXISTS cancellations (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    booking_id      INT NOT NULL UNIQUE,
    reason          VARCHAR(255),
    refund_amount   DECIMAL(10,2),
    cancelled_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- ============================================================
--  SEED DATA
-- ============================================================

-- Admin (password: admin123)
INSERT IGNORE INTO admins (username, password, full_name, email) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y', 'Super Admin', 'admin@busgo.com');

-- Sample buses
INSERT IGNORE INTO buses (bus_number, bus_name, bus_type, capacity, status) VALUES
('KA01-BUS-001', 'Volvo Multi-Axle', 'AC Seater',  40, 'Active'),
('MH03-BUS-042', 'Scania Metrolink', 'AC Sleeper',  36, 'Active'),
('KA07-BUS-019', 'Mercedes Tourismo','AC Seater',  44, 'Maintenance'),
('TN05-BUS-008', 'KPN Travels',      'Non-AC Seater', 50, 'Active'),
('AP02-BUS-015', 'APSRTC Garuda',    'AC Seater',  40, 'Active');

-- Sample routes
INSERT IGNORE INTO routes (source, destination, distance_km, duration_hrs) VALUES
('Mumbai',    'Bangalore',  980,  10.5),
('Delhi',     'Agra',       210,   3.5),
('Bangalore', 'Chennai',    345,   6.0),
('Hyderabad', 'Pune',       560,   9.0),
('Chennai',   'Hyderabad',  625,  10.0),
('Mumbai',    'Pune',       150,   3.0);

-- Sample schedules
INSERT IGNORE INTO schedules (bus_id, route_id, departure_time, arrival_time, run_days, price) VALUES
(1, 1, '10:00:00', '20:30:00', 'Daily', 850.00),
(2, 1, '22:00:00', '08:00:00', 'Daily', 1200.00),
(1, 3, '07:00:00', '13:00:00', 'Daily', 550.00),
(4, 3, '14:00:00', '21:00:00', 'Daily', 380.00),
(5, 4, '08:30:00', '17:30:00', 'Daily', 720.00),
(1, 2, '09:00:00', '12:30:00', 'Mon,Wed,Fri,Sat,Sun', 450.00);

-- Seat layouts for bus 1 (40 seats, A1-J4)
-- Windows: col 1 (A) and col 4 (D); Elder: A1,A4; Disabled: B1,B4
INSERT IGNORE INTO seat_layouts (bus_id, seat_number, seat_type) VALUES
(1,'A1','Elder'),(1,'A2','Standard'),(1,'A3','Standard'),(1,'A4','Elder'),
(1,'B1','Disabled'),(1,'B2','Standard'),(1,'B3','Standard'),(1,'B4','Disabled'),
(1,'C1','Window'),(1,'C2','Standard'),(1,'C3','Standard'),(1,'C4','Window'),
(1,'D1','Window'),(1,'D2','Standard'),(1,'D3','Standard'),(1,'D4','Window'),
(1,'E1','Window'),(1,'E2','Standard'),(1,'E3','Standard'),(1,'E4','Window'),
(1,'F1','Standard'),(1,'F2','Standard'),(1,'F3','Standard'),(1,'F4','Standard'),
(1,'G1','Standard'),(1,'G2','Standard'),(1,'G3','Standard'),(1,'G4','Standard'),
(1,'H1','Standard'),(1,'H2','Standard'),(1,'H3','Standard'),(1,'H4','Standard'),
(1,'I1','Standard'),(1,'I2','Standard'),(1,'I3','Standard'),(1,'I4','Standard'),
(1,'J1','Standard'),(1,'J2','Standard'),(1,'J3','Standard'),(1,'J4','Standard');

-- ============================================================
--  USEFUL VIEWS
-- ============================================================

CREATE OR REPLACE VIEW v_schedule_details AS
SELECT
    s.id            AS schedule_id,
    b.bus_number,
    b.bus_name,
    b.bus_type,
    b.capacity,
    r.source,
    r.destination,
    s.departure_time,
    s.arrival_time,
    s.run_days,
    s.price,
    s.is_active
FROM schedules s
JOIN buses  b ON s.bus_id   = b.id
JOIN routes r ON s.route_id = r.id;

CREATE OR REPLACE VIEW v_booking_details AS
SELECT
    bk.id           AS booking_id,
    bk.booking_ref,
    u.full_name     AS passenger_name,
    u.email,
    u.phone,
    b.bus_name,
    r.source,
    r.destination,
    s.departure_time,
    s.arrival_time,
    bk.journey_date,
    bk.num_passengers,
    bk.total_amount,
    bk.payment_method,
    bk.status,
    bk.booked_at
FROM bookings bk
JOIN users     u ON bk.user_id     = u.id
JOIN schedules s ON bk.schedule_id = s.id
JOIN buses     b ON s.bus_id       = b.id
JOIN routes    r ON s.route_id     = r.id;

CREATE OR REPLACE VIEW v_today_analytics AS
SELECT
    COUNT(*)                                    AS total_bookings,
    SUM(total_amount)                           AS total_revenue,
    SUM(CASE WHEN status='Cancelled' THEN 1 ELSE 0 END) AS cancellations,
    SUM(num_passengers)                         AS total_passengers
FROM bookings
WHERE DATE(booked_at) = CURDATE();
