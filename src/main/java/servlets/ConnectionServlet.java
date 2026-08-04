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
 *
 * WORKED EXAMPLE A — sending, used in the comments below.
 *   Brandon (1) is on /attendees?eventId=1 and clicks Connect on Maria's card.
 *   The form posts:  action="send", receiverId=5, eventId=1,
 *                    purpose="Find a Teammate", message="Want to team up?"
 *   Maria has no existing connection with Brandon.
 *
 * WORKED EXAMPLE B — accepting.
 *   Maria (5) later opens the same page and clicks Accept.
 *   The form posts:  action="accept", connectionId=4, eventId=1
 */
@WebServlet("/connect")
public class ConnectionServlet extends HttpServlet {

    /**
     * doPost runs for every button on attendees.jsp — Connect, Accept, and
     * Decline all post here. The "action" field is what tells them apart.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // getSession(false) means "give me the existing session, do not create
        // one". No session or no userId in it means nobody is logged in.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // EXAMPLE A: myId = 1 (Brandon)
        // EXAMPLE B: myId = 5 (Maria)
        int myId = (Integer) session.getAttribute("userId");

        // EXAMPLE A: action = "send"
        // EXAMPLE B: action = "accept"
        String action = request.getParameter("action");

        try {
            if ("accept".equals(action)) {
                // EXAMPLE B goes here: respond(5, 4, "accepted")
                respond(myId, intParam(request, "connectionId"), "accepted");
                session.setAttribute("flashSuccess", "Connection accepted.");

            } else if ("decline".equals(action)) {
                respond(myId, intParam(request, "connectionId"), "declined");
                session.setAttribute("flashSuccess", "Connection declined.");

            } else {
                // EXAMPLE A goes here:
                //   send(1, 5, 1, "Want to team up?", "Find a Teammate", session)
                send(myId,
                     intParam(request, "receiverId"),
                     intParam(request, "eventId"),
                     request.getParameter("message"),
                     request.getParameter("purpose"),
                     session);
            }

        // Two kinds of failure, both shown to the user as a red flash bar
        // rather than a stack trace.
        } catch (SQLException e) {
            // The database itself failed.
            session.setAttribute("flashError", "Could not update the request: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // A rule was broken — self-connect, not attending, duplicate, etc.
            // getMessage() is the sentence thrown below, so the user sees a
            // useful explanation.
            session.setAttribute("flashError", e.getMessage());
        }

        // Send the browser back where it came from. Note this is a redirect,
        // not a forward — the browser makes a fresh GET, which is why the flash
        // message had to go in the session rather than on the request.
        // EXAMPLE A and B both end at:  attendees?eventId=1
        String back = request.getParameter("returnTo");
        if ("connections".equals(back)) {
            response.sendRedirect("connections");
        } else {
            response.sendRedirect("attendees?eventId=" + request.getParameter("eventId"));
        }
    }

    /**
     * Create a request, or revive one that was previously declined.
     *
     * EXAMPLE A arrives here as:
     *   senderId=1, receiverId=5, eventId=1,
     *   message="Want to team up?", purpose="Find a Teammate"
     */
    private void send(int senderId, int receiverId, int eventId,
                      String message, String purpose, HttpSession session)
            throws SQLException {

        // The Check constraint
        // EXAMPLE A: 1 == 5 is false, so this passes.
        if (senderId == receiverId) {
            throw new IllegalArgumentException("You cannot send a request to yourself.");
        }

        // Purpose is Not null in the table, so give it a value if the form
        // somehow sent nothing.
        if (purpose == null || purpose.trim().isEmpty()) {
            purpose = "Networking";
        }

        // Matches Message Varchar(500). The JSP also sets maxlength=500, but
        // that only binds the browser — a hand-crafted POST could exceed it.
        if (message != null && message.length() > 500) {
            throw new IllegalArgumentException("Message must be 500 characters or fewer.");
        }

        try (Connection con = DBConnection.getConnection()) {

            // Turn off autocommit so everything below is ONE transaction.
            // Without this, each statement would save on its own, and a failure
            // halfway could leave a connection row with no notification.
            con.setAutoCommit(false);
            try {

                // ---- Check 1: are both students actually attending? ----------
                // FR6: both students must be attending the same event. The In
                // operator checks whether Student_ID is a member of the pair, so
                // a count of 2 means both are registered.
                //
                // EXAMPLE A: Event 1 has Brandon and Maria both registered,
                //            so Count(*) comes back as 2 and this passes.
                //            If Maria had cancelled, it would be 1 and throw.
                String checkAttending =
                        "Select Count(*) " +
                        "From Signups " +
                        "Where Event_ID = ? " +
                        "  And Student_ID In (?, ?) " +
                        "  And Status = 'registered'";
                try (PreparedStatement stmt = con.prepareStatement(checkAttending)) {
                    stmt.setInt(1, eventId);      // slot 1 <- 1
                    stmt.setInt(2, senderId);     // slot 2 <- 1
                    stmt.setInt(3, receiverId);   // slot 3 <- 5
                    try (ResultSet rs = stmt.executeQuery()) {
                        // Count(*) always returns exactly one row, so no while
                        // loop — just move to it.
                        rs.next();
                        // getInt(1) reads column 1 of the result, the count.
                        if (rs.getInt(1) < 2) {
                            con.rollback();
                            throw new IllegalArgumentException(
                                    "You can only connect with students registered for this event.");
                        }
                    }
                }

                // ---- Check 2: does a connection already exist? ---------------
                // Look for an existing request in EITHER direction. The Unique key
                // only catches one direction and ignores a null Event_ID, so this
                // check is what actually stops duplicates.
                //
                // Slots 2 and 3 ask "did I send to them". Slots 4 and 5 are the
                // same two IDs swapped, asking "did they send to me". To the
                // database those are different rows; to a person they are the
                // same connection.
                //
                // EXAMPLE A: no row matches either ordering, so both variables
                //            stay null.
                Integer existingId = null;
                String existingStatus = null;
                String findExisting =
                        "Select Connection_ID, Status " +
                        "From Connections " +
                        "Where Event_ID = ? " +
                        "  And ( (Sender_ID = ? And Receiver_ID = ?) " +
                        "     Or (Sender_ID = ? And Receiver_ID = ?) )";
                try (PreparedStatement stmt = con.prepareStatement(findExisting)) {
                    stmt.setInt(1, eventId);      // slot 1 <- 1
                    stmt.setInt(2, senderId);     // slot 2 <- 1
                    stmt.setInt(3, receiverId);   // slot 3 <- 5
                    stmt.setInt(4, receiverId);   // slot 4 <- 5
                    stmt.setInt(5, senderId);     // slot 5 <- 1
                    try (ResultSet rs = stmt.executeQuery()) {
                        // if, not while — at most one connection can exist per
                        // pair per event.
                        if (rs.next()) {
                            existingId = rs.getInt("Connection_ID");
                            existingStatus = rs.getString("Status");
                        }
                    }
                }

                // Both of these are skipped in EXAMPLE A because existingStatus
                // is null. They fire on a second click of the same button, or
                // when the other person already asked you.
                if ("pending".equals(existingStatus)) {
                    con.rollback();
                    throw new IllegalArgumentException("There is already a pending request with this student.");
                }
                if ("accepted".equals(existingStatus)) {
                    con.rollback();
                    throw new IllegalArgumentException("You are already connected with this student.");
                }

                // ---- Write: either revive a declined row, or insert a new one --
                if (existingId != null) {
                	//basically the ask me again clause 
                    // Only reachable when existingStatus is 'declined', since
                    // pending and accepted already threw above.
                    //
                    // Previously declined — set it back to pending rather than
                    // inserting, since the Unique key would block a new row.
                    // Sender and Receiver are reassigned because the person
                    // asking again may be the one who was originally declined.
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
                        stmt.executeUpdate();   // executeUpdate, not executeQuery
                    }
                } else {
                    // EXAMPLE A takes this branch.
                    //
                    // Status and Created_At are left out on purpose — the table's
                    // Default fills in 'pending' and the current time. Connection_ID
                    // is left out too, because it is Auto_increment.
                    //
                    // Resulting row:  (4, 1, 5, 1, 'pending',
                    //                  'Want to team up?', 'Find a Teammate', now)
                    String insert =
                            "Insert into Connections (Sender_ID, Receiver_ID, Event_ID, Message, Purpose) " +
                            "Values (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = con.prepareStatement(insert)) {
                        stmt.setInt(1, senderId);      // 1
                        stmt.setInt(2, receiverId);    // 5
                        stmt.setInt(3, eventId);       // 1
                        setNullable(stmt, 4, message); // "Want to team up?"
                        stmt.setString(5, purpose);    // "Find a Teammate"
                        stmt.executeUpdate();
                    }
                }

                // ---- FR9: tell the receiver -----------------------------------
                // EXAMPLE A: inserts a notification row for Maria (5).
                notify(con, receiverId,
                       "You received a new connection request.",
                       "Connection_Received");

                // Nothing above was saved until this line. commit() makes the
                // insert AND the notification permanent together.
                con.commit();
                session.setAttribute("flashSuccess", "Connection request sent.");

            } catch (SQLException | IllegalArgumentException e) {
                // Any failure undoes everything in this transaction, so you can
                // never end up with a notification about a connection that was
                // never created. Then rethrow so doPost can show the message.
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Accept or decline. Only the RECEIVER may respond, and only while the request
     * is still pending. Both rules are conditions in the Where clause, so the
     * database enforces them instead of trusting the form.
     *
     * EXAMPLE B arrives here as: myId=5, connectionId=4, newStatus="accepted"
     */
    private void respond(int myId, int connectionId, String newStatus) throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {

                // Three rules in one Where clause:
                //   the connection exists, I am the receiver, it is still pending.
                // If any fails, no row comes back. This is the authorization
                // check — connectionId came from a hidden form field, which a
                // user could edit, so it is never trusted on its own.
                //
                // EXAMPLE B: row 4 has Receiver_ID 5 and Status 'pending',
                //            so this returns Sender_ID = 1.
                Integer senderId = null;
                String findSender =
                        "Select Sender_ID " +
                        "From Connections " +
                        "Where Connection_ID = ? " +
                        "  And Receiver_ID = ? " +
                        "  And Status = 'pending'";
                try (PreparedStatement stmt = con.prepareStatement(findSender)) {
                    stmt.setInt(1, connectionId);   // slot 1 <- 4
                    stmt.setInt(2, myId);           // slot 2 <- 5
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // getInt(1) = column 1 of the Select, Sender_ID.
                            senderId = rs.getInt(1);
                        }
                    }
                }

