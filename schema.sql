DROP DATABASE IF EXISTS campus_connect;
CREATE DATABASE campus_connect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_connect;

CREATE TABLE Users (
 User_ID INT AUTO_INCREMENT PRIMARY KEY, First_Name VARCHAR(50) NOT NULL, Last_Name VARCHAR(50) NOT NULL,
 Email VARCHAR(100) NOT NULL UNIQUE, Password VARCHAR(255) NOT NULL, Is_Active BOOLEAN NOT NULL DEFAULT TRUE,
 Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE Students (
 User_ID INT PRIMARY KEY, Major VARCHAR(100), Grad_Year INT, Bio VARCHAR(500),
 CONSTRAINT fk_student_user FOREIGN KEY (User_ID) REFERENCES Users(User_ID) ON DELETE CASCADE
);
CREATE TABLE EventOrganizer (User_ID INT PRIMARY KEY, CONSTRAINT fk_org_user FOREIGN KEY (User_ID) REFERENCES Users(User_ID) ON DELETE CASCADE);
CREATE TABLE Administrator (User_ID INT PRIMARY KEY, CONSTRAINT fk_admin_user FOREIGN KEY (User_ID) REFERENCES Users(User_ID) ON DELETE CASCADE);
CREATE TABLE Categories (Category_ID INT AUTO_INCREMENT PRIMARY KEY, Name VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE Skills (Skill_ID INT AUTO_INCREMENT PRIMARY KEY, Name VARCHAR(60) NOT NULL UNIQUE);
CREATE TABLE HasSkill (
 User_ID INT NOT NULL, Skill_ID INT NOT NULL, PRIMARY KEY(User_ID, Skill_ID),
 FOREIGN KEY(User_ID) REFERENCES Students(User_ID) ON DELETE CASCADE,
 FOREIGN KEY(Skill_ID) REFERENCES Skills(Skill_ID) ON DELETE CASCADE
);
CREATE TABLE Events (
 Event_ID INT AUTO_INCREMENT PRIMARY KEY, Organizer_ID INT NOT NULL, Category_ID INT NOT NULL,
 Title VARCHAR(150) NOT NULL, Description TEXT, Location VARCHAR(150) NOT NULL, Event_Date DATETIME NOT NULL,
 Capacity INT NOT NULL CHECK(Capacity > 0), Is_Cancelled BOOLEAN NOT NULL DEFAULT FALSE, Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(Organizer_ID) REFERENCES EventOrganizer(User_ID), FOREIGN KEY(Category_ID) REFERENCES Categories(Category_ID)
);
CREATE TABLE Signups (
 Signup_ID INT AUTO_INCREMENT PRIMARY KEY, Student_ID INT NOT NULL, Event_ID INT NOT NULL,
 Status ENUM('registered','waitlisted','cancelled') NOT NULL DEFAULT 'registered', Waitlist_Position INT NULL,
 Signed_Up_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP, UNIQUE KEY uq_signup(Student_ID, Event_ID),
 FOREIGN KEY(Student_ID) REFERENCES Students(User_ID) ON DELETE CASCADE,
 FOREIGN KEY(Event_ID) REFERENCES Events(Event_ID) ON DELETE CASCADE,
 INDEX idx_event_status(Event_ID, Status), INDEX idx_waitlist(Event_ID, Status, Waitlist_Position)
);
INSERT INTO Categories(Name) VALUES ('Hackathon'),('Workshop'),('Career Fair'),('Club Meeting'),('Networking'),('Social');
INSERT INTO Skills(Name) VALUES ('Python'),('Java'),('Data Analysis'),('Machine Learning'),('UI/UX Design'),('Web Development'),('Cloud Computing'),('Public Speaking'),('Project Management'),('Graphic Design');
-- Password for all demo accounts: Password123!
INSERT INTO Users(First_Name,Last_Name,Email,Password) VALUES
('Brandon','Phan','brandon.phan@sjsu.edu','$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Frank','Lin','frank.lin02@sjsu.edu','$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('SJSU','Events','organizer@sjsu.edu','$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin','User','admin@sjsu.edu','$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq');
INSERT INTO Students(User_ID,Major,Grad_Year,Bio) VALUES
(1,'Computer Science',2027,'Interested in full-stack development and hackathons.'),
(2,'Data Science',2027,'Learning machine learning and looking for project teammates.');
INSERT INTO EventOrganizer VALUES(3); INSERT INTO Administrator VALUES(4);
INSERT INTO HasSkill VALUES (1,2),(1,6),(1,7),(2,1),(2,3),(2,4);
INSERT INTO Events(Organizer_ID,Category_ID,Title,Description,Location,Event_Date,Capacity) VALUES
(3,4,'VSA General Meeting','Vietnamese Student Association weekly meeting. Everyone is welcome!','Clark Hall 100','2026-07-28 17:00:00',60),
(3,2,'Intro to Machine Learning','Hands-on workshop covering ML fundamentals with Python and scikit-learn.','MacQuarrie Hall 225','2026-07-30 14:00:00',2),
(3,1,'SpartaHack 2026','SJSU annual 24-hour hackathon. Build something awesome!','Engineering Building, Room 285','2026-08-05 09:00:00',100),
(3,5,'Tech Networking Night','Meet fellow students and industry professionals over food and drinks.','Student Union Ballroom','2026-08-08 18:00:00',80),
(3,3,'Summer Career Fair','Connect with companies hiring SJSU students for internships and full-time roles.','Event Center','2026-08-12 10:00:00',300);
INSERT INTO Signups(Student_ID,Event_ID,Status) VALUES (1,1,'registered'),(1,4,'registered'),(2,2,'registered');
