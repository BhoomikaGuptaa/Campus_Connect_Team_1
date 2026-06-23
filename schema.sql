USE campusconnect;

-- WARNING: This will drop and recreate all tables.
-- Run this to set up a fresh database or reset to seed data.
DROP TABLE IF EXISTS signups;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'student',
    major VARCHAR(100),
    grad_year INT,
    bio TEXT,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Events table
CREATE TABLE events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    organizer_id INT NOT NULL,
    category_id INT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(150),
    event_date DATETIME NOT NULL,
    capacity INT NOT NULL DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_cancelled TINYINT(1) DEFAULT 0,
    FOREIGN KEY (organizer_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Signups table
CREATE TABLE signups (
    signup_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    event_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'registered',
    waitlist_position INT DEFAULT NULL,
    signed_up_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_signup (student_id, event_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id)
);

-- Seed data
INSERT INTO categories (name) VALUES
('Hackathon'), ('Workshop'), ('Career Fair'),
('Club Meeting'), ('Networking'), ('Social');

INSERT INTO users (email, password_hash, full_name, role, major, grad_year) VALUES
('brandon.phan@sjsu.edu', 'password123', 'Brandon Phan', 'student', 'Computer Science', 2027),
('organizer@sjsu.edu', 'password123', 'SJSU Events', 'organizer', NULL, NULL),
('admin@sjsu.edu', 'password123', 'Admin User', 'admin', NULL, NULL);

INSERT INTO events (organizer_id, category_id, title, description, location, event_date, capacity) VALUES
(2, 1, 'SpartaHack 2026', 'SJSU''s annual 24-hour hackathon. Build something awesome!', 'Engineering Building, Rm 285', '2026-07-15 09:00:00', 100),
(2, 3, 'Summer Career Fair', 'Connect with 50+ companies hiring SJSU students for internships and full-time roles.', 'Event Center', '2026-07-22 10:00:00', 300),
(2, 2, 'Intro to Machine Learning', 'Hands-on workshop covering ML fundamentals with Python and scikit-learn.', 'MacQuarrie Hall 225', '2026-07-10 14:00:00', 40),
(2, 5, 'Tech Networking Night', 'Meet fellow CS students and industry professionals over food and drinks.', 'Student Union Ballroom', '2026-07-18 18:00:00', 80),
(2, 4, 'VSA General Meeting', 'Vietnamese Student Association weekly meeting. All welcome!', 'Clark Hall 100', '2026-07-08 17:00:00', 60);