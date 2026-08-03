<%--
    my-events.jsp
    FR5 — Signup History

    Lists every event the logged-in student has signed up for, with the
    current status of each signup and an option to cancel.

    Receives from MyEventsServlet:
        events — List of Maps, one per signup row

    Each event Map holds:
        Event_ID, Title, Category, Event_Date, Location
        Status            — 'registered', 'waitlisted', or 'cancelled'
        Waitlist_Position — only meaningful when Status is 'waitlisted'
--%>

<%@ page import="java.util.*, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Data handed over by the servlet.
    List<Map<String, Object>> events =
            (List<Map<String, Object>>) request.getAttribute("events");

    // Flash messages are read once and then removed, so they show on this
    // page load only and do not reappear when the user refreshes.
    Object flashSuccess = session.getAttribute("flashSuccess");
    Object flashError   = session.getAttribute("flashError");
    session.removeAttribute("flashSuccess");
    session.removeAttribute("flashError");
%>

<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Events | CampusConnect</title>
    <link rel="stylesheet" href="css/style.css">
</head>

<body>

<%-- ============================================================
     Navigation bar
     ============================================================ --%>
<nav class="nav">
    <a class="brand" href="index.jsp">
        <img src="images/sjsu-mark.svg" alt="SJSU">
        <span>Campus<b>Connect</b></span>
    </a>

    <div class="nav-links">
        <a href="index.jsp">Home</a>
        <a href="events.jsp">Events</a>
        <a href="people">Find People</a>
        <a href="profile">Profile</a>
        <a href="logout">Log out</a>
    </div>
</nav>


<main class="container">

    <%-- ============================================================
         Page heading
         ============================================================ --%>
    <div class="section-head">
        <div>
            <div class="eyebrow" style="color:#0b4f9c">FR5 — Signup history</div>
            <h2>My events</h2>
            <p class="muted">See registered, waitlisted, and cancelled event records.</p>
        </div>
    </div>


    <%-- ============================================================
         Flash messages set by SignupServlet
         ============================================================ --%>
    <% if (flashSuccess != null) { %>
        <div class="flash success"><%= HtmlUtil.esc(flashSuccess) %></div>
    <% } %>

    <% if (flashError != null) { %>
        <div class="flash error"><%= HtmlUtil.esc(flashError) %></div>
    <% } %>


    <%-- ============================================================
         Signup table
         ============================================================ --%>
    <div class="card table-wrap">
        <table class="data-table">

            <thead>
                <tr>
                    <th>Event</th>
                    <th>Category</th>
                    <th>Date</th>
                    <th>Location</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>

            <tbody>

            <% if (events != null && !events.isEmpty()) { %>

                <%
                    for (Map<String, Object> event : events) {

                        // Status drives both the badge colour and which
                        // actions are offered in the last column.
                        String status = String.valueOf(event.get("Status"));
                %>

                    <tr>
                        <td><b><%= HtmlUtil.esc(event.get("Title")) %></b></td>
                        <td><%= HtmlUtil.esc(event.get("Category")) %></td>
                        <td><%= HtmlUtil.esc(event.get("Event_Date")) %></td>
                        <td><%= HtmlUtil.esc(event.get("Location")) %></td>

                        <%-- Status badge, with waitlist position when relevant --%>
                        <td>
                            <span class="badge <%= status %>">
                                <%= HtmlUtil.esc(status) %>
                                <% if ("waitlisted".equals(status)) { %>
                                    #<%= event.get("Waitlist_Position") %>
                                <% } %>
                            </span>
                        </td>

                        <%-- Actions: only offered while the signup is active --%>
                        <td>
                            <% if (!"cancelled".equals(status)) { %>

                                <%-- FR6 — attendees of an event I am attending.
                                     Placed here rather than on events.jsp because
                                     ConnectionServlet only allows a request when
                                     both students are registered for the event. --%>
                                <a class="btn secondary"
                                   href="attendees?eventId=<%= event.get("Event_ID") %>">
                                    See who's going
                                </a>

                                <form method="post" action="signup">
                                    <input type="hidden" name="eventId"  value="<%= event.get("Event_ID") %>">
                                    <input type="hidden" name="action"   value="cancel">
                                    <input type="hidden" name="returnTo" value="my-events">
                                    <button class="btn danger">Cancel</button>
                                </form>

                            <% } else { %>

                                <span class="muted">&mdash;</span>

                            <% } %>
                        </td>
                    </tr>

                <% } %>

            <% } else { %>

                <tr>
                    <td colspan="6" class="empty">You have not signed up for an event yet.</td>
                </tr>

            <% } %>

            </tbody>
        </table>
    </div>

</main>

</body>
</html>