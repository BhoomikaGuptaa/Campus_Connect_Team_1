<%--
    attendees.jsp
    FR6 — Connection Requests

    Shows every student registered for one event, along with a Connect form
    or the current status of an existing connection request.

    Receives from AttendeesServlet:
        eventTitle, eventDate, eventLocation  — event details for the heading
        eventId                               — passed back with every form
        attendees                             — List of Maps, one per student

    Each attendee Map holds:
        User_ID, First_Name, Last_Name, Major, Grad_Year, Bio
        Status         — 'pending', 'accepted', 'declined', or null if none
        TheySent       — true when the other student sent the request to me
        Connection_ID  — needed by the Accept and Decline forms
--%>

<%@ page import="java.util.*, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Data handed over by the servlet.
    List<Map<String, Object>> attendees =
            (List<Map<String, Object>>) request.getAttribute("attendees");
    Integer eventId = (Integer) request.getAttribute("eventId");

    // Flash messages are read once and then removed, so they show on this
    // page load only and do not reappear when the user refreshes.
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError   = (String) session.getAttribute("flashError");
    session.removeAttribute("flashSuccess");
    session.removeAttribute("flashError");

    boolean loggedIn = session.getAttribute("userId") != null;
%>

<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Attendees | CampusConnect</title>
    <link rel="stylesheet" href="css/style.css">
</head>

<body>

<%-- ============================================================
     Navigation bar — matches the other pages in the project
     ============================================================ --%>
<nav class="nav">
    <a class="brand" href="index.jsp">
        <img src="images/sjsu-mark.svg" alt="SJSU">
        <span>Campus<b>Connect</b></span>
    </a>

    <div class="nav-links">
        <a href="index.jsp">Home</a>
        <a href="events.jsp">Events</a>
        <a href="people">People</a>

        <% if (loggedIn) { %>
            <a href="my-events">My Events</a>
            <a href="profile">Profile</a>
            <a href="logout">Log out</a>
        <% } else { %>
            <a href="login.jsp">Log in</a>
        <% } %>
    </div>
</nav>


<main class="container">

    <%-- ============================================================
         Page heading — which event these attendees belong to
         ============================================================ --%>
    <div class="section-head">
        <div>
            <div class="eyebrow" style="color:#0b4f9c">FR6 — Connection requests</div>
            <h2><%= HtmlUtil.esc(request.getAttribute("eventTitle")) %></h2>
            <p class="muted">
                <%= HtmlUtil.esc(request.getAttribute("eventLocation")) %>
                &middot;
                <%= HtmlUtil.esc(request.getAttribute("eventDate")) %>
            </p>
        </div>
    </div>


    <%-- ============================================================
         Flash messages set by ConnectionServlet
         ============================================================ --%>
    <% if (flashSuccess != null) { %>
        <div class="flash success"><%= HtmlUtil.esc(flashSuccess) %></div>
    <% } %>

    <% if (flashError != null) { %>
        <div class="flash error"><%= HtmlUtil.esc(flashError) %></div>
    <% } %>


    <%-- ============================================================
         Attendee list
         ============================================================ --%>
    <% if (attendees == null || attendees.isEmpty()) { %>

        <div class="card empty">
            <p class="muted">No other students are registered for this event yet.</p>
        </div>

    <% } else { %>

        <div class="grid people" style="margin-top:18px">

        <%
            for (Map<String, Object> person : attendees) {

                String  status   = (String) person.get("Status");
                boolean theySent = Boolean.TRUE.equals(person.get("TheySent"));

                // First letter of each name, used for the circular avatar.
                String initials =
                        (String.valueOf(person.get("First_Name")).substring(0, 1) +
                         String.valueOf(person.get("Last_Name")).substring(0, 1)).toUpperCase();
        %>

            <article class="card person-card">

                <div class="avatar"><%= HtmlUtil.esc(initials) %></div>

                <h3>
                    <%= HtmlUtil.esc(person.get("First_Name")) %>
                    <%= HtmlUtil.esc(person.get("Last_Name")) %>
                </h3>

                <p>
                    <b><%= HtmlUtil.esc(person.get("Major")) %></b>
                    &middot; Class of <%= HtmlUtil.esc(person.get("Grad_Year")) %>
                </p>

                <p class="muted"><%= HtmlUtil.esc(person.get("Bio")) %></p>

                <%-- One of four states, decided by Status and TheySent --%>

                <% if ("accepted".equals(status)) { %>

                    <%-- State 1: already connected --%>
                    <span class="badge registered">Connected</span>

                <% } else if ("pending".equals(status) && theySent) { %>

                    <%-- State 2: they asked me, so I get to answer --%>
                    <span class="badge waitlisted">Wants to connect</span>

                    <div style="display:flex; gap:8px; margin-top:10px">

                        <form method="post" action="connect">
                            <input type="hidden" name="action"       value="accept">
                            <input type="hidden" name="connectionId" value="<%= person.get("Connection_ID") %>">
                            <input type="hidden" name="eventId"      value="<%= eventId %>">
                            <button class="btn">Accept</button>
                        </form>

                        <form method="post" action="connect">
                            <input type="hidden" name="action"       value="decline">
                            <input type="hidden" name="connectionId" value="<%= person.get("Connection_ID") %>">
                            <input type="hidden" name="eventId"      value="<%= eventId %>">
                            <button class="btn danger">Decline</button>
                        </form>

                    </div>

                <% } else if ("pending".equals(status)) { %>

                    <%-- State 3: I asked them, still waiting --%>
                    <span class="badge waitlisted">Request sent</span>

                <% } else { %>

                    <%-- State 4: no connection yet (or declined), so offer the form.
                         A declined request can be sent again — ConnectionServlet
                         updates the existing row back to pending. --%>
                    <form method="post" action="connect" style="margin-top:10px">

                        <input type="hidden" name="action"     value="send">
                        <input type="hidden" name="receiverId" value="<%= person.get("User_ID") %>">
                        <input type="hidden" name="eventId"    value="<%= eventId %>">

                        <select name="purpose">
                            <option>Find a Teammate</option>
                            <option>Networking</option>
                            <option>Carpooling</option>
                            <option>Skill Match</option>
                        </select>

                        <input type="text"
                               name="message"
                               maxlength="500"
                               placeholder="Optional message"
                               style="margin-top:8px">

                        <button class="btn" style="margin-top:8px">Connect</button>

                    </form>

                <% } %>

            </article>

        <% } %>

        </div>

    <% } %>

</main>


<div class="footer">CampusConnect &middot; SJSU</div>

</body>
</html>