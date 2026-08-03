<%--
    notifications.jsp
    FR9 — Notifications

    Shows every notification for the logged-in user, newest first, and lets
    them be marked as read.

    Receives from NotificationServlet:
        notifications — List of Maps, one per notification row
        unreadCount   — how many are still unread

    Each notification Map holds:
        Notification_ID, Message, Type, Is_Read, Created_At
--%>

<%@ page import="java.util.*, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Data handed over by the servlet.
    List<Map<String, Object>> notifications =
            (List<Map<String, Object>>) request.getAttribute("notifications");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");

    // Flash messages are read once and then removed.
    Object flashSuccess = session.getAttribute("flashSuccess");
    Object flashError   = session.getAttribute("flashError");
    session.removeAttribute("flashSuccess");
    session.removeAttribute("flashError");
%>

<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Notifications | CampusConnect</title>
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
        <a href="my-events">My Events</a>
        <a href="notifications">Notifications</a>
        <a href="profile">Profile</a>
        <a href="logout">Log out</a>
    </div>
</nav>


<main class="container">

    <%-- ============================================================
         Page heading, with the unread count
         ============================================================ --%>
    <div class="section-head">
        <div>
            <div class="eyebrow" style="color:#0b4f9c">FR9 — Notifications</div>
            <h2>
                Notifications
                <% if (unreadCount != null && unreadCount > 0) { %>
                    <span class="badge waitlisted"><%= unreadCount %> unread</span>
                <% } %>
            </h2>
            <p class="muted">Signup confirmations, waitlist updates, and connection activity.</p>
        </div>

        <%-- Only offer "mark all" when there is something to mark --%>
        <% if (unreadCount != null && unreadCount > 0) { %>
            <form method="post" action="notifications">
                <input type="hidden" name="action" value="markAll">
                <button class="btn secondary">Mark all as read</button>
            </form>
        <% } %>
    </div>


    <%-- ============================================================
         Flash messages
         ============================================================ --%>
    <% if (flashSuccess != null) { %>
        <div class="flash success"><%= HtmlUtil.esc(flashSuccess) %></div>
    <% } %>

    <% if (flashError != null) { %>
        <div class="flash error"><%= HtmlUtil.esc(flashError) %></div>
    <% } %>


    <%-- ============================================================
         Notification list
         ============================================================ --%>
    <% if (notifications == null || notifications.isEmpty()) { %>

        <div class="card empty">
            <p class="muted">You have no notifications yet.</p>
        </div>

    <% } else { %>

        <div class="event-list">

        <%
            for (Map<String, Object> note : notifications) {

                boolean isRead = Boolean.TRUE.equals(note.get("Is_Read"));

                // Unread rows get the gold left border used elsewhere in the app.
                String cardStyle = isRead
                        ? "opacity:0.65"
                        : "border-left:5px solid var(--gold)";
        %>

            <article class="event-card" style="<%= cardStyle %>">

                <div>
                    <h3><%= HtmlUtil.esc(note.get("Message")) %></h3>

                    <div class="meta">
                        <span><%= HtmlUtil.esc(note.get("Type")) %></span>
                        <span><%= HtmlUtil.esc(note.get("Created_At")) %></span>
                    </div>
                </div>

                <div class="actions">
                    <% if (isRead) { %>

                        <span class="badge cancelled">Read</span>

                    <% } else { %>

                        <form method="post" action="notifications">
                            <input type="hidden" name="notificationId"
                                   value="<%= note.get("Notification_ID") %>">
                            <button class="btn secondary">Mark as read</button>
                        </form>

                    <% } %>
                </div>

            </article>

        <% } %>

        </div>

    <% } %>

</main>


<div class="footer">CampusConnect &middot; SJSU</div>

</body>
</html>