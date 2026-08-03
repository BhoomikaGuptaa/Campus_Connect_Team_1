package servlets;

import db.DBConnection;

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

/**
 * FR6 — sending, accepting, and declining connection requests.
 *
 * Every query uses only Select / From / Where, Insert, and Update. The work that
 * would normally need a fancier query is done in Java instead.
 */
@WebServlet("/connect")
public class ConnectionServlet extends HttpServlet {

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

        try {
            if ("accept".equals(action)) {
                respond(myId, intParam(request, "connectionId"), "accepted");
                session.setAttribute("flashSuccess", "Connection accepted.");
            } else if ("decline".equals(action)) {
                respond(myId, intParam(request, "connectionId"), "declined");
                session.setAttribute("flashSuccess", "Connection declined.");
            } else {
                send(myId,
                     intParam(request, "receiverId"),
                     intParam(request, "eventId"),
                     request.getParameter("message"),
                     request.getParameter("purpose"),
                     session);
            }
        } catch (SQLException e) {
            session.setAttribute("flashError", "Could not update the request: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        }

        String back = request.getParameter("returnTo");
        if ("connections".equals(back)) {
            response.sendRedirect("connections");
        } else {
            response.sendRedirect("attendees?eventId=" + request.getParameter("eventId"));
        }
    }

    /**
     * Create a request, or revive one that was previously declined.
     */
    private void send(int senderId, int receiverId, int eventId,
                      String message, String purpose, HttpSession session)
            throws SQLException {

        // The Check constraint we left out of the schema, written in Java instead.
        if (senderId == receiverId) {
            throw new IllegalArgumentException("You cannot send a request to yourself.");
        }

        if (purpose == null || purpose.trim().isEmpty()) {
            purpose = "Networking";
        }
        if (message != null && message.length() > 500) {
            throw new IllegalArgumentException("Message must be 500 characters or fewer.");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {

                // FR6: both students must be attending the same event. The In
                // operator checks whether Student_ID is a member of the pair, so
                // a count of 2 means both are registered.
                String checkAttending =
                        "Select Count(*) " +
                        "From Signups " +
                        "Where Event_ID = ? " +
                        "  And Student_ID In (?, ?) " +
                        "  And Status = 'registered'";
                try (PreparedStatement stmt = con.prepareStatement(checkAttending)) {
                    stmt.setInt(1, eventId);
                    stmt.setInt(2, senderId);
                    stmt.setInt(3, receiverId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) < 2) {
                            con.rollback();
                            throw new IllegalArgumentException(
                                    "You can only connect with students registered for this event.");
                        }
                    }
                }

                // Look for an existing request in EITHER direction. The Unique key
                // only catches one direction and ignores a null Event_ID, so this
                // check is what actually stops duplicates.
                Integer existingId = null;
                String existingStatus = null;
                String findExisting =
                        "Select Connection_ID, Status " +
                        "From Connections " +
                        "Where Event_ID = ? " +
                        "  And ( (Sender_ID = ? And Receiver_ID = ?) " +
                        "     Or (Sender_ID = ? And Receiver_ID = ?) )";
                try (PreparedStatement stmt = con.prepareStatement(findExisting)) {
                    stmt.setInt(1, eventId);
                    stmt.setInt(2, senderId);
                    stmt.setInt(3, receiverId);
                    stmt.setInt(4, receiverId);
                    stmt.setInt(5, senderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            existingId = rs.getInt("Connection_ID");
                            existingStatus = rs.getString("Status");
                        }
                    }
                }

                if ("pending".equals(existingStatus)) {
                    con.rollback();
                    throw new IllegalArgumentException("There is already a pending request with this student.");
                }
                if ("accepted".equals(existingStatus)) {
                    con.rollback();
                    throw new IllegalArgumentException("You are already connected with this student.");
                }

                if (existingId != null) {
                    // Previously declined — set it back to pending rather than
                    // inserting, since the Unique key would block a new row.
                    String revive =
                            "Update Connections " +
                            "Set Sender_ID = ?, Receiver_ID = ?, Status = 'pending', " +
                            "    Message = ?, Purpose = ?, Created_At = Current_timestamp " +
                            "Where Connection_ID = ?";
                    try (PreparedStatement stmt = con.prepareStatement(revive)) {
                        stmt.setInt(1, senderId);
                        stmt.setInt(2, receiverId);
                        setNullable(stmt, 3, message);
                        stmt.setString(4, purpose);
                        stmt.setInt(5, existingId);
                        stmt.executeUpdate();
                    }
                } else {
                    // Status and Created_At are left out on purpose — the table's
                    // Default fills in 'pending' and the current time.
                    String insert =
                            "Insert into Connections (Sender_ID, Receiver_ID, Event_ID, Message, Purpose) " +
                            "Values (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = con.prepareStatement(insert)) {
                        stmt.setInt(1, senderId);
                        stmt.setInt(2, receiverId);
                        stmt.setInt(3, eventId);
                        setNullable(stmt, 4, message);
                        stmt.setString(5, purpose);
                        stmt.executeUpdate();
                    }
                }

                notify(con, receiverId,
                       "You received a new connection request.",
                       "Connection_Received");

                con.commit();
                session.setAttribute("flashSuccess", "Connection request sent.");

            } catch (SQLException | IllegalArgumentException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Accept or decline. Only the RECEIVER may respond, and only while the request
     * is still pending. Both rules are conditions in the Where clause, so the
     * database enforces them instead of trusting the form.
     */
    private void respond(int myId, int connectionId, String newStatus) throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {

                Integer senderId = null;
                String findSender =
                        "Select Sender_ID " +
                        "From Connections " +
                        "Where Connection_ID = ? " +
                        "  And Receiver_ID = ? " +
                        "  And Status = 'pending'";
                try (PreparedStatement stmt = con.prepareStatement(findSender)) {
                    stmt.setInt(1, connectionId);
                    stmt.setInt(2, myId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            senderId = rs.getInt(1);
                        }
                    }
                }

                // No row came back, so either I am not the receiver or it was
                // already answered. Either way there is nothing to update.
                if (senderId == null) {
                    con.rollback();
                    throw new IllegalArgumentException("That request is no longer waiting for a response.");
                }

                String update =
                        "Update Connections " +
                        "Set Status = ? " +
                        "Where Connection_ID = ?";
                try (PreparedStatement stmt = con.prepareStatement(update)) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, connectionId);
                    stmt.executeUpdate();
                }

                // FR9 only calls for a notification when a request is accepted.
                if ("accepted".equals(newStatus)) {
                    notify(con, senderId,
                           "Your connection request was accepted.",
                           "Connection_Accepted");
                }

                con.commit();

            } catch (SQLException | IllegalArgumentException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * FR9 — inserts on the SAME connection as the caller, so the notification and
     * the connection change commit or roll back together.
     */
    private void notify(Connection con, int userId, String message, String type) throws SQLException {
        String sql =
                "Insert into Notifications (User_ID, Message, Type) " +
                "Values (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.setString(3, type);
            stmt.executeUpdate();
        }
    }

    private void setNullable(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }

    private int intParam(HttpServletRequest request, String name) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong with that request.");
        }
    }
}