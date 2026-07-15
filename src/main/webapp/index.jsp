<%-- JSP comment: hidden from browser, only visible in source code --%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="db.DBConnection" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Campus Connect</title>

    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f9f9f9; color: #222; }
        nav { background: #0055A2; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        nav h1 { color: white; margin: 0; font-size: 1.3rem; text-decoration: underline; }
        nav a { color: white; text-decoration: none; margin-left: 15px; font-weight: bold; }
        nav a:hover { text-decoration: underline; }
        nav .greeting { color: white; margin-left: 15px; }

        .hero { background: #0055A2; color: white; text-align: center; padding: 65px 20px; }
        .hero h2 { font-size: 2.1rem; margin-bottom: 10px; }
        .hero p { font-size: 1.05rem; margin-bottom: 24px; }
        .hero a { background: #FFB800; color: #003d7a; padding: 11px 26px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; }
        .hero a:hover { background: #ffd15c; }

        .features { display: flex; gap: 20px; padding: 35px 30px 10px 30px; }
        .feature-card { background: white; flex: 1; padding: 22px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .feature-card h3 { margin-top: 0; color: #0055A2; }
        .feature-card p { margin-bottom: 0; line-height: 1.5; }

        .events { padding: 30px; }
        .events h3 { font-size: 1.4rem; color: #0055A2; margin-bottom: 18px; }

        .filter-box { background: white; padding: 18px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .filter-box input, .filter-box select { padding: 10px; margin-right: 8px; margin-bottom: 8px; border: 1px solid #ccc; border-radius: 5px; }
        .filter-box button { padding: 10px 18px; background: #0055A2; color: white; border: none; border-radius: 5px; font-weight: bold; cursor: pointer; }
        .filter-box button:hover { background: #003d7a; }
        .clear-link { margin-left: 10px; color: #0055A2; text-decoration: none; font-weight: bold; }
        .clear-link:hover { text-decoration: underline; }

        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.08); border-radius: 8px; overflow: hidden; }
        th { background: #0055A2; color: white; padding: 12px; text-align: left; }
        td { padding: 12px; border-bottom: 1px solid #ddd; vertical-align: top; }
        tr:hover { background: #f0f5ff; }
        .event-title { font-weight: bold; color: #003d7a; }
        .event-description { display: block; margin-top: 5px; color: #555; font-size: 0.9rem; line-height: 1.4; }
        .status-open { color: #137333; font-weight: bold; }
        .status-waitlist { color: #b06000; font-weight: bold; }
        .signup-link { color: #0055A2; font-weight: bold; text-decoration: none; }
        .signup-link:hover { text-decoration: underline; }
        .signup-btn { background: none; border: none; padding: 0; font: inherit; cursor: pointer; }
        .students-only { color: #999; font-size: 0.85rem; font-style: italic; }
        .no-events { text-align: center; color: #666; padding: 25px; }

        footer { text-align: center; padding: 20px; color: #888; font-size: 0.85rem; }

        @media screen and (max-width: 800px) {
            .features { flex-direction: column; }
            nav { flex-direction: column; gap: 10px; }
            .filter-box input, .filter-box select { width: 100%; box-sizing: border-box; }
            table { font-size: 0.9rem; }
        }
    </style>
</head>

<body>

<%
    /* Check whether the visitor is logged in, and their role (used by nav bar + Action column) */
    HttpSession userSession = request.getSession(false);
    String loggedInFirstName = (userSession != null) ? (String) userSession.getAttribute("firstName") : null;
    String role = (userSession != null) ? (String) userSession.getAttribute("role") : null;
    Integer loggedInUserId = (userSession != null) ? (Integer) userSession.getAttribute("userId") : null;
%>

<%-- Navigation bar: logo on left, session-aware links on right --%>
<nav>
    <h1>CampusConnect</h1>
    <div>
        <a href="index.jsp">Home</a>
        <% if (loggedInFirstName != null) { %>
            <span class="greeting">Hi, <%= loggedInFirstName %></span>
            <a href="logout">Logout</a>
        <% } else { %>
            <a href="login.jsp">Login</a>
        <% } %>
    </div>
</nav>

<%-- Hero section --%>
<div class="hero">
    <h2>🌐 Discover SJSU Events 🌟</h2>
    <p>Find hackathons, career fairs, workshops, club meetings, and networking events in one place.</p>
    <% if (loggedInFirstName == null) { %>
        <a href="login.jsp">Get Started</a>
    <% } %>
</div>

<%-- Feature cards --%>
<div class="features">
    <div class="feature-card">
        <h3>Browse Events</h3>
        <p>View upcoming campus events, locations, categories, and available spots.</p>
    </div>
    <div class="feature-card">
        <h3>Sign Up Faster</h3>
        <p>Log in as a student to sign up for events, with automatic waitlist handling.</p>
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

    /* Pre-fetch this student's active signups (Event_ID -> Status / Waitlist_Position)
       so we know what to show in the Action column for each event row below. */
    Map<Integer, String> mySignupStatus = new HashMap<Integer, String>();
    Map<Integer, Integer> mySignupWaitlistPos = new HashMap<Integer, Integer>();

    if ("student".equals(role) && loggedInUserId != null) {
        try (Connection sc = DBConnection.getConnection();
             PreparedStatement sStmt = sc.prepareStatement(
                 "SELECT Event_ID, Status, Waitlist_Position FROM Signups " +
                 "WHERE Student_ID = ? AND Status IN ('registered', 'waitlisted')")) {
            sStmt.setInt(1, loggedInUserId);
            try (ResultSet sRs = sStmt.executeQuery()) {
                while (sRs.next()) {
                    mySignupStatus.put(sRs.getInt("Event_ID"), sRs.getString("Status"));
                    mySignupWaitlistPos.put(sRs.getInt("Event_ID"), sRs.getInt("Waitlist_Position"));
                }
            }
        } catch (SQLException ex) {
            // if this fails, Action column falls back to showing plain Sign Up links
        }
    }
%>

    <%-- Search and filter form --%>
    <div class="filter-box">
        <form method="get" action="index.jsp">
            <input type="text" name="search" placeholder="Search events..."
                   value="<%= search %>">

            <select name="category">
                <option value="">All Categories</option>
<%
    Connection categoryCon = null;
    PreparedStatement categoryStmt = null;
    ResultSet categoryRs = null;

    try {
        categoryCon = DBConnection.getConnection();

        String categorySql = "SELECT Name FROM Categories ORDER BY Name ASC";
        categoryStmt = categoryCon.prepareStatement(categorySql);
        categoryRs = categoryStmt.executeQuery();

        while (categoryRs.next()) {
            String categoryName = categoryRs.getString("Name");
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
        <tr>
            <th>Event</th>
            <th>Category</th>
            <th>Date</th>
            <th>Location</th>
            <th>Spots Left</th>
            <th>Status</th>
            <th>Action</th>
        </tr>

<%
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        con = DBConnection.getConnection();

        String sql =
            "SELECT e.Event_ID, e.Title, e.Description, e.Location, e.Event_Date, e.Capacity, " +
            "c.Name AS category_name, " +
            "COUNT(s.Signup_ID) AS registered_count " +
            "FROM Events e " +
            "JOIN Categories c ON e.Category_ID = c.Category_ID " +
            "LEFT JOIN Signups s ON e.Event_ID = s.Event_ID AND s.Status = 'registered' " +
            "WHERE e.Is_Cancelled = 0 ";

        if (!search.trim().isEmpty()) {
            sql += "AND (e.Title LIKE ? OR e.Description LIKE ?) ";
        }
        if (!category.trim().isEmpty()) {
            sql += "AND c.Name = ? ";
        }
        if (!location.trim().isEmpty()) {
            sql += "AND e.Location LIKE ? ";
        }

        sql +=
            "GROUP BY e.Event_ID, e.Title, e.Description, e.Location, e.Event_Date, e.Capacity, c.Name " +
            "ORDER BY e.Event_Date ASC";

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

            int currentEventId = rs.getInt("Event_ID");
            int capacity = rs.getInt("Capacity");
            int registered = rs.getInt("registered_count");
            int spotsLeft = capacity - registered;

            String status = spotsLeft > 0 ? "Open" : "Waitlist";
            String statusClass = spotsLeft > 0 ? "status-open" : "status-waitlist";

            String mySignupStatusForEvent = mySignupStatus.get(currentEventId);
%>
        <tr>
            <td>
                <span class="event-title"><%= rs.getString("Title") %></span>
                <span class="event-description"><%= rs.getString("Description") %></span>
            </td>
            <td><%= rs.getString("category_name") %></td>
            <td><%= rs.getTimestamp("Event_Date").toString().substring(0, 16) %></td>
            <td><%= rs.getString("Location") %></td>
            <td><%= spotsLeft %> / <%= capacity %></td>
            <td class="<%= statusClass %>"><%= status %></td>
            <td>
<%
    if (loggedInFirstName == null) {
%>
                <a class="signup-link" href="login.jsp">Sign Up</a>
<%
    } else if (!"student".equals(role)) {
%>
                <span class="students-only">Students only</span>
<%
    } else if ("registered".equals(mySignupStatusForEvent)) {
%>
                <span class="status-open">Registered</span><br>
                <form method="post" action="signup" style="display:inline;">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="eventId" value="<%= currentEventId %>">
                    <button type="submit" class="signup-link signup-btn">Cancel</button>
                </form>
<%
    } else if ("waitlisted".equals(mySignupStatusForEvent)) {
        Integer myPos = mySignupWaitlistPos.get(currentEventId);
%>
                <span class="status-waitlist">Waitlisted (#<%= myPos %>)</span><br>
                <form method="post" action="signup" style="display:inline;">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="eventId" value="<%= currentEventId %>">
                    <button type="submit" class="signup-link signup-btn">Cancel</button>
                </form>
<%
    } else {
%>
                <form method="post" action="signup" style="display:inline;">
                    <input type="hidden" name="action" value="signup">
                    <input type="hidden" name="eventId" value="<%= currentEventId %>">
                    <button type="submit" class="signup-link signup-btn">Sign Up</button>
                </form>
<%
    }
%>
            </td>
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
        out.println("<tr><td colspan='7'>Error: " + e.getMessage() + "</td></tr>");
    } finally {
        if (rs != null) try { rs.close(); } catch (SQLException ex) {}
        if (pstmt != null) try { pstmt.close(); } catch (SQLException ex) {}
        if (con != null) try { con.close(); } catch (SQLException ex) {}
    }
%>
    </table>
</div>

<footer>
    &copy; 2026 CampusConnect — San José State University
</footer>

</body>
</html>