<%@ page import="java.util.*,util.HtmlUtil" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%
    List<Map<String, Object>> organizerRequests =
            (List<Map<String, Object>>) request.getAttribute("organizerRequests");
    List<Map<String, Object>> users =
            (List<Map<String, Object>>) request.getAttribute("users");
    List<Map<String, Object>> categories =
            (List<Map<String, Object>>) request.getAttribute("categories");
    List<Map<String, Object>> activityLogs =
            (List<Map<String, Object>>) request.getAttribute("activityLogs");

    Integer currentAdminId = (Integer) session.getAttribute("userId");
%>
<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Admin Dashboard | CampusConnect</title>
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
        <a href="admin">Admin Dashboard</a>
        <a href="logout">Log out</a>
    </div>
</nav>

<main class="container">
    <div class="section-head">
        <div>
            <div class="eyebrow" style="color:#0b4f9c">
                FR10 — Administrator Functions
            </div>
            <h2>Administrator dashboard</h2>
            <p class="muted">
                Review organizer requests, manage accounts and categories, and view activity logs.
            </p>
        </div>
    </div>

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

    <section>
        <h2>Organizer requests</h2>
        <div class="table-wrap card">
            <table class="data-table">
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Submitted</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                <%
                    if (organizerRequests != null) {
                        for (Map<String, Object> item : organizerRequests) {
                %>
                <tr>
                    <td>
                        <%= HtmlUtil.esc(item.get("First_Name")) %>
                        <%= HtmlUtil.esc(item.get("Last_Name")) %>
                    </td>
                    <td><%= HtmlUtil.esc(item.get("Email")) %></td>
                    <td><%= HtmlUtil.esc(item.get("Submitted_At")) %></td>
                    <td><span class="badge"><%= HtmlUtil.esc(item.get("Status")) %></span></td>
                    <td>
                        <% if ("Pending".equals(String.valueOf(item.get("Status")))) { %>
                        <form method="post" action="admin-action" style="display:inline">
                            <input type="hidden" name="action" value="approveOrganizer">
                            <input type="hidden" name="requestId" value="<%= item.get("Request_ID") %>">
                            <button class="btn">Approve</button>
                        </form>
                        <form method="post" action="admin-action" style="display:inline">
                            <input type="hidden" name="action" value="rejectOrganizer">
                            <input type="hidden" name="requestId" value="<%= item.get("Request_ID") %>">
                            <button class="btn danger">Reject</button>
                        </form>
                        <% } else { %>
                        Reviewed
                        <% } %>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </table>
        </div>
    </section>

    <section>
        <h2>User accounts</h2>
        <div class="table-wrap card">
            <table class="data-table">
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                <%
                    if (users != null) {
                        for (Map<String, Object> user : users) {
                            boolean active = Boolean.TRUE.equals(user.get("Is_Active"));
                            int userId = (Integer) user.get("User_ID");
                %>
                <tr>
                    <td>
                        <%= HtmlUtil.esc(user.get("First_Name")) %>
                        <%= HtmlUtil.esc(user.get("Last_Name")) %>
                    </td>
                    <td><%= HtmlUtil.esc(user.get("Email")) %></td>
                    <td>
                        <span class="badge <%= active ? "registered" : "cancelled" %>">
                            <%= active ? "Active" : "Suspended" %>
                        </span>
                    </td>
                    <td>
                        <% if (currentAdminId != null && currentAdminId == userId) { %>
                        Current admin
                        <% } else { %>
                        <form method="post" action="admin-action">
                            <input type="hidden" name="action"
                                   value="<%= active ? "suspendUser" : "activateUser" %>">
                            <input type="hidden" name="userId" value="<%= userId %>">
                            <button class="btn <%= active ? "danger" : "" %>">
                                <%= active ? "Suspend" : "Reactivate" %>
                            </button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </table>
        </div>
    </section>

    <section>
        <h2>Event categories</h2>
        <div class="card">
            <form method="post" action="admin-action"
                  style="display:grid;grid-template-columns:1fr auto;gap:12px;max-width:600px">
                <input type="hidden" name="action" value="addCategory">
                <input type="text" name="categoryName"
                       placeholder="New category name" required>
                <button class="btn">Add category</button>
            </form>

            <div class="table-wrap" style="margin-top:18px">
                <table class="data-table">
                    <tr>
                        <th>Category</th>
                        <th>Action</th>
                    </tr>
                    <%
                        if (categories != null) {
                            for (Map<String, Object> category : categories) {
                    %>
                    <tr>
                        <td><%= HtmlUtil.esc(category.get("Name")) %></td>
                        <td>
                            <form method="post" action="admin-action">
                                <input type="hidden" name="action" value="deleteCategory">
                                <input type="hidden" name="categoryId"
                                       value="<%= category.get("Category_ID") %>">
                                <button class="btn danger">Delete</button>
                            </form>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </table>
            </div>
        </div>
    </section>

    <section style="margin-bottom:50px">
        <h2>Administrator activity logs</h2>
        <div class="table-wrap card">
            <table class="data-table">
                <tr>
                    <th>Administrator</th>
                    <th>Action</th>
                    <th>Target type</th>
                    <th>Target ID</th>
                    <th>Date</th>
                </tr>
                <%
                    if (activityLogs != null) {
                        for (Map<String, Object> log : activityLogs) {
                %>
                <tr>
                    <td>
                        <%= HtmlUtil.esc(log.get("First_Name")) %>
                        <%= HtmlUtil.esc(log.get("Last_Name")) %>
                    </td>
                    <td><%= HtmlUtil.esc(log.get("Action")) %></td>
                    <td><%= HtmlUtil.esc(log.get("Target_Type")) %></td>
                    <td><%= HtmlUtil.esc(log.get("Target_ID")) %></td>
                    <td><%= HtmlUtil.esc(log.get("Created_At")) %></td>
                </tr>
                <%
                        }
                    }
                %>
            </table>
        </div>
    </section>
</main>
</body>
</html>
