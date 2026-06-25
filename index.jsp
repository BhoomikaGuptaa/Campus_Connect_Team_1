<%-- JSP comment: hidden from browser, only visible in source code --%>
<%-- Import Java SQL classes needed for database connection --%>
<%@ page import="java.sql.*" %>
<%-- Tell Tomcat: use Java, send HTML with UTF-8 encoding --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Campus Connect</title>

    <style>
        /* Reset default margin/padding, set font and background for whole page */
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #f9f9f9;
            color: #222;
        }

        /* Top navigation bar */
        nav {
            background: #0055A2;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        nav h1 {
            color: white;
            margin: 0;
            font-size: 1.3rem;
            text-decoration: underline;
        }

        nav a {
            color: white;
            text-decoration: none;
            margin-left: 15px;
            font-weight: bold;
        }

        nav a:hover {
            text-decoration: underline;
        }

        /* Hero banner */
        .hero {
            background: #0055A2;
            color: white;
            text-align: center;
            padding: 65px 20px;
        }

        .hero h2 {
            font-size: 2.1rem;
            margin-bottom: 10px;
        }

        .hero p {
            font-size: 1.05rem;
            margin-bottom: 24px;
        }

        .hero a {
            background: #FFB800;
            color: #003d7a;
            padding: 11px 26px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
            display: inline-block;
        }

        .hero a:hover {
            background: #ffd15c;
        }

        /* Feature cards */
        .features {
            display: flex;
            gap: 20px;
            padding: 35px 30px 10px 30px;
        }

        .feature-card {
            background: white;
            flex: 1;
            padding: 22px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }

        .feature-card h3 {
            margin-top: 0;
            color: #0055A2;
        }

        .feature-card p {
            margin-bottom: 0;
            line-height: 1.5;
        }

        /* Events section wrapper */
        .events {
            padding: 30px;
        }

        .events h3 {
            font-size: 1.4rem;
            color: #0055A2;
            margin-bottom: 18px;
        }

        /* Search/filter form */
        .filter-box {
            background: white;
            padding: 18px;
            margin-bottom: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }

        .filter-box input,
        .filter-box select {
            padding: 10px;
            margin-right: 8px;
            margin-bottom: 8px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        .filter-box button {
            padding: 10px 18px;
            background: #0055A2;
            color: white;
            border: none;
            border-radius: 5px;
            font-weight: bold;
            cursor: pointer;
        }

        .filter-box button:hover {
            background: #003d7a;
        }

        .clear-link {
            margin-left: 10px;
            color: #0055A2;
            text-decoration: none;
            font-weight: bold;
        }

        .clear-link:hover {
            text-decoration: underline;
        }

        /* Events table */
        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            border-radius: 8px;
            overflow: hidden;
        }

        th {
            background: #0055A2;
            color: white;
            padding: 12px;
            text-align: left;
        }

        td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            vertical-align: top;
        }

        tr:hover {
            background: #f0f5ff;
        }

        .event-title {
            font-weight: bold;
            color: #003d7a;
        }

        .event-description {
            display: block;
            margin-top: 5px;
            color: #555;
            font-size: 0.9rem;
            line-height: 1.4;
        }

        .status-open {
            color: #137333;
            font-weight: bold;
        }

        .status-waitlist {
            color: #b06000;
            font-weight: bold;
        }

        .signup-link {
            color: #0055A2;
            font-weight: bold;
            text-decoration: none;
        }

        .signup-link:hover {
            text-decoration: underline;
        }

        .no-events {
            text-align: center;
            color: #666;
            padding: 25px;
        }

        footer {
            text-align: center;
            padding: 20px;
            color: #888;
            font-size: 0.85rem;
        }

        @media screen and (max-width: 800px) {
            .features {
                flex-direction: column;
            }

            nav {
                flex-direction: column;
                gap: 10px;
            }

            .filter-box input,
            .filter-box select {
                width: 100%;
                box-sizing: border-box;
            }

            table {
                font-size: 0.9rem;
            }
        }
    </style>
