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
 * Three separate queries instead of one big join. Each one only uses Select,
 * From, Where, and Order by, so every line is explainable.
 */
@WebServlet("/attendees")
public class AttendeesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        int myId = (Integer) session.getAttribute("userId");

        int eventId;
        try {
            eventId = Integer.parseInt(request.getParameter("eventId"));
        } catch (Exception e) {
            response.sendRedirect("events.jsp");
            return;
        }

        // Query 1 — the event itself, so the page has a title to show.
        String eventSql =
                "Select Title, Event_Date, Location " +
                "From Events " +
                "Where Event_ID = ?";

        // Query 2 — everyone registered for this event, except me.
        // Three relations listed in the From clause with tuple variables u, s, sg,
        // and the join conditions written in the Where clause.
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
        String connectionSql =
                "Select Connection_ID, Sender_ID, Receiver_ID, Status " +
                "From Connections " +
                "Where Event_ID = ? " +
                "  And (Sender_ID = ? Or Receiver_ID = ?)";

        List<Map<String, Object>> people = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement stmt = con.prepareStatement(eventSql)) {
                stmt.setInt(1, eventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        response.sendRedirect("events.jsp");
                        return;
                    }
                    request.setAttribute("eventTitle", rs.getString("Title"));
                    request.setAttribute("eventDate", rs.getObject("Event_Date"));
                    request.setAttribute("eventLocation", rs.getString("Location"));
                }
            }

            // Read the connections first and store them in a Map keyed by the
            // OTHER student's User_ID. Then looking up a person's status while
            // building the list below is a simple map lookup.
            Map<Integer, Map<String, Object>> myConnections = new HashMap<>();
            try (PreparedStatement stmt = con.prepareStatement(connectionSql)) {
                stmt.setInt(1, eventId);
                stmt.setInt(2, myId);
                stmt.setInt(3, myId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int senderId = rs.getInt("Sender_ID");
                        int receiverId = rs.getInt("Receiver_ID");

                        // Whichever of the two IDs is not mine is the other person.
                        int otherId = (senderId == myId) ? receiverId : senderId;

                        Map<String, Object> info = new HashMap<>();
                        info.put("Connection_ID", rs.getInt("Connection_ID"));
                        info.put("Status", rs.getString("Status"));
                        info.put("TheySent", senderId != myId);
                        myConnections.put(otherId, info);
                    }
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(peopleSql)) {
                stmt.setInt(1, eventId);
                stmt.setInt(2, myId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int userId = rs.getInt("User_ID");

                        Map<String, Object> row = new HashMap<>();
                        row.put("User_ID", userId);
                        row.put("First_Name", rs.getString("First_Name"));
                        row.put("Last_Name", rs.getString("Last_Name"));
                        row.put("Major", rs.getString("Major"));
                        row.put("Grad_Year", rs.getObject("Grad_Year"));
                        row.put("Bio", rs.getString("Bio"));

                        // Null status is what tells the JSP to show a Connect form.
                        Map<String, Object> info = myConnections.get(userId);
                        if (info == null) {
                            row.put("Status", null);
                            row.put("TheySent", Boolean.FALSE);
                            row.put("Connection_ID", null);
                        } else {
                            row.put("Status", info.get("Status"));
                            row.put("TheySent", info.get("TheySent"));
                            row.put("Connection_ID", info.get("Connection_ID"));
                        }

                        people.add(row);
                    }
                }
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        request.setAttribute("eventId", eventId);
        request.setAttribute("attendees", people);
        request.getRequestDispatcher("attendees.jsp").forward(request, response);
    }
}