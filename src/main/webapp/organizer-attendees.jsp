<%--
    FR3 — Event Management (attendee list for organizers)

    Shows every student registered for one of my events, so I can see who
    signed up. This is separate from attendees.jsp, which is FR6's page for
    students to browse who else is attending an event and send connection
    requests.

    Receives from EventServlet:
        attendees — List<Attendee>, one per registered student
--%>

<%@ page import="java.util.List, servlets.Attendee, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Get the attendee list EventServlet already queried and packaged.
    List<Attendee> attendees = (List<Attendee>) request.getAttribute("attendees");
%>

<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Attendees | CampusConnect</title>
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
        <a href="my-events">My Events</a>
        <a href="profile">Profile</a>
        <a href="logout">Log out</a>
    </div>
</nav>

<main class="container">

    <div class="section-head">
        <div>
            <div class="eyebrow" style="color:#0b4f9c">FR3 — Event management</div>
            <h2>Registered attendees</h2>
            <p class="muted">Everyone currently registered for this event.</p>
        </div>
    </div>

    <% if (attendees == null || attendees.isEmpty()) { %>

        <div class="card empty">
            <p class="muted">No students are registered for this event yet.</p>
        </div>

    <% } else { %>

        <div class="card table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Status</th>
                        <th>Signed Up At</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Attendee attendee : attendees) { %>
                        <tr>
                            <td><%= HtmlUtil.esc(attendee.getFirstName()) %> <%= HtmlUtil.esc(attendee.getLastName()) %></td>
                            <td><%= HtmlUtil.esc(attendee.getEmail()) %></td>
                            <td><%= HtmlUtil.esc(attendee.getStatus()) %></td>
                            <td><%= attendee.getSignedUpAt() %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

    <% } %>

</main>

</body>
</html>



