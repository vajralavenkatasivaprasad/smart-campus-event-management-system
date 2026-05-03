-- Smart Campus EMS Database Schema
CREATE DATABASE IF NOT EXISTS smart_campus_ems;
USE smart_campus_ems;

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    role ENUM('ADMIN','STUDENT','FACULTY','STAFF') DEFAULT 'STUDENT',
    otp VARCHAR(10),
    otp_expiry DATETIME,
    is_verified BOOLEAN DEFAULT FALSE,
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Venues Table
CREATE TABLE venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    capacity INT NOT NULL,
    location VARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    amenities TEXT,
    image_url VARCHAR(255),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Events Table
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category ENUM('ACADEMIC','CULTURAL','SPORTS','WORKSHOP','SEMINAR','CONFERENCE','SOCIAL','OTHER') DEFAULT 'OTHER',
    venue_id BIGINT,
    organizer_id BIGINT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    registration_deadline DATETIME,
    max_attendees INT,
    current_attendees INT DEFAULT 0,
    status ENUM('DRAFT','PUBLISHED','CANCELLED','COMPLETED') DEFAULT 'DRAFT',
    banner_image VARCHAR(255),
    is_free BOOLEAN DEFAULT TRUE,
    ticket_price DECIMAL(10,2) DEFAULT 0.00,
    location_name VARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE SET NULL,
    FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Event Registrations
CREATE TABLE event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    registration_status ENUM('PENDING','CONFIRMED','CANCELLED','ATTENDED') DEFAULT 'CONFIRMED',
    ticket_number VARCHAR(50) UNIQUE,
    qr_code TEXT,
    payment_status ENUM('PENDING','PAID','REFUNDED') DEFAULT 'PAID',
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attended_at TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_registration (event_id, user_id)
);

-- Announcements
CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    target_role ENUM('ALL','STUDENT','FACULTY','STAFF') DEFAULT 'ALL',
    is_pinned BOOLEAN DEFAULT FALSE,
    expires_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Feedback
CREATE TABLE feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_feedback (event_id, user_id)
);

-- Chat Messages (Chatbot history)
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(100),
    message TEXT NOT NULL,
    response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('EVENT','ANNOUNCEMENT','SYSTEM','REMINDER') DEFAULT 'SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    related_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- OTP Logs
CREATE TABLE otp_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    purpose ENUM('REGISTRATION','LOGIN','PASSWORD_RESET') DEFAULT 'REGISTRATION',
    is_used BOOLEAN DEFAULT FALSE,
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Data
INSERT INTO users (name, email, password, phone, role, is_verified) VALUES
('Admin User', 'admin@campus.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '9876543210', 'ADMIN', TRUE),
('Dr. Ramesh Kumar', 'faculty@campus.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '9876543211', 'FACULTY', TRUE),
('Priya Sharma', 'student@campus.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '9876543212', 'STUDENT', TRUE);
-- Default password: password123

INSERT INTO venues (name, description, capacity, location, latitude, longitude, amenities, is_available) VALUES
('Main Auditorium', 'Large auditorium with stage and AV equipment', 500, 'Block A, Ground Floor', 9.9252, 78.1198, 'AC,Projector,Mic,Stage,Seating', TRUE),
('Seminar Hall 1', 'Air-conditioned seminar hall', 100, 'Block B, 1st Floor', 9.9254, 78.1200, 'AC,Projector,Whiteboard', TRUE),
('Open Air Theatre', 'Outdoor amphitheater', 1000, 'Campus Ground', 9.9248, 78.1195, 'Stage,Lighting,Sound System', TRUE),
('Conference Room A', 'Executive conference room', 30, 'Admin Block, 2nd Floor', 9.9256, 78.1202, 'AC,TV,Video Conference', TRUE),
('Sports Complex', 'Indoor sports facility', 200, 'Sports Block', 9.9244, 78.1190, 'Courts,Equipment,Changing Rooms', TRUE);

INSERT INTO events (title, description, category, venue_id, organizer_id, start_date, end_date, registration_deadline, max_attendees, status, is_free) VALUES
('Annual Tech Fest 2025', 'Biggest technical festival with hackathons, coding contests and robotics', 'ACADEMIC', 1, 1, '2025-06-15 09:00:00', '2025-06-17 18:00:00', '2025-06-10 23:59:59', 500, 'PUBLISHED', TRUE),
('Cultural Night 2025', 'A grand evening of music, dance and drama performances', 'CULTURAL', 3, 2, '2025-06-20 18:00:00', '2025-06-20 22:00:00', '2025-06-18 23:59:59', 800, 'PUBLISHED', TRUE),
('Machine Learning Workshop', 'Hands-on workshop on ML with Python and TensorFlow', 'WORKSHOP', 2, 2, '2025-06-25 10:00:00', '2025-06-25 17:00:00', '2025-06-22 23:59:59', 80, 'PUBLISHED', FALSE),
('Sports Day 2025', 'Annual inter-department sports competition', 'SPORTS', 5, 1, '2025-07-05 08:00:00', '2025-07-05 18:00:00', '2025-07-01 23:59:59', 200, 'PUBLISHED', TRUE),
('Industry Connect Seminar', 'Guest lectures from top industry professionals', 'SEMINAR', 4, 2, '2025-07-10 10:00:00', '2025-07-10 16:00:00', '2025-07-08 23:59:59', 30, 'PUBLISHED', TRUE);

INSERT INTO announcements (title, content, author_id, target_role, is_pinned) VALUES
('Welcome to Smart Campus EMS', 'Welcome to our new Event Management System. Register and explore exciting events!', 1, 'ALL', TRUE),
('Tech Fest Registration Open', 'Registrations for Annual Tech Fest 2025 are now open. Limited seats available!', 1, 'STUDENT', TRUE),
('Faculty Meeting Rescheduled', 'The monthly faculty meeting has been rescheduled to June 5th at 3 PM.', 1, 'FACULTY', FALSE);
