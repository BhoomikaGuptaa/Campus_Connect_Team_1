<%@ page import="java.util.*,util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    List<Map<String, Object>> skills =
            (List<Map<String, Object>>) request.getAttribute("skills");
%>
<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Profile | CampusConnect</title>
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
        <a href="logout">Log out</a>
    </div>
</nav>

<main class="form-shell" style="max-width:850px">
    <div class="form-card">
        <div class="eyebrow" style="color:#0b4f9c">
            FR2 — Student Profile Management
        </div>
        <h1>Your student profile</h1>
        <p class="muted">
            Update your information and choose the skills shown on your public profile.
        </p>

        <%
            Object success = session.getAttribute("flashSuccess");
            Object error = session.getAttribute("flashError");

            if (success != null) {
        %>
        <div class="flash success"><%= HtmlUtil.esc(success) %></div>
        <%
                session.removeAttribute("flashSuccess");
            }

            if (error != null) {
        %>
        <div class="flash error"><%= HtmlUtil.esc(error) %></div>
        <%
                session.removeAttribute("flashError");
            }
        %>

        <form method="post" action="profile">
            <div class="form-row">
                <div>
                    <label class="label">First name</label>
                    <input name="firstName"
                           value="<%= HtmlUtil.esc(request.getAttribute("First_Name")) %>"
                           required>
                </div>
                <div>
                    <label class="label">Last name</label>
                    <input name="lastName"
                           value="<%= HtmlUtil.esc(request.getAttribute("Last_Name")) %>"
                           required>
                </div>
            </div>

            <label class="label">Email</label>
            <input value="<%= HtmlUtil.esc(request.getAttribute("Email")) %>" disabled>

            <div class="form-row">
                <div>
                    <label class="label">Major</label>
                    <input name="major"
                           value="<%= HtmlUtil.esc(request.getAttribute("Major")) %>">
                </div>
                <div>
                    <label class="label">Graduation year</label>
                    <input type="number" name="gradYear" min="2025" max="2035"
                           value="<%= HtmlUtil.esc(request.getAttribute("Grad_Year")) %>"
                           required>
                </div>
            </div>

            <label class="label">Bio</label>
            <textarea name="bio" maxlength="500"><%= HtmlUtil.esc(request.getAttribute("Bio")) %></textarea>

            <label class="label">Skills</label>
            <div class="skill-grid">
                <%
                    if (skills != null) {
                        for (Map<String, Object> skill : skills) {
                %>
                <label class="check">
                    <input type="checkbox" name="skills"
                           value="<%= skill.get("id") %>"
                           <%= Boolean.TRUE.equals(skill.get("selected")) ? "checked" : "" %>>
                    <%= HtmlUtil.esc(skill.get("name")) %>
                </label>
                <%
                        }
                    }
                %>
            </div>

            <button class="btn" style="margin-top:22px">Save profile</button>
        </form>
    </div>
</main>
</body>
</html>
