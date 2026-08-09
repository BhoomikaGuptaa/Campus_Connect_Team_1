USE campus_connect;

INSERT INTO Users (First_Name, Last_Name, Email, Password) VALUES
-- Students (User_ID 7-13)
('Jordan', 'Kim',       'jordan.kim@sjsu.edu',       '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Maria',  'Chen',      'maria.chen@sjsu.edu',       '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('David',  'Nguyen',    'david.nguyen@sjsu.edu',     '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Priya',  'Patel',     'priya.patel@sjsu.edu',      '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Sam',    'Rodriguez', 'sam.rodriguez@sjsu.edu',    '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Emily',  'Zhao',      'emily.zhao@sjsu.edu',       '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Kevin',  'Tran',      'kevin.tran@sjsu.edu',       '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
-- Event Organizers (User_ID 14-22)
('ACM',      'Club',           'acm.club@sjsu.edu',        '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('WiCS',     'Chapter',        'wics.chapter@sjsu.edu',    '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Career',   'Center',         'career.center@sjsu.edu',   '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('IEEE',     'SJSU',           'ieee.sjsu@sjsu.edu',       '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Spartan',  'Esports',        'spartan.esports@sjsu.edu', '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('VSA',      'Board',          'vsa.board@sjsu.edu',       '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Design',   'Society',        'design.society@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Robotics', 'Club',           'robotics.club@sjsu.edu',   '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Finance',  'Club',           'finance.club@sjsu.edu',    '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
-- Administrators (User_ID 23-31)
('Admin', 'Two',   'admin2@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Three', 'admin3@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Four',  'admin4@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Five',  'admin5@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Six',   'admin6@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Seven', 'admin7@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Eight', 'admin8@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Nine',  'admin9@sjsu.edu',  '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq'),
('Admin', 'Ten',   'admin10@sjsu.edu', '$2a$10$o1A6d45IdvdbikT2Pz1bTeKAjT8t8sKeXFYzLQPYqNQkCzM.y1Usq');

-- ----------------------------------------------------------------
-- 2. Students 
-- ----------------------------------------------------------------
INSERT INTO Students (User_ID, Major, Grad_Year, Bio) VALUES
(7,  'Computer Engineering', 2027, 'Enjoys building hardware/software integration projects.'),
(8,  'Computer Science',     2028, 'Interested in UI/UX and front-end development.'),
(9,  'Business Analytics',   2027, 'Looking for teammates for the career fair prep group.'),
(10, 'Computer Science',     2026, 'Focused on cloud computing and DevOps.'),
(11, 'Software Engineering', 2028, 'Big fan of competitive gaming and esports events.'),
(12, 'Data Science',         2027, 'Wants to find a study group for robotics coursework.'),
(13, 'Computer Science',     2029, 'New transfer student, looking to meet people on campus.');

-- ----------------------------------------------------------------
-- 3. EventOrganizer
-- ----------------------------------------------------------------
INSERT INTO EventOrganizer (User_ID) VALUES
(14), (15), (16), (17), (18), (19), (20), (21), (22);

-- ----------------------------------------------------------------
-- 4. Administrator 
-- ----------------------------------------------------------------
INSERT INTO Administrator (User_ID) VALUES
(23), (24), (25), (26), (27), (28), (29), (30), (31);

-- ----------------------------------------------------------------
-- 5. Categories
-- ----------------------------------------------------------------
INSERT INTO Categories (Name) VALUES
('Sports'),
('Volunteering'),
('Study Group'),
('Cultural');

-- ----------------------------------------------------------------
-- 6. HasSkill 
-- ----------------------------------------------------------------
INSERT INTO HasSkill (User_ID, Skill_ID) VALUES
(7, 1),   -- Jordan - Python
(8, 5);   -- Maria - UI/UX Design

-- ----------------------------------------------------------------
-- 7. Events 
-- ----------------------------------------------------------------
INSERT INTO Events (Organizer_ID, Category_ID, Title, Description, Location, Event_Date, Capacity) VALUES
(14, 2, 'Git & GitHub Workshop',      'Hands-on workshop on version control basics.',                 'MLK Library Room 225',          '2026-08-15 15:00:00', 40),
(15, 4, 'WiCS Weekly Mixer',          'Weekly meetup for Women in Computer Science.',                 'Engineering Building 189',      '2026-08-18 17:00:00', 50),
(16, 3, 'Fall Career Expo',           'Meet recruiters from top Bay Area companies.',                 'Event Center',                  '2026-08-20 10:00:00', 250),
(18, 7, 'Spartan Esports Tournament', 'Campus-wide gaming tournament, all skill levels welcome.',     'Student Union Games Room',      '2026-08-22 12:00:00', 32),
(21, 9, 'Robotics Study Group',       'Weekly study session for robotics coursework.',                'Engineering Building 301',      '2026-08-25 16:00:00', 20);

-- ----------------------------------------------------------------
-- 8. Signups 
-- ----------------------------------------------------------------
INSERT INTO Signups (Student_ID, Event_ID, Status) VALUES
(7,  6,  'registered'),
(8,  7,  'registered'),
(9,  8,  'registered'),
(10, 9,  'registered'),
(11, 10, 'registered'),
(12, 6,  'registered');

-- ----------------------------------------------------------------
-- 9. OrganizerRequests 
-- ----------------------------------------------------------------
INSERT INTO OrganizerRequests (User_ID, Status, Reviewed_At) VALUES
(7,  'Pending',  NULL),
(8,  'Approved', '2026-07-20 10:00:00'),
(9,  'Rejected', '2026-07-21 11:00:00'),
(10, 'Pending',  NULL),
(11, 'Approved', '2026-07-22 09:30:00'),
(12, 'Pending',  NULL),
(13, 'Rejected', '2026-07-23 14:00:00'),
(2,  'Approved', '2026-07-24 13:00:00'),
(1,  'Pending',  NULL);

-- ----------------------------------------------------------------
-- 10. ActivityLogs
-- ----------------------------------------------------------------
INSERT INTO ActivityLogs (Admin_ID, Action, Target_Type, Target_ID) VALUES
(4,  'Approved Organizer Request', 'OrganizerRequest', 2),
(4,  'Rejected Organizer Request', 'OrganizerRequest', 9),
(23, 'Deleted Category',           'Category',         6),
(23, 'Suspended User',             'User',             6),
(24, 'Approved Organizer Request', 'OrganizerRequest', 8),
(24, 'Added Category',             'Category',         10),
(25, 'Rejected Organizer Request', 'OrganizerRequest', 13),
(25, 'Reactivated User',           'User',             6),
(4,  'Cancelled Event',            'Event',            5),
(26, 'Approved Organizer Request', 'OrganizerRequest', 11);

-- ----------------------------------------------------------------
-- 11. Connections 
-- ----------------------------------------------------------------
INSERT INTO Connections (Sender_ID, Receiver_ID, Event_ID, Status, Message, Purpose) VALUES
(5,  7,  6,    'pending',  'Want to team up for the Git workshop?',        'Find a Teammate'),
(7,  5,  NULL, 'accepted', 'Nice meeting you at orientation!',             'Networking'),
(8,  9,  8,    'pending',  'Split an Uber to the career expo?',            'Carpooling'),
(9,  8,  8,    'declined', 'Sorry, already have a ride.',                  'Carpooling'),
(10, 11, 9,    'accepted', 'Looking for a duo partner for the tournament.','Find a Teammate'),
(11, 10, 9,    'pending',  'Are you good with strategy games?',            'Skill Match'),
(12, 13, 10,   'accepted', 'Study buddy for robotics?',                    'Find a Teammate'),
(13, 12, NULL, 'pending',  'Do you know Python well? Need help with HW.',  'Skill Match');

-- ----------------------------------------------------------------
-- 12. Notifications 
-- ----------------------------------------------------------------
INSERT INTO Notifications (User_ID, Message, Type) VALUES
(7,  'Your connection request was declined.',              'Connection_Declined'),
(8,  'You received a new connection request.',              'Connection_Received'),
(9,  'Your signup for Fall Career Expo is confirmed.',       'Signup_Confirmed'),
(10, 'You have been added to the waitlist.',                 'Waitlist_Added'),
(11, 'Your organizer request was approved.',                 'Organizer_Approved'),
(4,  'A new organizer request is pending review.',           'Admin_Alert'),
(23, 'A new organizer request is pending review.',           'Admin_Alert');

