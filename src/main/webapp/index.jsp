<%@ page import="java.sql.*, db.DBConnection, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Read whos logged in (if anyone) so I can show the right nav
    // links and greet them by name.
    Integer userId = (Integer) session.getAttribute("userId");
    String role     = (String) session.getAttribute("role");
%>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>CampusConnect</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="home-page">

<nav class="nav">
    <a class="brand" href="index.jsp">
        <img src="images/sjsu.png" alt="San José State University">
        <span>Campus<b>Connect</b></span>
    </a>
    <div class="nav-links">
        <a href="index.jsp">Home</a>
        <% if (userId != null) { %>
            <% if ("admin".equals(role)) { %>
                <a href="admin">Admin Dashboard</a>
            <% } else { %>
                <a href="events.jsp">Events</a>
                <a href="people">Find People</a>
                <a href="my-events">My Events</a>
                <a href="notifications">Notifications</a>
                <a href="profile">Profile</a>
            <% } %>
            <span class="pill">Hi, <%= HtmlUtil.esc(session.getAttribute("firstName")) %></span>
            <a href="logout">Log out</a>
        <% } else { %>
            <a href="login.jsp">Log in</a>
            <a class="pill" href="register.jsp">Join now</a>
        <% } %>
    </div>
</nav>

<section class="hero">
    <div class="hero-inner">
        <div class="hero-copy">
            <div class="eyebrow">Built for the SJSU community</div>
            <h1>
                <span>Discover events.</span>
                <span>Meet new people.</span>
            </h1>
            <p>Find campus opportunities, grow your network, and connect with students who share your interests.</p>
            <div class="hero-actions">
                <a class="btn gold" href="<%= userId == null ? "register.jsp" : "events.jsp" %>">
                    <%= userId == null ? "Create an account" : "Explore all events" %>
                </a>
                <a class="text-link" href="#upcoming">See what's coming up ↓</a>
            </div>
        </div>
        <div class="hero-mark seal-card">
            <img src="images/sjsu.png" alt="San José State University seal" class="sjsu-seal">
        </div>
    </div>
</section>

<main class="home-surface">
    <div class="container">

        <div class="grid features">
            <div class="card">
                <div class="feature-icon">🔎</div>
                <h3>Discover faster</h3>
                <p class="muted">See what is happening around campus without digging through multiple websites.</p>
            </div>
            <div class="card">
                <div class="feature-icon">🎟️</div>
                <h3>Smart signup</h3>
                <p class="muted">Register for events and automatically join a waitlist when an event fills up.</p>
            </div>
            <div class="card">
                <div class="feature-icon">🤝</div>
                <h3>Find teammates</h3>
                <p class="muted">Create an account to search students by major and practical skills.</p>
            </div>
        </div>

        <%-- Show and clear any flash message left by another servlet. --%>
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

        <section id="upcoming" class="upcoming-section">
            <div class="section-head">
                <div>
                    <div class="eyebrow dark">Campus calendar</div>
                    <h2>Upcoming events</h2>
                    <p class="muted">A quick preview of what is happening next at SJSU.</p>
                </div>
                <% if (userId != null) { %>
                    <a class="btn secondary" href="events.jsp">Search and filter</a>
                <% } %>
            </div>

            <div class="public-event-list">
                <%
                    boolean anyResults = false;
                    String errorMessage = null;

                    // Only want events that are still active and havent happened
                    // yet, capped at the next 6.
                    String sql =
                            "SELECT e.Event_ID, e.Title, e.Location, e.Event_Date, e.Description, c.Name AS Category " +
                            "FROM Events e, Categories c " +
                            "WHERE e.Category_ID = c.Category_ID " +
                            "  AND e.Is_Cancelled = 0 " +
                            "  AND e.Event_Date >= NOW() " +
                            "ORDER BY e.Event_Date " +
                            "LIMIT 6";

                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement stmt = con.prepareStatement(sql);
                         ResultSet rs = stmt.executeQuery()) {

                        while (rs.next()) {
                            anyResults = true;
                %>
                    <article class="event-banner">
                        <div class="date-tile">
                            <span><%= new java.text.SimpleDateFormat("MMM").format(rs.getTimestamp("Event_Date")) %></span>
                            <strong><%= new java.text.SimpleDateFormat("dd").format(rs.getTimestamp("Event_Date")) %></strong>
                        </div>
                        <div class="event-preview">
                            <% if (userId != null) { %>
                                <span class="badge"><%= HtmlUtil.esc(rs.getString("Category")) %></span>
                            <% } %>
                            <h3><%= HtmlUtil.esc(rs.getString("Title")) %></h3>
                            <p class="event-line">
                                📍 <%= HtmlUtil.esc(rs.getString("Location")) %>
                                &nbsp; • &nbsp;
                                🕒 <%= new java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a").format(rs.getTimestamp("Event_Date")) %>
                            </p>
                            <% if (userId != null) { %>
                                <p class="muted"><%= HtmlUtil.esc(rs.getString("Description")) %></p>
                            <% } else { %>
                                <p class="locked-copy">Create an account to view the event type, full details, capacity, and signup options.</p>
                            <% } %>
                        </div>
                        <div class="banner-action">
                            <a class="btn <%= userId == null ? "secondary" : "" %>"
                               href="<%= userId == null ? "register.jsp" : "events.jsp" %>">
                                <%= userId == null ? "Join to view" : "View details" %>
                            </a>
                        </div>
                    </article>
                <%
                        }
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }
                %>

                <% if (errorMessage != null) { %>
                    <div class="flash error">
                        <b>Database connection problem.</b> The page design loaded, but event data could not be read.
                        Check that MySQL is running, the schema was executed, and Tomcat received your database password.
                    </div>
                <% } else if (!anyResults) { %>
                    <div class="card empty">
                        <h3>No upcoming events yet</h3>
                        <p class="muted">Run the included schema.sql file to load the demo events.</p>
                    </div>
                <% } %>
            </div>
        </section>

    </div>
</main>

<footer class="footer">© 2026 CampusConnect — San José State University</footer>
</body>
</html>

