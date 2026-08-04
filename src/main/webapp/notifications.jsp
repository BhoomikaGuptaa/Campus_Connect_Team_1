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

    WORKED EXAMPLE used in the comments below.
    Brandon (User_ID 1) clicks Notifications in the nav. The servlet hands over:

        unreadCount   = 1
        notifications = [ { Notification_ID=5,
                            Message="Your connection request was accepted.",
                            Type="Connection_Accepted",
                            Is_Read=false, Created_At=... },
                          { Notification_ID=2,
                            Message="Your connection request was accepted.",
                            Type="Connection_Accepted",
                            Is_Read=true,  Created_At=... } ]

    Row 5 is newer, which is why Order by Created_At Desc put it first.
--%>

<%@ page import="java.util.*, util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    // Data handed over by the servlet. getAttribute returns Object, so each
    // one is cast back to what the servlet put in.
    // EXAMPLE: notifications = the two Maps above, unreadCount = 1
    List<Map<String, Object>> notifications =
            (List<Map<String, Object>>) request.getAttribute("notifications");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");

    // Flash messages are read once and then removed.
    // EXAMPLE: after clicking Mark all as read, flashSuccess is
    //          "1 notification(s) marked as read."
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

     No logged-in check here, unlike attendees.jsp. This page is only
     ever reached through the servlet, which already redirects anyone
     not logged in — so by the time this JSP runs, there is a user.
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

                <%-- The null check comes first for a reason: if unreadCount
                     were null, unreadCount > 0 would unbox null to an int and
                     throw a NullPointerException. Java evaluates && left to
                     right and stops at the first false, so the null check
                     protects the comparison after it.

                     EXAMPLE: unreadCount is 1, so this renders "1 unread".
                             Once everything is read it becomes 0 and the
                             badge disappears entirely. --%>
                <% if (unreadCount != null && unreadCount > 0) { %>
                    <span class="badge waitlisted"><%= unreadCount %> unread</span>
                <% } %>
            </h2>
            <p class="muted">Signup confirmations, waitlist updates, and connection activity.</p>
        </div>

        <%-- Only offer "mark all" when there is something to mark.
             Same condition as the badge, so the button and the badge appear
             and vanish together.

             This form posts action="markAll" and nothing else — the servlet
             does not need an ID, because the Where clause targets every
             unread row belonging to the logged-in user. --%>
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

         Empty is a real state for a brand new account — no signups, no
         connection activity, so nothing has been generated yet.
         ============================================================ --%>
    <% if (notifications == null || notifications.isEmpty()) { %>

        <div class="card empty">
            <p class="muted">You have no notifications yet.</p>
        </div>

    <% } else { %>

        <div class="event-list">

        <%
            // EXAMPLE: runs twice — row 5 first, then row 2.
            for (Map<String, Object> note : notifications) {

                // The servlet used rs.getBoolean, so this value is already a
                // Java Boolean rather than MySQL's 0 or 1.
                // Boolean.TRUE.equals(...) is null-safe: a null would return
                // false rather than throwing.
                //
                // EXAMPLE pass 1 (row 5): isRead = false
                // EXAMPLE pass 2 (row 2): isRead = true
                boolean isRead = Boolean.TRUE.equals(note.get("Is_Read"));

                // Unread rows get the gold left border used elsewhere in the app.
                // Read rows are faded instead, so unread ones stand out.
                //
                // This is a ternary: condition ? valueIfTrue : valueIfFalse.
                // EXAMPLE pass 1: "border-left:5px solid var(--gold)"
                // EXAMPLE pass 2: "opacity:0.65"
                String cardStyle = isRead
                        ? "opacity:0.65"
                        : "border-left:5px solid var(--gold)";
        %>

            <article class="event-card" style="<%= cardStyle %>">

                <div>
                    <%-- The Message column is the sentence written by whichever
                         servlet created the row. ConnectionServlet.notify()
                         wrote this one.
                         EXAMPLE: "Your connection request was accepted." --%>
                    <h3><%= HtmlUtil.esc(note.get("Message")) %></h3>

                    <div class="meta">
                        <%-- Type is the machine-readable category, stored as
                             Varchar(50). Shown here mostly so the demo makes
                             the column visible.
                             EXAMPLE: "Connection_Accepted" --%>
                        <span><%= HtmlUtil.esc(note.get("Type")) %></span>
                        <span><%= HtmlUtil.esc(note.get("Created_At")) %></span>
                    </div>
                </div>

                <div class="actions">
                    <% if (isRead) { %>

                        <%-- EXAMPLE pass 2: row 2 is already read, so there is
                             nothing to click — just a grey Read badge. --%>
                        <span class="badge cancelled">Read</span>

                    <% } else { %>

                        <%-- EXAMPLE pass 1: row 5 is unread, so it gets a button.

                             This form sends no action field, which is what makes
                             the servlet fall to its else branch and run the
                             single-row Update instead of markAll.

                             notificationId is a hidden field, so a user could
                             edit it with dev tools — which is exactly why the
                             servlet's Update also carries And User_ID = ?.
                             Posting someone else's ID matches no row and
                             changes nothing.

                             EXAMPLE: posts notificationId = 5 --%>
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