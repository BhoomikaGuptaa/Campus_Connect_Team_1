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

    WORKED EXAMPLE used in the comments below.
    Brandon (User_ID 1) is logged in and opened /attendees?eventId=1.
    Two other students are registered for event 1:
        Frank Lin (2)  — already connected with Brandon, Connection_ID 3
        Maria Chen (5) — no connection at all
    So attendees arrives as a list of two Maps:
        [ { User_ID=2, First_Name="Frank", Status="accepted",
            TheySent=false, Connection_ID=3, ... },
          { User_ID=5, First_Name="Maria", Status=null,
            TheySent=false, Connection_ID=null, ... } ]
--%>

<%@ page import="java.util.*, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Data handed over by the servlet. getAttribute returns Object, so each
    // one is cast back to the type the servlet put in.
    // EXAMPLE: attendees = the two Maps above, eventId = 1
    List<Map<String, Object>> attendees =
            (List<Map<String, Object>>) request.getAttribute("attendees");
    Integer eventId = (Integer) request.getAttribute("eventId");

    // Flash messages are read once and then removed, so they show on this
    // page load only and do not reappear when the user refreshes.
    // ConnectionServlet is what puts them in the session.
    // EXAMPLE: right after clicking Connect, flashSuccess is
    //          "Connection request sent." On a plain page load both are null.
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError   = (String) session.getAttribute("flashError");
    session.removeAttribute("flashSuccess");
    session.removeAttribute("flashError");

    // EXAMPLE: true, because Brandon is logged in.
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

        <%-- Logged-out visitors only get a Log in link --%>
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

         HtmlUtil.esc() escapes the value before printing. If a title
         contained <script>, it renders as visible characters instead of
         running. Every printed value on this page goes through it.

         EXAMPLE renders:
             VSA General Meeting
             Clark Hall 100 · 2026-07-28 17:00
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
         Skipped entirely when both are null.
         ============================================================ --%>
    <% if (flashSuccess != null) { %>
        <div class="flash success"><%= HtmlUtil.esc(flashSuccess) %></div>
    <% } %>

    <% if (flashError != null) { %>
        <div class="flash error"><%= HtmlUtil.esc(flashError) %></div>
    <% } %>


    <%-- ============================================================
         Attendee list

         Empty happens when nobody else is registered for this event.
         The servlet excludes the logged-in user from their own list,
         so a solo signup produces an empty list rather than one card.
         ============================================================ --%>
    <% if (attendees == null || attendees.isEmpty()) { %>

        <div class="card empty">
            <p class="muted">No other students are registered for this event yet.</p>
        </div>

    <% } else { %>

        <div class="grid people" style="margin-top:18px">

        <%
            // EXAMPLE: this loop runs twice — once for Frank, once for Maria.
            for (Map<String, Object> person : attendees) {

                // EXAMPLE pass 1 (Frank):  status = "accepted", theySent = false
                // EXAMPLE pass 2 (Maria):  status = null,       theySent = false
                String  status   = (String) person.get("Status");
                boolean theySent = Boolean.TRUE.equals(person.get("TheySent"));

                // First letter of each name, used for the circular avatar.
                // substring(0, 1) takes character 0 up to but not including 1.
                // EXAMPLE: Frank Lin -> "FL",  Maria Chen -> "MC"
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

                <%-- One of four states, decided by Status and TheySent.

                     The four are mutually exclusive, which is why this is one
                     if / else if chain rather than four separate ifs.

                     Note the order in "accepted".equals(status) — the literal
                     calls .equals, not status. Written the other way round,
                     status.equals("accepted") would throw a
                     NullPointerException on Maria, whose status is null.
                     This way a null status simply returns false and falls
                     through to State 4.
                --%>

                <% if ("accepted".equals(status)) { %>

                    <%-- State 1: already connected.
                         EXAMPLE: Frank lands here.
                         FR8 messaging was a bonus and is not implemented, so
                         this state is just a status label.
                    --%>
                    <span class="badge registered">Connected</span>


                <% } else if ("pending".equals(status) && theySent) { %>

                    <%-- State 2: they asked me, so I get to answer.
                         Reached only when theySent is true. If Frank had sent
                         the request instead of Brandon, AttendeesServlet would
                         have set TheySent = true and Brandon would see these
                         buttons rather than "Request sent".
                    --%>
                    <span class="badge waitlisted">Wants to connect</span>

                    <div style="display:flex; gap:8px; margin-top:10px">

                        <%-- Hidden fields are how the JSP hands values to the
                             servlet. The user never sees them, but they can be
                             edited with browser dev tools — which is exactly
                             why ConnectionServlet re-checks everything in SQL
                             instead of trusting them. --%>
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

                    <%-- State 3: I asked them, still waiting.
                         Same pending status as State 2, but theySent is false,
                         so there is nothing for me to click. --%>
                    <span class="badge waitlisted">Request sent</span>

                <% } else { %>

                    <%-- State 4: no connection yet (or declined), so offer the form.
                         A declined request can be sent again — ConnectionServlet
                         updates the existing row back to pending.

                         EXAMPLE: Maria lands here, because her Status is null.
                         The form posts:
                             action     = "send"
                             receiverId = 5      (Maria's User_ID)
                             eventId    = 1
                             purpose    = whichever option is selected
                             message    = whatever was typed, or empty
                    --%>
                    <form method="post" action="connect" style="margin-top:10px">

                        <input type="hidden" name="action"     value="send">
                        <input type="hidden" name="receiverId" value="<%= person.get("User_ID") %>">
                        <input type="hidden" name="eventId"    value="<%= eventId %>">

                        <%-- Purpose values match the four listed in FR6 --%>
                        <select name="purpose">
                            <option>Find a Teammate</option>
                            <option>Networking</option>
                            <option>Carpooling</option>
                            <option>Skill Match</option>
                        </select>

                        <%-- maxlength 500 matches Message Varchar(500) in the
                             Connections table. The servlet checks the length
                             again, since maxlength only binds the browser. --%>
                        <input type="text"
                               name="message"
                               maxlength="500"
                               placeholder="Optional message"
                               style="margin-top:8px">

                        <button class="btn" style="margin-top:8px">Connect</button>

                    </form>

                <% } %>

            </article>

        <% } %>          <%-- closes the for loop --%>

        </div>

    <% } %>              <%-- closes if attendees not empty --%>

</main>


<div class="footer">CampusConnect &middot; SJSU</div>

</body>
</html>