# CampusConnect

CS157A Database Management Systems — Team 1 Project

An event-based collaboration platform for SJSU students to browse and sign up for campus events, and connect with other attendees.

**Team 1**
- Bhoomika Gupta (Team Lead) — bhoomika.gupta@sjsu.edu
- Frank Lin — frank.lin02@sjsu.edu
- Brandon Phan — brandon.phan@sjsu.edu

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, JavaScript |
| Backend | Java Servlets + JSP |
| Web/App Server | Apache Tomcat 11 |
| Database | MySQL Community Server |
| Build Tool | Maven |
| Password Hashing | jBCrypt |

See `requirements.txt` for exact versions.

---

## Project Structure

```
CampusConnect/
├── src/main/java/
│   ├── db/
│   │   └── DBConnection.java       # shared DB connection helper
│   └── servlets/
│       ├── HomeServlet.java
│       ├── LoginServlet.java
│       ├── LogoutServlet.java
│       ├── RegisterServlet.java
│       └── SignupServlet.java      # event signup + cancel + waitlist logic
├── src/main/webapp/
│   ├── index.jsp                   # homepage — event listing, search/filter, signup actions
│   ├── login.jsp
│   ├── register.jsp
│   ├── css/style.css
│   └── WEB-INF/web.xml
├── schema.sql                      # run this in MySQL Workbench first
├── pom.xml
└── README.md
```

---

## Setup Instructions

### 1. Clone the repo

```bash
git clone <repo-url>
cd CampusConnect
```

### 2. Set up the database

Open **MySQL Workbench**, connect to your local MySQL instance, and run the entire contents of `schema.sql` (it's a SQL script, not something you run through IntelliJ). This will:

- Create the `campus_connect` database
- Create all tables (`Users`, `Students`, `EventOrganizer`, `Administrator`, `Categories`, `Events`, `Signups`)
- Insert seed data, including 3 demo accounts (see [Demo Accounts](#demo-accounts) below)

> ⚠️ This script drops and recreates the database, so if you already have a `campus_connect` database with different data, back it up first.

### 3. Set your local MySQL credentials

Open `src/main/java/db/DBConnection.java` and update the password to **your own local MySQL root password**:

```java
private static final String PASSWORD = "YOUR_LOCAL_MYSQL_PASSWORD";
```

This is a per-developer setting. Everyone's local password will be different.

### 4. Build the project

In IntelliJ's terminal (or any terminal, **from the project root** — not the Tomcat folder):

```bash
mvn clean package
```

Wait for `BUILD SUCCESS`. This produces `target/CampusConnect.war`.

### 5. Deploy to Tomcat

```bash
# Stop Tomcat if it's already running
cd /path/to/your/apache-tomcat-11.x/bin
./shutdown.sh

# Copy the fresh WAR into Tomcat's webapps folder
# (delete the old exploded CampusConnect/ folder in webapps first, if present)
cp /path/to/CampusConnect/target/CampusConnect.war /path/to/your/apache-tomcat-11.x/webapps/

# Start Tomcat again
./startup.sh
```

### 6. Open the site

```
http://localhost:8080/CampusConnect/index.jsp
```

---

## Demo Accounts

Seeded by `schema.sql`. Password for all three is **`Password123!`**

| Email | Role |
|---|---|
| brandon.phan@sjsu.edu | Student |
| organizer@sjsu.edu | Event Organizer |
| admin@sjsu.edu | Administrator |

You can also register a new student account directly from the site (`register.jsp`) — must use an `@sjsu.edu` email and a password of at least 8 characters.

---

## What's Implemented So Far

- **FR1 — User Authentication**: register, login, logout, session management, bcrypt password hashing
- **FR4 — Event Discovery**: keyword search, category filter, location filter, live spots-left calculation
- **FR5 — Event Signup**: students can sign up for events (auto-registered or auto-waitlisted based on capacity), cancel their signup at any time, and cancelling a *registered* spot automatically promotes the first person on the waitlist. Waitlist positions are renumbered so they stay contiguous after any cancellation.

Everything else (connections, skills, admin tools, notifications) is planned for upcoming milestones.

---

## Database Design Notes

- Users are modeled with the **ISA / E-R separation method**: a shared `Users` table holds common attributes, and `Students`, `EventOrganizer`, `Administrator` are separate subclass tables keyed by `User_ID`. There's no `Role` column — a user's role is determined by which subclass table their `User_ID` appears in.
- 1:N relationships (e.g. an organizer to their events) are implemented as foreign key columns on the "many" side (e.g. `Events.Organizer_ID`), not as separate join tables. Only genuine M:N relationships get their own join table.

