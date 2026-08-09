# Campus Connect

CS157A Database Management Systems — Team 1 Project

Campus Connect is a web-based platform for SJSU students to discover and sign up for campus events, manage profiles and skills, search for other students, and connect with attendees.

## Team 1

- Bhoomika Gupta (Team Lead) — bhoomika.gupta@sjsu.edu
- Frank Lin — frank.lin02@sjsu.edu
- Brandon Phan — brandon.phan@sjsu.edu

---

## Tech Stack

- HTML, CSS, JavaScript
- JSP and Java Servlets
- Apache Tomcat 10/11
- MySQL
- Maven
- BCrypt / jBCrypt

---

## Functional Requirements

1. **FR1 — User Authentication**  
   Registration, login/logout, SJSU email validation, password hashing, and inactive-account checks.

2. **FR2 — Student Profile Management**  
   Students can update their name, major, graduation year, bio, and skills.

3. **FR3 — Event Management**  
   Event Organizers can create, edit, cancel, and view attendees for their own events.

4. **FR4 — Event Discovery**  
   Students can browse and filter events by keyword, category, date, and location.

5. **FR5 — Event Signup and Waitlist**  
   Students can register, join a waitlist when an event is full, cancel, and automatically promote the next waitlisted student.

6. **FR6 — Connection Requests**  
   Students attending the same event can send, accept, or decline connection requests.

7. **FR7 — Skill-Based Search**  
   Students can search for other students by skill and optionally filter by event.

8. **FR8 — Notifications**  
   Users can view notifications and mark them as read.

9. **FR9 — Administrator Functions**  
   Administrators can approve organizer requests, suspend/reactivate users, manage categories, and view activity logs.

---

## Database

The project uses a MySQL database named `campus_connect`.

The final database contains 13 tables:

- Users
- Students
- EventOrganizer
- Administrator
- Categories
- Skills
- HasSkill
- Events
- Signups
- Connections
- OrganizerRequests
- ActivityLogs
- Notifications

---
---

## Project Structure

```text
CampusConnect/
├── src/main/java/
│   ├── db/
│   │   └── DBConnection.java
│   └── servlets/
│       ├── LoginServlet.java
│       ├── LogoutServlet.java
│       ├── RegisterServlet.java
│       ├── ProfileServlet.java
│       ├── EventServlet.java
│       ├── SignupServlet.java
│       ├── MyEventsServlet.java
│       ├── AttendeesServlet.java
│       ├── ConnectionServlet.java
│       ├── SkillSearchServlet.java
│       ├── NotificationServlet.java
│       ├── AdminDashboardServlet.java
│       └── AdminActionServlet.java
├── src/main/webapp/
│   ├── index.jsp
│   ├── login.jsp
│   ├── register.jsp
│   ├── events.jsp
│   ├── my-events.jsp
│   ├── attendees.jsp
│   ├── people.jsp
│   ├── profile.jsp
│   ├── notifications.jsp
│   ├── admin.jsp
│   └── WEB-INF/
│       └── web.xml
├── schema.sql
├── pom.xml
└── README.md
```

---

## Prerequisites

- JDK 17 or newer
- Maven 3.9+
- MySQL Community Server 8+
- MySQL Workbench
- Apache Tomcat 10 or 11

---

## Database Setup

1. Open MySQL Workbench.
2. Open `schema.sql`.
3. Run the entire script.
4. This creates the `campus_connect` database, all 13 tables, and demo data.
5. Set the database credentials used by `DBConnection.java`.

Do not put personal database passwords in GitHub.

---

## Build

From the project root:

```bash
mvn clean package
```

The deployable file will be:

```text
target/CampusConnect.war
```

---

## Deploy to Tomcat

1. Stop Tomcat.
2. Delete any old `CampusConnect.war` and `CampusConnect/` folder from `webapps`.
3. Copy `target/CampusConnect.war` into `webapps`.
4. Start Tomcat.
5. Open:

```text
http://localhost:8080/CampusConnect/
```

---

## Demo Accounts

All demo passwords are:

**`Password123!`**

| Email | Role |
|---|---|
| brandon.phan@sjsu.edu | Student |
| frank.lin02@sjsu.edu | Student |
| bhoomika.gupta@sjsu.edu | Student |
| organizer1@sjsu.edu | Event Organizer |
| admin1@sjsu.edu | Administrator |

A new student can also register using an `@sjsu.edu` email.

---

## Suggested Demo Flow

Student: log in → update profile → browse/filter events → sign up → demonstrate waitlist → cancel signup → search by skill → send connection request → view notifications.

Organizer: log in → create event → edit event → view attendees → cancel event.

Administrator: log in → approve organizer request → suspend/reactivate user → manage categories → view activity logs.

---

## Final Project Note

The final submitted system contains **FR1 through FR9**.

Some earlier FR numbering remain in the GitHub to show work attempted during development. These unfinished features are not part of the final submitted ZIP, database, report, or demo.