                // No row came back, so either I am not the receiver or it was
                // already answered. Either way there is nothing to update.
                // Brandon clicking Accept on his own request lands here.
                if (senderId == null) {
                    con.rollback();
                    throw new IllegalArgumentException("That request is no longer waiting for a response.");
                }

                // EXAMPLE B: row 4 becomes Status = 'accepted'.
                String update =
                        "Update Connections " +
                        "Set Status = ? " +
                        "Where Connection_ID = ?";
                try (PreparedStatement stmt = con.prepareStatement(update)) {
                    stmt.setString(1, newStatus);   // "accepted"
                    stmt.setInt(2, connectionId);   // 4
                    stmt.executeUpdate();
                }

                // FR9 only calls for a notification when a request is accepted.
                // A decline is deliberately silent — no point telling someone
                // they were turned down.
                //
                // EXAMPLE B: notifies Brandon (1), the sender we just looked up.
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
     *
     * Notice the Connection is a parameter rather than opened here. That is what
     * puts this insert inside the caller's transaction. If it opened its own
     * connection, the notification would save even when the connection change
     * rolled back.
     *
     * Notification_ID, Is_Read, and Created_At are left out — Auto_increment and
     * the table Defaults fill them in.
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

    /**
     * Message is a nullable column, and an empty text box arrives as "" rather
     * than null. This stores a real SQL null instead of an empty string, so
     * "no message" is one thing in the database rather than two.
     *
     * setNull needs the column type, which is why Types.VARCHAR is passed.
     */
    private void setNullable(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.trim());
        }
    }

    /**
     * Form fields always arrive as text. This converts one to an int and turns
     * a missing or non-numeric value into a friendly error rather than a crash.
     *
     * EXAMPLE A: getParameter("receiverId") returns the String "5",
     *            parseInt turns it into the number 5.
     */
    private int intParam(HttpServletRequest request, String name) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong with that request.");
        }
    }
}