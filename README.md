# CampusConnect - Tomcat 9 Compatible

This build uses javax.servlet and is compatible with Apache Tomcat 9.0.x.

# CampusConnect — polished code-review build

This build completes five proposal requirements:

1. **FR1 User Authentication** — student registration, SJSU-email validation, BCrypt password hashing, login/logout, inactive-account check, 30-minute sessions.
2. **FR2 Student Profile Management** — edit name, major, graduation year, bio, and skills.
3. **FR4 Event Discovery** — browse and filter by keyword, category, exact date, and location; organizer and remaining capacity are shown.
4. **FR5 Event Signup + Waitlist** — signup, duplicate prevention, cancellation, automatic waitlist, promotion, renumbering, and complete signup history.
5. **FR7 Skill-Based Search** — browse students and filter public profiles by skill.

## Prerequisites

- JDK 17 or newer (`java -version`)
- Maven 3.9+ (`mvn -version`)
- MySQL Community Server 8+
- Apache Tomcat 11

## Database setup

1. Open MySQL Workbench.
2. Open `schema.sql` and run the entire script.
3. Set the database credentials as environment variables. **Do not put passwords in Git.**

### Windows PowerShell

```powershell
$env:CAMPUS_DB_USER="root"
$env:CAMPUS_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:CAMPUS_DB_URL="jdbc:mysql://localhost:3306/campus_connect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Los_Angeles"
```

These variables must be available to the process that starts Tomcat. For a permanent Windows setup, add them under **System Properties → Environment Variables**, then restart IntelliJ/Tomcat.

### macOS/Linux

```bash
export CAMPUS_DB_USER=root
export CAMPUS_DB_PASSWORD='YOUR_MYSQL_PASSWORD'
export CAMPUS_DB_URL='jdbc:mysql://localhost:3306/campus_connect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Los_Angeles'
```

## Build

From the project root:

```bash
mvn clean package
```

The deployable file will be:

```text
target/CampusConnect.war
```

## Deploy to Tomcat 11

1. Stop Tomcat.
2. Delete any old `CampusConnect.war` and old exploded `CampusConnect/` directory from Tomcat's `webapps` folder.
3. Copy `target/CampusConnect.war` into `webapps`.
4. Start Tomcat.
5. Open `http://localhost:8080/CampusConnect/`.

### Windows example

```powershell
cd C:\apache-tomcat-11\bin
.\shutdown.bat
copy C:\path\to\CampusConnect\target\CampusConnect.war C:\apache-tomcat-11\webapps\
.\startup.bat
```

### macOS/Linux example

```bash
$CATALINA_HOME/bin/shutdown.sh
rm -rf $CATALINA_HOME/webapps/CampusConnect $CATALINA_HOME/webapps/CampusConnect.war
cp target/CampusConnect.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```

## Demo accounts

All demo passwords are `Password123!`.

- Student: `brandon.phan@sjsu.edu`
- Student: `frank.lin02@sjsu.edu`
- Organizer seed account: `organizer@sjsu.edu`
- Administrator seed account: `admin@sjsu.edu`

The current five-requirement UI focuses on student workflows. Organizer and administrator dashboards remain future work.

## Suggested demo

Register a new account → log in → update profile and skills → filter events by category/date/location → join an open event → join a full event to demonstrate waitlisting → open My Events → cancel a registration → search students by skill.

## Updated public homepage
The public homepage now shows simple upcoming-event banners only. Full event categories, descriptions, capacity, search filters, and signup controls are available after login on `events.jsp`.
