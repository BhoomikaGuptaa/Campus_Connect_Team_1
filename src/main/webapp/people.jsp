<%@ page import="java.util.*,util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    List<Map<String, Object>> skillOptions =
            (List<Map<String, Object>>) request.getAttribute("skillOptions");
    List<Map<String, Object>> eventOptions =
            (List<Map<String, Object>>) request.getAttribute("eventOptions");
    List<Map<String, Object>> people =
            (List<Map<String, Object>>) request.getAttribute("people");

    Integer selectedSkillId = (Integer) request.getAttribute("selectedSkillId");
    Integer selectedEventId = (Integer) request.getAttribute("selectedEventId");
%>
<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Find People | CampusConnect</title>
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
        <% if (session.getAttribute("userId") != null) { %>
        <a href="my-events">My Events</a>
        <a href="profile">Profile</a>
        <a href="logout">Log out</a>
        <% } else { %>
        <a href="login.jsp">Log in</a>
        <% } %>
    </div>
</nav>

<main class="container">
    <div class="section-head">
        <div>
            <div class="eyebrow" style="color:#0b4f9c">
                FR7 — Skill-Based Search
            </div>
            <h2>Find student teammates</h2>
            <p class="muted">
                Search by skill and optionally filter students by an event they are attending.
            </p>
        </div>
    </div>

    <div class="card">
        <form method="get" action="people"
              style="display:grid;grid-template-columns:1fr 1fr auto;gap:12px">

            <select name="skillId">
                <option value="">All skills</option>
                <%
                    if (skillOptions != null) {
                        for (Map<String, Object> skill : skillOptions) {
                            int skillId = (Integer) skill.get("id");
                %>
                <option value="<%= skillId %>"
                        <%= selectedSkillId != null && selectedSkillId == skillId ? "selected" : "" %>>
                    <%= HtmlUtil.esc(skill.get("name")) %>
                </option>
                <%
                        }
                    }
                %>
            </select>

            <select name="eventId">
                <option value="">All events</option>
                <%
                    if (eventOptions != null) {
                        for (Map<String, Object> event : eventOptions) {
                            int eventId = (Integer) event.get("id");
                %>
                <option value="<%= eventId %>"
                        <%= selectedEventId != null && selectedEventId == eventId ? "selected" : "" %>>
                    <%= HtmlUtil.esc(event.get("title")) %>
                </option>
                <%
                        }
                    }
                %>
            </select>

            <button class="btn">Search</button>
        </form>
    </div>

    <% if (people == null || people.isEmpty()) { %>
    <div class="card empty" style="margin-top:18px">
        <h3>No matching students found</h3>
        <p class="muted">Try another skill or event.</p>
    </div>
    <% } else { %>
    <div class="grid people" style="margin-top:18px">
        <%
            for (Map<String, Object> person : people) {
                String firstName = String.valueOf(person.get("First_Name"));
                String lastName = String.valueOf(person.get("Last_Name"));
                String initials = (firstName.substring(0, 1)
                        + lastName.substring(0, 1)).toUpperCase();
        %>
        <article class="card person-card">
            <div class="avatar"><%= HtmlUtil.esc(initials) %></div>
            <h3>
                <%= HtmlUtil.esc(firstName) %>
                <%= HtmlUtil.esc(lastName) %>
            </h3>
            <p>
                <b><%= HtmlUtil.esc(person.get("Major")) %></b>
                · Class of <%= HtmlUtil.esc(person.get("Grad_Year")) %>
            </p>
            <p class="muted"><%= HtmlUtil.esc(person.get("Bio")) %></p>
            <span class="badge"><%= HtmlUtil.esc(person.get("Skill_List")) %></span>
        </article>
        <%
            }
        %>
    </div>
    <% } %>
</main>
</body>
</html>