</head>

<body>

<%-- Navigation bar: logo on left, Home and Login links on right --%>
<nav>
    <h1>CampusConnect</h1>
    <div>
        <a href="index.jsp">Home</a>
        <a href="login.jsp">Login</a>
    </div>
</nav>

<%-- Hero section: big banner with tagline and Get Started button --%>
<div class="hero">
    <h2>🌐 Discover SJSU Events 🌟</h2>
    <p>Find hackathons, career fairs, workshops, club meetings, and networking events in one place.</p>
    <a href="login.jsp">Get Started</a>
</div>

<%-- Feature cards: these match the proposal idea without adding extra backend pages yet --%>
<div class="features">
    <div class="feature-card">
        <h3>Browse Events</h3>
        <p>View upcoming campus events, locations, categories, and available spots.</p>
    </div>

    <div class="feature-card">
        <h3>Sign Up Faster</h3>
        <p>Students can log in and sign up for events once authentication is added.</p>
    </div>

    <div class="feature-card">
        <h3>Connect With Peers</h3>
        <p>Future versions will help students find teammates, network, and connect by skills.</p>
    </div>
</div>

<%-- Events section: table of upcoming events pulled from MySQL --%>
<div class="events">
    <h3>Upcoming Events</h3>

<%
    /* Read filter values from URL query parameters */
    String search = request.getParameter("search");
    String category = request.getParameter("category");
    String location = request.getParameter("location");

    if (search == null) search = "";
    if (category == null) category = "";
    if (location == null) location = "";
%>

    <%-- Search and filter form --%>
    <div class="filter-box">
        <form method="get" action="index.jsp">
            <input type="text" name="search" placeholder="Search events..."
                   value="<%= search %>">

            <select name="category">
                <option value="">All Categories</option>
<%
    /*
       Load categories separately for the dropdown.
       This keeps the homepage dynamic but still simple.
    */
    Connection categoryCon = null;
    PreparedStatement categoryStmt = null;
    ResultSet categoryRs = null;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String dbUrl = "jdbc:mysql://localhost:3306/campusconnect?autoReconnect=true&useSSL=false";
        String dbUser = "root";
        String dbPassword = "Bhoomika$1234";

        categoryCon = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

        String categorySql = "SELECT name FROM categories ORDER BY name ASC";
        categoryStmt = categoryCon.prepareStatement(categorySql);
        categoryRs = categoryStmt.executeQuery();

        while (categoryRs.next()) {
            String categoryName = categoryRs.getString("name");
            String selected = categoryName.equals(category) ? "selected" : "";
%>
                <option value="<%= categoryName %>" <%= selected %>><%= categoryName %></option>
<%
        }
    } catch (Exception e) {
        /* If dropdown fails, homepage table can still try to load below */
    } finally {
        if (categoryRs != null) try { categoryRs.close(); } catch (SQLException ex) {}
        if (categoryStmt != null) try { categoryStmt.close(); } catch (SQLException ex) {}
        if (categoryCon != null) try { categoryCon.close(); } catch (SQLException ex) {}
    }
%>
            </select>

            <input type="text" name="location" placeholder="Location..."
                   value="<%= location %>">

            <button type="submit">Search</button>
            <a class="clear-link" href="index.jsp">Clear</a>
        </form>
    </div>

    <table>
        <%-- Table header row --%>
        <tr>
            <th>Event</th>
            <th>Category</th>
            <th>Date</th>
            <th>Location</th>
            <th>Spots Left</th>
            <th>Status</th>
            <th>Action</th>
        </tr>

