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
 * FR6 — shows everyone registered for one event so a student can send them a
 * connection request. This page is where Event_ID comes from: FR6 says a student
 * connects with someone attending the SAME event, and people.jsp has no event.
 *
 * Three separate queries instead of one big join. 
 *
 * ---------------------------------------------------------------------------
 * WORKED EXAMPLE — the values used in the comments below
 * ---------------------------------------------------------------------------
 * Logged in as Brandon (User_ID 1), opening /attendees?eventId=1
 *
 * Users            Students                     Signups (event 1)
 *   1 Brandon Phan   1 Computer Science 2027       1  registered
 *   2 Frank Lin      2 Data Science 2027           2  registered
 *   5 Maria Chen     5 Software Engineering 2026   5  registered
 *
 * Connections
 *   Connection_ID 3 | Sender 1 | Receiver 2 | Event 1 | accepted
 *
 * So Brandon is already connected with Frank, and Maria is a stranger.
 * Those two people take opposite paths through the code below, which is the
 * whole point of the example.
 * ---------------------------------------------------------------------------
 */
@WebServlet("/attendees")
public class AttendeesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session check, give me the existing session
        // if theres no session or no userId in it; nobody should be logged in and go back to login
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        int myId = (Integer) session.getAttribute("userId");
        // EXAMPLE: myId = 1

        int eventId;
        try {
            // getParameter reads ? eventId=1 from the URL and returns the TEXT "1",
            // so parseInt turns it into a number
            eventId = Integer.parseInt(request.getParameter("eventId"));
            // EXAMPLE: eventId = 1
        } catch (Exception e) {
            response.sendRedirect("events.jsp");
            return;
        }

        // Query 1 — the event itself, so the page has a title to show.
        // the three columns needed for the page heading
        // ? is the place holder for stmt.setInt(1,eventId)
        //
        // EXAMPLE result: Title = "VSA General Meeting"
        //                 Location = "Clark Hall 100"
        //                 Event_Date = 2026-07-28 17:00
        String eventSql =
                "Select Title, Event_Date, Location " +
                "From Events " +
                "Where Event_ID = ?";

        // Query 2 — everyone registered for this event, except me.
        // Three relations listed in the From clause with tuple variables u, s, sg,
        // and the join conditions written in the Where clause.
        
        // u.User_ID = s.User_ID  the first join condition to link a user to their student record
        // s.User_ID = sg.Student_ID second join condition to like a student to their signups
        // And sg.Event_ID = ?  a second consition to narrow to one event
        // And sg.Status = 'registered  we only want the confirmed registered attendees
        // And u.Is_Active = 1  only for users whos account is still active
        // And u.User_ID != ?  give me every user ID who isnt mine at that specific time
        
        //
        // EXAMPLE: all three students are registered for event 1, but
        //          u.User_ID != 1 drops Brandon, and Order by First_Name gives
        //             row 1 = Frank Lin,  Data Science,         2027
        //             row 2 = Maria Chen, Software Engineering, 2026
        String peopleSql =
                "Select u.User_ID, u.First_Name, u.Last_Name, s.Major, s.Grad_Year, s.Bio " +
                "From Users u, Students s, Signups sg " +
                "Where u.User_ID = s.User_ID " +
                "  And s.User_ID = sg.Student_ID " +
                "  And sg.Event_ID = ? " +
                "  And sg.Status = 'registered' " +
                "  And u.Is_Active = 1 " +
                "  And u.User_ID != ? " +
                "Order by u.First_Name, u.Last_Name";

        // Query 3 — every connection for this event that involves me, in either
        // direction. Matching each connection to the right person happens in Java
        // below rather than in SQL.
        
        // "Where Event_ID = ? "   means for this events connections
        // And (Sender_ID = ? Or Receiver_ID = ?)"; // basically asking whether or not you are the sender or receiver
        // a row will come back and java will figure out this : int otherId = (senderId == myId) ? receiverId : senderId;
    
        // would read it as (Event_ID = ? And Sender_ID = ?) Or (Receiver_ID = ?)
        // and return connections from other events too.
        //
        // EXAMPLE: only Connection_ID 3 matches — event is 1 and Brandon is the sender
        String connectionSql =
                "Select Connection_ID, Sender_ID, Receiver_ID, Status " +
                "From Connections " +
                "Where Event_ID = ? " +
                "  And (Sender_ID = ? Or Receiver_ID = ?)";

        // The finished list handed to the JSP. One Map per attendee, where the
        // String key is a column name and the Object value is that column's data.
        // Object rather than String because the values are a mix of Integer,
        // String and Boolean.
        List<Map<String, Object>> people = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {

            // ---------- Query 1 runs ----------
            try (PreparedStatement stmt = con.prepareStatement(eventSql)) {
                stmt.setInt(1, eventId);                    // EXAMPLE: ? = 1
                try (ResultSet rs = stmt.executeQuery()) {

                    // The cursor starts BEFORE the first row, so next() moves to it
                    // and returns false when there is nothing there. No row means
                    // the event id does not exist.
                    if (!rs.next()) {
                        response.sendRedirect("events.jsp");
                        return;
                    }
                    request.setAttribute("eventTitle", rs.getString("Title"));
                    request.setAttribute("eventDate", rs.getObject("Event_Date"));
                    request.setAttribute("eventLocation", rs.getString("Location"));
                }
            }

            // ---------- Query 3 runs ----------
            // Read the connections first and store them in a Map keyed by the
            // OTHER student's User_ID. Then looking up a person's status while
            // building the list below is a simple map lookup. 1 2 and 3 just tells what slots to fill
            Map<Integer, Map<String, Object>> myConnections = new HashMap<>();
            try (PreparedStatement stmt = con.prepareStatement(connectionSql)) {
                stmt.setInt(1, eventId);                    // EXAMPLE: ? = 1
                stmt.setInt(2, myId);                       // EXAMPLE: ? = 1
                stmt.setInt(3, myId);                       // EXAMPLE: ? = 1
                try (ResultSet rs = stmt.executeQuery()) {

                    // while, not if, because there can be several connections
                    // EXAMPLE: this loop runs once, for Connection_ID 3
                    while (rs.next()) {
                        int senderId = rs.getInt("Sender_ID");      // EXAMPLE: 1
                        int receiverId = rs.getInt("Receiver_ID");  // EXAMPLE: 2

                        // Whichever of the two IDs is not mine is the other person.
                        // Read the ? as "then" and the : as "else":
                        //   is senderId the same as myId? then receiverId, else senderId
                        // EXAMPLE: 1 == 1 is true, so otherId = 2 (Frank)
                        int otherId = (senderId == myId) ? receiverId : senderId;

                        Map<String, Object> info = new HashMap<>();
                        info.put("Connection_ID", rs.getInt("Connection_ID")); // EXAMPLE: 3
                        info.put("Status", rs.getString("Status"));            // EXAMPLE: "accepted"
                        info.put("TheySent", senderId != myId);                // EXAMPLE: 1 != 1 -> false
                        myConnections.put(otherId, info);

                        // EXAMPLE after this loop finishes:
                        //   myConnections = { 2 -> { Connection_ID=3,
                        //                            Status="accepted",
                        //                            TheySent=false } }
                        // Maria (5) is not in here at all.
                        // but if Frank loads the page myId = 2  and then map key would be 1 
                    }
                }
            }

            // ---------- Query 2 runs ----------
            try (PreparedStatement stmt = con.prepareStatement(peopleSql)) {
                stmt.setInt(1, eventId);                    // EXAMPLE: ? = 1
                stmt.setInt(2, myId);                       // EXAMPLE: ? = 1
                try (ResultSet rs = stmt.executeQuery()) {

                    // EXAMPLE: this loop runs twice — once for Frank, once for Maria
                    while (rs.next()) {
                        int userId = rs.getInt("User_ID");
                        // EXAMPLE pass 1: userId = 2 (Frank)
                        // EXAMPLE pass 2: userId = 5 (Maria)

                        Map<String, Object> row = new HashMap<>();
                        row.put("User_ID", userId);
                        row.put("First_Name", rs.getString("First_Name"));
                        row.put("Last_Name", rs.getString("Last_Name"));
                        row.put("Major", rs.getString("Major"));

                        // getObject not getInt because Grad_Year can be null, and
                        // getInt would turn a null into 0 and print "Class of 0"
                        row.put("Grad_Year", rs.getObject("Grad_Year"));
                        row.put("Bio", rs.getString("Bio"));

                        // THE MERGE. Look this person up in the connections map.
                        // get() returns null when the key is not there.
                        // Null status is what tells the JSP to show a Connect form.
                        //
                        // EXAMPLE pass 1: get(2) finds Frank's entry  -> else branch
                        // EXAMPLE pass 2: get(5) returns null         -> if branch
                        Map<String, Object> info = myConnections.get(userId);
                        if (info == null) {
                            row.put("Status", null);
                            row.put("TheySent", Boolean.FALSE);
                            row.put("Connection_ID", null);
                        } else {
                            row.put("Status", info.get("Status"));               // EXAMPLE: "accepted"
                            row.put("TheySent", info.get("TheySent"));           // EXAMPLE: false
                            row.put("Connection_ID", info.get("Connection_ID")); // EXAMPLE: 3
                        }

                        people.add(row);
                    }

                    // EXAMPLE final list:
                    //   people[0] = { User_ID=2, First_Name="Frank", ...,
                    //                 Status="accepted", TheySent=false, Connection_ID=3 }
                    //   people[1] = { User_ID=5, First_Name="Maria", ...,
                    //                 Status=null,       TheySent=false, Connection_ID=null }
                }
            }

        } catch (SQLException e) {
            // A read failure means something is genuinely broken, so let Tomcat
            // show the error page. ConnectionServlet catches instead, because a
            // failed write is usually a user mistake worth a friendly message.
            throw new ServletException(e);
        }

        // Attach the results to the request, then hand control to the JSP.
        // forward happens on the server, so the browser URL stays /attendees
        // and the user never sees attendees.jsp in the address bar.
        //
        // EXAMPLE what the JSP then renders:
        //   Frank — Status "accepted" matches the first branch
        //           -> Connected badge + Message button (messages?connectionId=3)
        //   Maria — Status null fails all three checks
        //           -> falls to the last branch, shows the Connect form
        request.setAttribute("eventId", eventId);
        request.setAttribute("attendees", people);
        request.getRequestDispatcher("attendees.jsp").forward(request, response);
    }
}