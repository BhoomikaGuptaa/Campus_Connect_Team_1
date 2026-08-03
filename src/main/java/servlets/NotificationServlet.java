package servlets;

import db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FR9 — Notifications.
 *
 * ConnectionServlet already creates the rows. This servlet is the other half:
 * showing them to the user and letting them be marked as read.
 *
 * doGet  — list every notification for the logged-in user
 * doPost — mark one notification read, or mark all of them read
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        int myId = (Integer) session.getAttribute("userId");

        // Newest first, so the most recent activity is at the top of the page.
        String listSql =
                "Select Notification_ID, Message, Type, Is_Read, Created_At " +
                "From Notifications " +
                "Where User_ID = ? " +
                "Order by Created_At Desc";

        // The unread count shown next to the page heading. This is the query
        // idx_unread was created for.
        String countSql =
                "Select Count(*) As Unread " +
                "From Notifications " +
                "Where User_ID = ? " +
                "  And Is_Read = False";

        List<Map<String, Object>> notifications = new ArrayList<>();
        int unreadCount = 0;

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement stmt = con.prepareStatement(listSql)) {
                stmt.setInt(1, myId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("Notification_ID", rs.getInt("Notification_ID"));
                        row.put("Message", rs.getString("Message"));
                        row.put("Type", rs.getString("Type"));
                        row.put("Is_Read", rs.getBoolean("Is_Read"));
                        row.put("Created_At", rs.getObject("Created_At"));
                        notifications.add(row);
                    }
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(countSql)) {
                stmt.setInt(1, myId);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    unreadCount = rs.getInt("Unread");
                }
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        request.setAttribute("notifications", notifications);
        request.setAttribute("unreadCount", unreadCount);
        request.getRequestDispatcher("notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        int myId = (Integer) session.getAttribute("userId");

        String action = request.getParameter("action");

        try (Connection con = DBConnection.getConnection()) {

            if ("markAll".equals(action)) {

                // Only rows that are still unread need updating.
                String markAll =
                        "Update Notifications " +
                        "Set Is_Read = True " +
                        "Where User_ID = ? " +
                        "  And Is_Read = False";
                try (PreparedStatement stmt = con.prepareStatement(markAll)) {
                    stmt.setInt(1, myId);
                    int changed = stmt.executeUpdate();
                    session.setAttribute("flashSuccess",
                            changed + " notification(s) marked as read.");
                }

            } else {

                int notificationId;
                try {
                    notificationId = Integer.parseInt(request.getParameter("notificationId"));
                } catch (Exception e) {
                    response.sendRedirect("notifications");
                    return;
                }

                // And User_ID = ? is the authorization check. Without it, anyone
                // could mark someone else's notification read by guessing an ID.
                // A row that is not mine simply does not match, so nothing changes.
                String markOne =
                        "Update Notifications " +
                        "Set Is_Read = True " +
                        "Where Notification_ID = ? " +
                        "  And User_ID = ?";
                try (PreparedStatement stmt = con.prepareStatement(markOne)) {
                    stmt.setInt(1, notificationId);
                    stmt.setInt(2, myId);
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            session.setAttribute("flashError", "Could not update notifications: " + e.getMessage());
        }

        response.sendRedirect("notifications");
    }
}