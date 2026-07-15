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
import java.sql.Types;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"student".equals(role)) {
            response.sendRedirect("index.jsp");
            return;
        }

        int studentId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        String eventIdParam = request.getParameter("eventId");

        int eventId;
        try {
            eventId = Integer.parseInt(eventIdParam);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendRedirect("index.jsp");
            return;
        }

        if ("cancel".equals(action)) {
            cancelSignup(studentId, eventId);
        } else {
            signUp(studentId, eventId);
        }

        response.sendRedirect("index.jsp");
    }

    /**
     * Logic 1: reject duplicate signups.
     * Logic 2: register if capacity allows, otherwise waitlist with the next position.
     *
     * IMPORTANT: Signups has UNIQUE(Student_ID, Event_ID) — a student can only ever
     * have ONE row for a given event, no matter the status. So if they previously
     * cancelled (a 'cancelled' row already exists), we must UPDATE that row instead
     * of INSERTing a new one, or we'd violate the unique constraint.
     */
    private void signUp(int studentId, int eventId) {
        String findExistingSql =
                "SELECT Signup_ID, Status FROM Signups WHERE Student_ID = ? AND Event_ID = ?";

        String capacitySql =
                "SELECT e.Capacity, " +
                        "       (SELECT COUNT(*) FROM Signups s WHERE s.Event_ID = e.Event_ID AND s.Status = 'registered') AS registered_count " +
                        "FROM Events e WHERE e.Event_ID = ?";

        String maxWaitlistSql =
                "SELECT COALESCE(MAX(Waitlist_Position), 0) AS max_pos FROM Signups " +
                        "WHERE Event_ID = ? AND Status = 'waitlisted'";

        String insertSql =
                "INSERT INTO Signups (Student_ID, Event_ID, Status, Waitlist_Position) VALUES (?, ?, ?, ?)";

        String reactivateSql =
                "UPDATE Signups SET Status = ?, Waitlist_Position = ?, Signed_Up_At = CURRENT_TIMESTAMP " +
                        "WHERE Signup_ID = ?";

        try (Connection con = DBConnection.getConnection()) {

            // Check whether ANY row already exists for this (student, event) pair —
            // active or cancelled — since the table only allows one row per pair.
            Integer existingSignupId = null;
            String existingStatus = null;

            try (PreparedStatement findStmt = con.prepareStatement(findExistingSql)) {
                findStmt.setInt(1, studentId);
                findStmt.setInt(2, eventId);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        existingSignupId = rs.getInt("Signup_ID");
                        existingStatus = rs.getString("Status");
                    }
                }
            }

            // Logic 1: reject duplicate signup if already active
            if ("registered".equals(existingStatus) || "waitlisted".equals(existingStatus)) {
                return;
            }

            // Logic 2: check capacity
            int capacity;
            int registeredCount;
            try (PreparedStatement capStmt = con.prepareStatement(capacitySql)) {
                capStmt.setInt(1, eventId);
                try (ResultSet rs = capStmt.executeQuery()) {
                    if (!rs.next()) {
                        return; // event doesn't exist
                    }
                    capacity = rs.getInt("Capacity");
                    registeredCount = rs.getInt("registered_count");
                }
            }

            String status;
            Integer waitlistPosition = null;

            if (registeredCount < capacity) {
                status = "registered";
            } else {
                status = "waitlisted";
                try (PreparedStatement maxStmt = con.prepareStatement(maxWaitlistSql)) {
                    maxStmt.setInt(1, eventId);
                    try (ResultSet rs = maxStmt.executeQuery()) {
                        rs.next();
                        waitlistPosition = rs.getInt("max_pos") + 1;
                    }
                }
            }

            if (existingSignupId != null) {
                // A 'cancelled' row already exists for this pair — reactivate it
                try (PreparedStatement reactivateStmt = con.prepareStatement(reactivateSql)) {
                    reactivateStmt.setString(1, status);
                    if (waitlistPosition != null) {
                        reactivateStmt.setInt(2, waitlistPosition);
                    } else {
                        reactivateStmt.setNull(2, Types.INTEGER);
                    }
                    reactivateStmt.setInt(3, existingSignupId);
                    reactivateStmt.executeUpdate();
                }
            } else {
                // First time this student has interacted with this event — insert fresh
                try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, studentId);
                    insertStmt.setInt(2, eventId);
                    insertStmt.setString(3, status);
                    if (waitlistPosition != null) {
                        insertStmt.setInt(4, waitlistPosition);
                    } else {
                        insertStmt.setNull(4, Types.INTEGER);
                    }
                    insertStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            // Swallow for the demo
        }
    }

    /**
     * Logic 3: cancel a signup.
     * If the cancelled signup was 'registered', automatically promote the first
     * person on the waitlist. Either way, renumber the remaining waitlist so
     * positions stay contiguous.
     */
    private void cancelSignup(int studentId, int eventId) {
        String findSignupSql =
                "SELECT Signup_ID, Status, Waitlist_Position FROM Signups " +
                        "WHERE Student_ID = ? AND Event_ID = ? AND Status IN ('registered', 'waitlisted')";

        String cancelSql =
                "UPDATE Signups SET Status = 'cancelled', Waitlist_Position = NULL WHERE Signup_ID = ?";

        String nextWaitlistedSql =
                "SELECT Signup_ID FROM Signups WHERE Event_ID = ? AND Status = 'waitlisted' " +
                        "ORDER BY Waitlist_Position ASC LIMIT 1";

        String promoteSql =
                "UPDATE Signups SET Status = 'registered', Waitlist_Position = NULL WHERE Signup_ID = ?";

        String renumberAllSql =
                "UPDATE Signups SET Waitlist_Position = Waitlist_Position - 1 " +
                        "WHERE Event_ID = ? AND Status = 'waitlisted'";

        String renumberAfterSql =
                "UPDATE Signups SET Waitlist_Position = Waitlist_Position - 1 " +
                        "WHERE Event_ID = ? AND Status = 'waitlisted' AND Waitlist_Position > ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            int signupId;
            String currentStatus;
            Integer currentWaitlistPos;

            try (PreparedStatement findStmt = con.prepareStatement(findSignupSql)) {
                findStmt.setInt(1, studentId);
                findStmt.setInt(2, eventId);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return;
                    }
                    signupId = rs.getInt("Signup_ID");
                    currentStatus = rs.getString("Status");
                    currentWaitlistPos = (Integer) rs.getObject("Waitlist_Position");
                }
            }

            try (PreparedStatement cancelStmt = con.prepareStatement(cancelSql)) {
                cancelStmt.setInt(1, signupId);
                cancelStmt.executeUpdate();
            }

            if ("registered".equals(currentStatus)) {
                Integer nextSignupId = null;
                try (PreparedStatement nextStmt = con.prepareStatement(nextWaitlistedSql)) {
                    nextStmt.setInt(1, eventId);
                    try (ResultSet rs = nextStmt.executeQuery()) {
                        if (rs.next()) {
                            nextSignupId = rs.getInt("Signup_ID");
                        }
                    }
                }
                if (nextSignupId != null) {
                    try (PreparedStatement promoteStmt = con.prepareStatement(promoteSql)) {
                        promoteStmt.setInt(1, nextSignupId);
                        promoteStmt.executeUpdate();
                    }
                    try (PreparedStatement renumberStmt = con.prepareStatement(renumberAllSql)) {
                        renumberStmt.setInt(1, eventId);
                        renumberStmt.executeUpdate();
                    }
                }
            } else if ("waitlisted".equals(currentStatus) && currentWaitlistPos != null) {
                try (PreparedStatement renumberStmt = con.prepareStatement(renumberAfterSql)) {
                    renumberStmt.setInt(1, eventId);
                    renumberStmt.setInt(2, currentWaitlistPos);
                    renumberStmt.executeUpdate();
                }
            }

            con.commit();

        } catch (SQLException e) {
            // Swallow for the demo
        }
    }
}