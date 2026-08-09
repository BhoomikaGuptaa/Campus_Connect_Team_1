<%@ page import="java.sql.*, db.DBConnection, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // I check login first, since FR4(Event Discovery) also needs to know who I am to
    // show my own signup status and waitlist position on each event card.
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Default every filter to an empty string so I dont have to
    // null-check them again later.
    String search   = request.getParameter("search");
    String category = request.getParameter("category");
    String location = request.getParameter("location");
    String date      = request.getParameter("date");
    if (search == null)   search = "";
    if (category == null) category = "";
    if (location == null) location = "";
    if (date == null)     date = "";
%>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Events | CampusConnect</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

<nav class="nav">
    <a class="brand" href="index.jsp">
        <img src="images/sjsu-mark.svg" alt="SJSU">
        <span>Campus<b>Connect</b></span>
    </a>
    <div class="nav-links">
        <a href="index.jsp">Home</a>
        <a href="events.jsp">Events</a>
        <a href="people">Find People</a>
        <a href="my-events">My Events</a>
        <a href="profile">Profile</a>
        <a href="logout">Log out</a>
    </div>
</nav>

<main class="container">
    <div class="section-head">
        <div>
            <div class="eyebrow dark">Member access</div>
            <h2>Explore all events</h2>
            <p class="muted">Search by keyword, category, date, or location.</p>
        </div>
    </div>

    <%
        Object flashSuccess = session.getAttribute("flashSuccess");
        Object flashError   = session.getAttribute("flashError");
        if (flashSuccess != null) {
    %>
        <div class="flash success"><%= HtmlUtil.esc(flashSuccess) %></div>
    <%
            session.removeAttribute("flashSuccess");
        }
        if (flashError != null) {
    %>
        <div class="flash error"><%= HtmlUtil.esc(flashError) %></div>
    <%
            session.removeAttribute("flashError");
        }
    %>

    <div class="card">
        <form class="filters" method="get">
            <input name="search" placeholder="Search events..." value="<%= HtmlUtil.esc(search) %>">

            <select name="category">
                <option value="">All categories</option>
                <%
                    // Pull the category list fresh from the database so
                    // the dropdown always matches whats actually in Categories.
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement stmt = con.prepareStatement("SELECT Name FROM Categories ORDER BY Name");
                         ResultSet rs = stmt.executeQuery()) {

                        while (rs.next()) {
                            String name = rs.getString("Name");
                %>
                    <option <%= name.equals(category) ? "selected" : "" %>><%= HtmlUtil.esc(name) %></option>
                <%
                        }
                    } catch (Exception e) {
                        // Skip a broken dropdown rather than breaking the whole page.
                    }
                %>
            </select>

            <input name="location" placeholder="Location" value="<%= HtmlUtil.esc(location) %>">
            <input type="date" name="date" value="<%= HtmlUtil.esc(date) %>">

            <button class="btn">Search</button>
            <a class="btn secondary" href="events.jsp">Clear</a>
        </form>
    </div>

    <div class="event-list" style="margin-top:18px">
        <%
            // e.Category_ID = c.Category_ID   links each event to its category name
            // e.Organizer_ID = u.User_ID       links each event to the organizer who created it
            // e.Is_Cancelled = 0               only shows events that are still active
            //   RegisteredCount  counts how many students are registered for this event
            //   MyStatus         checks if I already have a signup here, and what status its at
            //   MyPosition       gets my spot in line if Im on the waitlist
            String sql =
                    "SELECT e.Event_ID, e.Title, e.Description, e.Location, e.Event_Date, e.Capacity, " +
                    "       c.Name AS Category, u.First_Name, u.Last_Name, " +
                    "       (SELECT COUNT(*) FROM Signups sr " +
                    "         WHERE sr.Event_ID = e.Event_ID AND sr.Status = 'registered') AS RegisteredCount, " +
                    "       (SELECT Status FROM Signups me " +
                    "         WHERE me.Event_ID = e.Event_ID AND me.Student_ID = ?) AS MyStatus, " +
                    "       (SELECT Waitlist_Position FROM Signups me " +
                    "         WHERE me.Event_ID = e.Event_ID AND me.Student_ID = ?) AS MyPosition " +
                    "FROM Events e, Categories c, Users u " +
                    "WHERE e.Category_ID = c.Category_ID " +
                    "  AND e.Organizer_ID = u.User_ID " +
                    "  AND e.Is_Cancelled = 0 ";

            // Only add a filter condition for the ones the student actually filled in.
            if (!search.isBlank())   sql += "AND (e.Title LIKE ? OR e.Description LIKE ?) ";
            if (!category.isBlank()) sql += "AND c.Name = ? ";
            if (!location.isBlank()) sql += "AND e.Location LIKE ? ";
            if (!date.isBlank())     sql += "AND DATE(e.Event_Date) = ? ";

            sql += "ORDER BY e.Event_Date";

            boolean anyResults = false;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                int paramIndex = 1;
                stmt.setInt(paramIndex++, userId);
                stmt.setInt(paramIndex++, userId);

                if (!search.isBlank()) {
                    stmt.setString(paramIndex++, "%" + search + "%");
                    stmt.setString(paramIndex++, "%" + search + "%");
                }
                if (!category.isBlank()) {
                    stmt.setString(paramIndex++, category);
                }
                if (!location.isBlank()) {
                    stmt.setString(paramIndex++, "%" + location + "%");
                }
                if (!date.isBlank()) {
                    stmt.setString(paramIndex++, date);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        anyResults = true;
                        int spotsLeft = rs.getInt("Capacity") - rs.getInt("RegisteredCount");
                        String myStatus = rs.getString("MyStatus");
        %>
        <article class="event-card">
            <div>
                <span class="badge"><%= HtmlUtil.esc(rs.getString("Category")) %></span>
                <h3><%= HtmlUtil.esc(rs.getString("Title")) %></h3>
                <p class="muted"><%= HtmlUtil.esc(rs.getString("Description")) %></p>
                <div class="meta">
                    <span>📅 <%= new java.text.SimpleDateFormat("EEE, MMM d, yyyy h:mm a").format(rs.getTimestamp("Event_Date")) %></span>
                    <span>📍 <%= HtmlUtil.esc(rs.getString("Location")) %></span>
                    <span>Hosted by <%= HtmlUtil.esc(rs.getString("First_Name")) %> <%= HtmlUtil.esc(rs.getString("Last_Name")) %></span>
                </div>
            </div>
            <div class="actions">
                <div class="spots"><%= spotsLeft %> spots left</div>
                <%
                    if ("registered".equals(myStatus)) {
                %>
                    <span class="badge registered">Registered</span>
                    <form method="post" action="signup" style="margin-top:9px">
                        <input type="hidden" name="eventId" value="<%= rs.getInt("Event_ID") %>">
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="returnTo" value="events.jsp">
                        <button class="btn danger">Cancel</button>
                    </form>
                <%
                    } else if ("waitlisted".equals(myStatus)) {
                %>
                    <span class="badge waitlisted">Waitlist #<%= rs.getInt("MyPosition") %></span>
                    <form method="post" action="signup" style="margin-top:9px">
                        <input type="hidden" name="eventId" value="<%= rs.getInt("Event_ID") %>">
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="returnTo" value="events.jsp">
                        <button class="btn danger">Leave waitlist</button>
                    </form>
                <%
                    } else {
                %>
                    <form method="post" action="signup">
                        <input type="hidden" name="eventId" value="<%= rs.getInt("Event_ID") %>">
                        <input type="hidden" name="returnTo" value="events.jsp">
                        <button class="btn"><%= spotsLeft > 0 ? "Sign up" : "Join waitlist" %></button>
                    </form>
                <%
                    }
                %>
            </div>
        </article>
        <%
                    }
                }
            } catch (Exception e) {
        %>
        <div class="flash error">Database error: <%= HtmlUtil.esc(e.getMessage()) %></div>
        <%
            }
            if (!anyResults) {
        %>
        <div class="card empty">
            <h3>No events found</h3>
            <p class="muted">Try clearing one or more filters.</p>
        </div>
        <%
            }
        %>
    </div>
</main>

<footer class="footer">© 2026 CampusConnect — San José State University</footer>
</body>
</html>

