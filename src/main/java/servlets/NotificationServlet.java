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
 *
 * WORKED EXAMPLE used in the comments below.
 *   Brandon (User_ID 1) clicks Notifications in the nav.
 *   The Notifications table currently holds:
 *
 *     ID  User_ID  Message                                 Type                  Is_Read
 *     2   1        Your connection request was accepted.   Connection_Accepted   1
 *     5   1        Your connection request was accepted.   Connection_Accepted   0
 *     6   5        You received a new connection request.  Connection_Received   0
 *
 *   Rows 2 and 5 belong to Brandon. Row 6 belongs to Maria and must never
 *   appear on his page.
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    /**
     * doGet runs when the browser requests /notifications — clicking the nav
     * link, or the redirect at the end of doPost.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // EXAMPLE: myId = 1 (Brandon)
        int myId = (Integer) session.getAttribute("userId");

        // Newest first, so the most recent activity is at the top of the page.
        //
        // Where User_ID = ? is doing two jobs at once here: it picks the right
        // notifications AND it is the privacy boundary. There is no way to see
        // another user's rows, because the query never asks for them.
        //
        // EXAMPLE returns rows 5 then 2 — row 5 is newer, and Desc puts the
        // newest first. Row 6 is Maria's, so it is excluded.
        String listSql =
                "Select Notification_ID, Message, Type, Is_Read, Created_At " +
                "From Notifications " +
                "Where User_ID = ? " +
                "Order by Created_At Desc";

        // The unread count shown next to the page heading. This is the query
        // idx_unread was created for.
        //
        // Count(*) counts the rows in the result. Is_Read = False keeps only
        // the unread ones. MySQL stores Boolean as 0 or 1, so False is 0.
        //
        // EXAMPLE: of Brandon's two rows, only row 5 is unread -> 1
        String countSql =
                "Select Count(*) As Unread " +
                "From Notifications " +
                "Where User_ID = ? " +
                "  And Is_Read = False";

        // Same List-of-Maps shape as AttendeesServlet. One Map per notification.
        List<Map<String, Object>> notifications = new ArrayList<>();
        int unreadCount = 0;

        try (Connection con = DBConnection.getConnection()) {

            // ---- Query 1: the list --------------------------------------
            try (PreparedStatement stmt = con.prepareStatement(listSql)) {
                stmt.setInt(1, myId);   // slot 1 <- 1

                try (ResultSet rs = stmt.executeQuery()) {
                    // while, because there may be many rows.
                    // EXAMPLE: this loop runs twice.
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();

                        // getBoolean converts MySQL's 0/1 into Java false/true,
                        // so the JSP can use it directly in an if.
                        //
                        // EXAMPLE pass 1: { Notification_ID=5, Message="Your
                        //   connection request was accepted.",
                        //   Type="Connection_Accepted", Is_Read=false, ... }
                        // EXAMPLE pass 2: same but Notification_ID=2,
                        //   Is_Read=true
                        row.put("Notification_ID", rs.getInt("Notification_ID"));
                        row.put("Message", rs.getString("Message"));
                        row.put("Type", rs.getString("Type"));
                        row.put("Is_Read", rs.getBoolean("Is_Read"));
                        row.put("Created_At", rs.getObject("Created_At"));

                        notifications.add(row);
                    }
                }
            }
            // EXAMPLE: notifications = [ row 5 map, row 2 map ]

            // ---- Query 2: the unread count ------------------------------
            try (PreparedStatement stmt = con.prepareStatement(countSql)) {
                stmt.setInt(1, myId);   // slot 1 <- 1

                try (ResultSet rs = stmt.executeQuery()) {
                    // Count(*) always returns exactly one row, so no while
                    // loop — just move onto it. Even with zero notifications
                    // it returns one row containing 0.
                    rs.next();

                    // "Unread" is the alias given by As Unread in the query,
                    // which is why it can be read by that name.
                    // EXAMPLE: unreadCount = 1
                    unreadCount = rs.getInt("Unread");
                }
            }

        } catch (SQLException e) {
            // This page only reads, so a failure means something is genuinely
            // broken. Throwing lets Tomcat show the error page rather than
            // pretending the user has no notifications.
            throw new ServletException(e);
        }

        // Attach both results to the request, then hand off to the JSP.
        // forward runs server-side, so the browser URL stays /notifications.
        request.setAttribute("notifications", notifications);
        request.setAttribute("unreadCount", unreadCount);
        request.getRequestDispatcher("notifications.jsp").forward(request, response);
    }

    /**
     * doPost handles both buttons on the page — the per-row "Mark as read" and
     * the "Mark all as read" in the heading. The action field tells them apart,
     * same pattern as ConnectionServlet.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // EXAMPLE: myId = 1 (Brandon)
        int myId = (Integer) session.getAttribute("userId");

        // "markAll" from the heading button. The per-row form sends no action
        // at all, so anything else falls to the else branch.
        String action = request.getParameter("action");

        // No setAutoCommit here — each branch runs a single Update, and one
        // statement is already atomic on its own. The transaction in
        // ConnectionServlet exists because it writes two tables.
        try (Connection con = DBConnection.getConnection()) {

            if ("markAll".equals(action)) {

                // Only rows that are still unread need updating.
                // And Is_Read = False is not strictly required — setting an
                // already-read row to read changes nothing — but it means
                // executeUpdate returns an accurate count for the flash message.
                //
                // EXAMPLE: Brandon has one unread row, so this updates row 5
                //          and changed = 1.
                String markAll =
                        "Update Notifications " +
                        "Set Is_Read = True " +
                        "Where User_ID = ? " +
                        "  And Is_Read = False";
                try (PreparedStatement stmt = con.prepareStatement(markAll)) {
                    stmt.setInt(1, myId);   // slot 1 <- 1

                    // executeUpdate returns HOW MANY rows changed, unlike
                    // executeQuery which returns rows.
                    int changed = stmt.executeUpdate();

                    session.setAttribute("flashSuccess",
                            changed + " notification(s) marked as read.");
                }

            } else {

                // The per-row form posts notificationId as text, so convert it.
                // A missing or non-numeric value just bounces back to the page.
                // EXAMPLE: Brandon clicks Mark as read on row 5
                //          -> notificationId = 5
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
                //
                // EXAMPLE of the attack it blocks: Brandon posts
                //   notificationId = 6, which belongs to Maria. The Where clause
                //   becomes Notification_ID = 6 And User_ID = 1. Row 6 has
                //   User_ID 5, so nothing matches, zero rows update, and no
                //   error is raised. Silently ignored, which is correct.
                String markOne =
                        "Update Notifications " +
                        "Set Is_Read = True " +
                        "Where Notification_ID = ? " +
                        "  And User_ID = ?";
                try (PreparedStatement stmt = con.prepareStatement(markOne)) {
                    stmt.setInt(1, notificationId);   // slot 1 <- 5
                    stmt.setInt(2, myId);             // slot 2 <- 1
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            session.setAttribute("flashError", "Could not update notifications: " + e.getMessage());
        }

        // Redirect rather than forward. That means the browser makes a fresh
        // GET to /notifications, which re-runs doGet and rebuilds the page with
        // the new counts. It also stops a refresh from resubmitting the form.
        response.sendRedirect("notifications");
    }
}