-- ------------------------------------
-- FR6 - Connection Requests 
-- ------------------------------------
-- i put the Sends/receives/ Context relationship tables, in the entity table. 
-- even though the ERD shows them seperately. Just like how SignsUpFor/HasSignup become columns on Signups 
-- Enum means that the column can only ever hold pending, accepted, or declined 
-- not null for Sender_ID and Receiver_ID, insert should fail if either is misssing, a connection with no sender makes no sense 
--  a connection may be associated with an event, so if doesnt have to be 
-- purpose is for finding a teammate, networking, carpooling, skill match 
-- UNIQUE (Sender_ID, Receiver_ID, Event_ID) means that that basically Frank cant send Brandon a request for event 3 twice, the combination has to be unique 
-- FOREIGN KEY (Sender_ID) References Students(User_ID) On delete cascade,  this column must match an existing USER_ID in Students 
-- On delete cascade ; if the students row gets deleted then delete there connections also 
USE campus_connect;
 
Drop table if exists Messages;
Drop table if exists Notifications;
Drop table if exists Connections;
CREATE TABLE Connections (
	Connection_ID INT AUTO_INCREMENT PRIMARY KEY, 
    Sender_ID 	INT NOT NULL, 
    Receiver_ID INT NOT NULL, 
    Event_ID 	INT,
    Status      Enum('pending', 'accepted', 'declined') NOT NULL DEFAULT 'pending', 
    Message     VARCHAR(500), 
    Purpose 	VARCHAR(50) NOT NULL, 
    Created_At 	Timestamp DEFAULT current_timestamp, 
    UNIQUE (Sender_ID, Receiver_ID, Event_ID), 
    FOREIGN KEY (Sender_ID) References Students(User_ID) On delete cascade, 
    FOREIGN KEY (Receiver_ID) References Students(User_ID) On delete cascade, 
    FOREIGN KEY (Event_ID) References Events(Event_ID) On delete cascade	
    );
    
-- the index is a second copy of just those columns, and will always run on a query shaped liked: 
-- Select... From Connections Where Receiver ID = ?  and Status. = 'pending' 
-- without and index MySQL will read every row. The index will keep a sorted copy of these two columns, so all of one receivers rows sit together allowing mySQL to jump to them 
CREATE INDEX idx_incoming ON Connections(Receiver_ID, Status); 


-- ------------------------------------
-- FR8 - Messages (bonus) 
-- ------------------------------------
-- A message belongs to a connection, so the foreign key itself enforces
-- that "only connected students can message one another 

CREATE TABLE Messages(
	Message_ID INT AUTO_INCREMENT PRIMARY KEY, 
    Connection_ID INT NOT NULL, 
    Sender_ID INT NOT NULL, 
    Body      VARCHAR(1000) NOT NULL, 
    Sent_At		Timestamp DEFAULT current_timestamp, 
    FOREIGN KEY (Connection_ID) REFERENCES Connections (Connection_ID) On delete cascade, 
    FOREIGN KEY (Sender_ID) REFERENCES Students(User_ID) On delete cascade );
    
-- Index for loading one message thread in order:
--   Select ... From Messages Where Connection_ID = ? Order by Sent_At
-- Connection_ID first narrows to the one conversation; Sent_At second means
-- the rows come back already in time order, so MySQL skips the sort step.
Create index idx_thread On Messages (Connection_ID, Sent_At);


-- ------------------------------------
-- FR9 - Notifications (bonus) 
-- ------------------------------------  
-- important is that it should reference Users instead of Students because organizers will get notified also

Create table Notifications (
  Notification_ID  Int Auto_increment Primary key,
  User_ID          Int Not null,
  Message          Varchar(255) Not null,
  Type             Varchar(50) Not null,
  Is_Read          Boolean Not null Default False,
  Created_At       Timestamp Default Current_timestamp,
  Foreign key (User_ID) References Users(User_ID) On delete cascade
);
 
-- Index for the unread count shown in the nav bar on every page:
--   Select Count(*) From Notifications Where User_ID = ? And Is_Read = False
-- This runs on every page load, so it is the query most worth indexing.
-- Important trade-off: an index makes reads faster but writes slightly slower,
-- since every Insert will have to update the index as well. Worth it ig because we
-- read notifications far more often than we create them.
Create index idx_unread On Notifications (User_ID, Is_Read);



-- ---------------------------------------------------------------
-- Demo rows so the JSPs have something to show before the servlets exist
-- User 1 = Brandon (student), User 2 = Frank (student)
-- ---------------------------------------------------------------
 
Insert into Connections (Sender_ID, Receiver_ID, Event_ID, Status, Message, Purpose)
Values
  (1, 2, 3, 'pending',  'Want to team up for SpartaHack?', 'Find a Teammate'),
  (2, 1, 4, 'accepted', 'Saw you are going to the networking night.', 'Networking');
 
Insert into Messages (Connection_ID, Sender_ID, Body)
Values
  (2, 2, 'Hey Brandon, are you heading to the ballroom early?'),
  (2, 1, 'Yeah, I will be there around 5:30.');
 
Insert into Notifications (User_ID, Message, Type)
Values
  (2, 'You received a new connection request.', 'Connection_Received'),
  (1, 'Your connection request was accepted.',  'Connection_Accepted'),
  (1, 'Your signup for Tech Networking Night is confirmed.', 'Signup_Confirmed');
    
    
-- ---------------------------------------------------------------
-- Verify
-- ---------------------------------------------------------------

Select Connection_ID, Sender_ID, Receiver_ID, Event_ID, Status, Purpose
From Connections
Order by Created_At;
 
Select Message_ID, Connection_ID, Sender_ID, Body, Sent_At
From Messages
Order by Connection_ID, Sent_At;
 
Select User_ID, Count(*) As Unread
From Notifications
Where Is_Read = False
Group by User_ID;
    
    