# CampusConnect

CS157A Database Management Systems — Team 1 Project

An event-based collaboration platform for SJSU students to browse and sign up for campus events, manage their profile and skills, and connect with other attendees.

**Team 1**
- Bhoomika Gupta (Team Lead) — bhoomika.gupta@sjsu.edu
- Frank Lin — frank.lin02@sjsu.edu
- Brandon Phan — brandon.phan@sjsu.edu

This build uses **jakarta.servlet** and is compatible with **Apache Tomcat 10/11**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, JavaScript |
| Backend | Java Servlets + JSP (Jakarta Servlet 6.0, JSP 3.1) |
| Web/App Server | Apache Tomcat 10 or 11 |
| Database | MySQL Community Server 8.x |
| Build Tool | Maven |
| Password Hashing | jBCrypt |

---

## What's Implemented So Far

This build completes five proposal requirements:

1. **FR1 — User Authentication** — student registration, SJSU-email validation, BCrypt password hashing, login/logout, inactive-account check, 30-minute sessions.
2. **FR2 — Student Profile Management** — edit name, major, graduation year, bio, and skills.
3. **FR4 — Event Discovery** — browse and filter by keyword, category, exact date, and location; organizer and remaining capacity are shown.
4. **FR5 — Event Signup + Waitlist** — signup, duplicate prevention, cancellation, automatic waitlist promotion, position renumbering, and complete signup history.
5. **FR7 — Skill-Based Search** — browse students and filter public profiles by skill.

Everything else (connections, activity logs, admin tools, notifications) is planned for upcoming milestones.

---
## Project Structure

```text
CampusConnect/
├── src/main/java/
│   ├── db/
│   │   └── DBConnection.java       # shared DB connection helper (reads credentials from env vars)
│   └── servlets/
│       ├── LoginServlet.java
│       ├── LogoutServlet.java
│       ├── RegisterServlet.java
│       ├── SignupServlet.java      # event signup + cancel + waitlist logic
│       ├── ProfileServlet.java     # FR2
│       └── SkillSearchServlet.java # FR7
├── src/main/webapp/
│   ├── index.jsp                   # public homepage — upcoming-event banners only
│   ├── events.jsp                  # full event listing, search/filter, signup (login required)
│   ├── login.jsp
│   ├── register.jsp
│   ├── profile.jsp
│   ├── my-events.jsp
│   ├── people.jsp                  # skill-based student search
│   └── WEB-INF/web.xml
├── schema.sql                      # run this in MySQL Workbench first
├── pom.xml
└── README.md
```

---

## Prerequisites

- JDK 17 or newer (`java -version`)
- Maven 3.9+ (`mvn -version`)
- MySQL Community Server 8+
- Apache Tomcat 10 or 11

---

## Database Setup

1. Open **MySQL Workbench**.
2. Open `schema.sql` and run the entire script. This creates the `campus_connect` database and all tables, and seeds demo data.
3. Set your database credentials as **environment variables** — do not put passwords in Git.

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
2. Delete any old `CampusConnect.war` and old exploded `CampusConnect/` directory from Tomcat's `webapps` folder.
3. Copy `target/CampusConnect.war` into `webapps`.
4. Start Tomcat.
5. Open `http://localhost:8080/CampusConnect/`.

---

## Demo Accounts

All demo passwords are **`Password123!`**

| Email | Role |
|---|---|
| frank.lin02@sjsu.edu  | Student |
| brandon.phan@sjsu.edu | Student |
| organizer@sjsu.edu | Event Organizer |
| admin@sjsu.edu | Administrator |

The current five-requirement UI focuses on student workflows. Organizer and administrator dashboards remain future work.

You can also register a new student account directly from the site (`register.jsp`) — must use an `@sjsu.edu` email and a password of at least 8 characters.

---

## Suggested Demo Flow

Register a new account → log in → update profile and skills → filter events by category/date/location → join an open event → join a full event to demonstrate waitlisting → open My Events → cancel a registration → search students by skill.

