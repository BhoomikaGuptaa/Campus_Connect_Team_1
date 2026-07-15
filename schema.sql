DROP DATABASE IF EXISTS campus_connect;
CREATE DATABASE campus_connect;
USE campus_connect;

-- This will drop and recreate all tables.
-- Run this to set up a fresh database or reset to seed data.

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Signups;
DROP TABLE IF EXISTS Events;
DROP TABLE IF EXISTS Categories;
DROP TABLE IF EXISTS Administrator;
DROP TABLE IF EXISTS EventOrganizer;
DROP TABLE IF EXISTS Students;
DROP TABLE IF EXISTS Users;

-- Users (ISA superclass) — shared attributes only, per E/R separation method
CREATE TABLE Users (
    User_ID     INT AUTO_INCREMENT PRIMARY KEY,
    First_Name  VARCHAR(50)  NOT NULL,
    Last_Name   VARCHAR(50)  NOT NULL,
    Email       VARCHAR(100) NOT NULL UNIQUE,
    Password    VARCHAR(255) NOT NULL,          -- bcrypt hash, never plain text
    Is_Active   TINYINT(1)   NOT NULL DEFAULT 1,
    Created_At  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Students (ISA subclass) — PK is also FK to Users; student-only attributes live here
CREATE TABLE Students (
    User_ID    INT PRIMARY KEY,
    Major      VARCHAR(100),
    Grad_Year  INT,
    FOREIGN KEY (User_ID) REFERENCES Users(User_ID)
);

-- EventOrganizer (ISA subclass) — no extra attributes beyond what Users already has
CREATE TABLE EventOrganizer (
    User_ID INT PRIMARY KEY,
    FOREIGN KEY (User_ID) REFERENCES Users(User_ID)
);

-- Administrator (ISA subclass) — no extra attributes beyond what Users already has
CREATE TABLE Administrator (
    User_ID INT PRIMARY KEY,
    FOREIGN KEY (User_ID) REFERENCES Users(User_ID)
);

-- Categories
CREATE TABLE Categories (
    Category_ID INT AUTO_INCREMENT PRIMARY KEY,
    Name        VARCHAR(50) NOT NULL UNIQUE
);

-- Events — Organizer_ID references EventOrganizer specifically (only organizers organize events)
CREATE TABLE Events (
    Event_ID      INT AUTO_INCREMENT PRIMARY KEY,
    Organizer_ID  INT NOT NULL,
    Category_ID   INT,
    Title         VARCHAR(150) NOT NULL,
    Description   TEXT,
    Location      VARCHAR(150),
    Event_Date    DATETIME NOT NULL,
    Capacity      INT NOT NULL DEFAULT 50,
    Is_Cancelled  TINYINT(1) NOT NULL DEFAULT 0,
    Created_At    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Organizer_ID) REFERENCES EventOrganizer(User_ID),
    FOREIGN KEY (Category_ID) REFERENCES Categories(Category_ID)
);

-- Signups — Student_ID references Students specifically (only students sign up)
CREATE TABLE Signups (
    Signup_ID          INT AUTO_INCREMENT PRIMARY KEY,
    Student_ID          INT NOT NULL,
    Event_ID             INT NOT NULL,
    Status                VARCHAR(20) NOT NULL DEFAULT 'registered',  -- 'registered' | 'waitlisted' | 'cancelled'
    Waitlist_Position    INT DEFAULT NULL,
    Signed_Up_At         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_signup (Student_ID, Event_ID),
    FOREIGN KEY (Student_ID) REFERENCES Students(User_ID),
    FOREIGN KEY (Event_ID) REFERENCES Events(Event_ID)
);

SET FOREIGN_KEY_CHECKS = 1;

-- Seed data
INSERT INTO Categories (Name) VALUES
('Hackathon'), ('Workshop'), ('Career Fair'),
('Club Meeting'), ('Networking'), ('Social');

-- Demo accounts. Password for ALL THREE below is: Password123!
-- (real bcrypt hash, cost factor 10 — LoginServlet verifies it with BCrypt.checkpw)
INSERT INTO Users (First_Name, Last_Name, Email, Password) VALUES
('Brandon', 'Phan', 'brandon.phan@sjsu.edu', '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),  -- User_ID 1
('SJSU', 'Events', 'organizer@sjsu.edu', '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),      -- User_ID 2
('Admin', 'User', 'admin@sjsu.edu', '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq');           -- User_ID 3

INSERT INTO Students (User_ID, Major, Grad_Year) VALUES
(1, 'Computer Science', 2027);

INSERT INTO EventOrganizer (User_ID) VALUES
(2);

INSERT INTO Administrator (User_ID) VALUES
(3);

INSERT INTO Events (Organizer_ID, Category_ID, Title, Description, Location, Event_Date, Capacity) VALUES
(2, 1, 'SpartaHack 2026', 'SJSU''s annual 24-hour hackathon. Build something awesome!', 'Engineering Building, Rm 285', '2026-07-15 09:00:00', 100),
(2, 3, 'Summer Career Fair', 'Connect with 50+ companies hiring SJSU students for internships and full-time roles.', 'Event Center', '2026-07-22 10:00:00', 300),
(2, 2, 'Intro to Machine Learning', 'Hands-on workshop covering ML fundamentals with Python and scikit-learn.', 'MacQuarrie Hall 225', '2026-07-10 14:00:00', 40),
(2, 5, 'Tech Networking Night', 'Meet fellow CS students and industry professionals over food and drinks.', 'Student Union Ballroom', '2026-07-18 18:00:00', 80),
(2, 4, 'VSA General Meeting', 'Vietnamese Student Association weekly meeting. All welcome!', 'Clark Hall 100', '2026-07-08 17:00:00', 60);

-- Optional seed signups so the homepage can show remaining spots properly
INSERT INTO Signups (Student_ID, Event_ID, Status) VALUES
(1, 1, 'registered'),
(1, 3, 'registered');