<%-- Connect to MySQL and query upcoming events --%>
<%
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        /* Load the MySQL JDBC driver into memory */
        Class.forName("com.mysql.cj.jdbc.Driver");

        /* Database connection info */
        String dbUrl = "jdbc:mysql://localhost:3306/campusconnect?autoReconnect=true&useSSL=false";
        String dbUser = "root";
        String dbPassword = "Bhoomika$1234";

        /* Open connection to campusconnect database on localhost */
        con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

        /*
           Query:
           - Join events and categories
           - Count registered signups
           - Skip cancelled events
           - Apply optional search/category/location filters
        */
        String sql =
            "SELECT e.event_id, e.title, e.description, e.location, e.event_date, e.capacity, " +
            "c.name AS category_name, " +
            "COUNT(s.signup_id) AS registered_count " +
            "FROM events e " +
            "JOIN categories c ON e.category_id = c.category_id " +
            "LEFT JOIN signups s ON e.event_id = s.event_id AND s.status = 'registered' " +
            "WHERE e.is_cancelled = 0 ";

        if (!search.trim().isEmpty()) {
            sql += "AND (e.title LIKE ? OR e.description LIKE ?) ";
        }

        if (!category.trim().isEmpty()) {
            sql += "AND c.name = ? ";
        }

        if (!location.trim().isEmpty()) {
            sql += "AND e.location LIKE ? ";
        }

        sql +=
            "GROUP BY e.event_id, e.title, e.description, e.location, e.event_date, e.capacity, c.name " +
            "ORDER BY e.event_date ASC";

        pstmt = con.prepareStatement(sql);

        int paramIndex = 1;

        if (!search.trim().isEmpty()) {
            pstmt.setString(paramIndex++, "%" + search.trim() + "%");
            pstmt.setString(paramIndex++, "%" + search.trim() + "%");
        }

        if (!category.trim().isEmpty()) {
            pstmt.setString(paramIndex++, category.trim());
        }

        if (!location.trim().isEmpty()) {
            pstmt.setString(paramIndex++, "%" + location.trim() + "%");
        }

        rs = pstmt.executeQuery();

        boolean hasEvents = false;

        while (rs.next()) {
            hasEvents = true;

            int capacity = rs.getInt("capacity");
            int registered = rs.getInt("registered_count");
            int spotsLeft = capacity - registered;

            String status = spotsLeft > 0 ? "Open" : "Waitlist";
            String statusClass = spotsLeft > 0 ? "status-open" : "status-waitlist";
%>
        <%-- Print one table row per event --%>
        <tr>
            <td>
                <span class="event-title"><%= rs.getString("title") %></span>
                <span class="event-description"><%= rs.getString("description") %></span>
            </td>

            <td><%= rs.getString("category_name") %></td>

            <%-- Trim timestamp to date and time, example: 2026-07-08 17:00 --%>
            <td><%= rs.getTimestamp("event_date").toString().substring(0, 16) %></td>

            <td><%= rs.getString("location") %></td>

            <td><%= spotsLeft %> / <%= capacity %></td>

            <td class="<%= statusClass %>"><%= status %></td>

            <%-- Sign Up link goes to login page for now because login/signup is not built yet --%>
            <td><a class="signup-link" href="login.jsp">Sign Up</a></td>
        </tr>
<%
        }

        if (!hasEvents) {
%>
        <tr>
            <td colspan="7" class="no-events">No events found. Try changing your search filters.</td>
        </tr>
<%
        }

    } catch (Exception e) {
        /* If anything goes wrong, print the error inside the table */
        out.println("<tr><td colspan='7'>Error: " + e.getMessage() + "</td></tr>");

    } finally {
        /* Always close resources, even if an error occurred */
        if (rs != null) try { rs.close(); } catch (SQLException ex) {}
        if (pstmt != null) try { pstmt.close(); } catch (SQLException ex) {}
        if (con != null) try { con.close(); } catch (SQLException ex) {}
    }
%>
    </table>
</div>

<%-- Footer --%>
<footer>
    &copy; 2026 CampusConnect — San José State University
</footer>

</body>
</html>