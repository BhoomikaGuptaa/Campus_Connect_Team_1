package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import db.DBConnection;

// FR3 — Event Management for Event Organizers.
// This servlet handles four different actions: create, edit, cancel,
// and viewing the attendee list. I split the work by reading an
// "action" parameter.

@WebServlet("/event")
public class EventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Every action here needs the person to be logged in as an
        // organizer, so I check the session first before doing
        // anything else.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int organizerId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                createEvent(request, response, organizerId);
            } else if ("edit".equals(action)) {
                editEvent(request, response, organizerId);
            } else if ("cancel".equals(action)) {
                cancelEvent(request, response, organizerId);
            } else {
                // If the action parameter is missing or doesn't match
                // anything I know how to handle, I treat it as a bad
                // request.
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (SQLException e) {
            // Wrap SQL exceptions in a ServletException so the
            // container's error handling takes over.
            throw new ServletException("Database error in EventServlet.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int organizerId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("attendees".equals(action)) {
                viewAttendees(request, response, organizerId);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in EventServlet.", e);
        }
    }


    // ------------------------------------------------------------
    // Create a new event
    // ------------------------------------------------------------
    private void createEvent(HttpServletRequest request, HttpServletResponse response, int organizerId)
            throws SQLException, ServletException, IOException {

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String location = request.getParameter("location");
        String eventDate = request.getParameter("eventDate");
        int categoryId = Integer.parseInt(request.getParameter("categoryId"));
        int capacity = Integer.parseInt(request.getParameter("capacity"));

        // Capacity has to be a positive number for the event to make
        // sense, so I check it here before it ever reaches the
        // database.
        if (capacity <= 0) {
            request.setAttribute("errorMessage", "Capacity must be greater than zero.");
            request.getRequestDispatcher("create-event.jsp").forward(request, response);
            return;
        }


        String sql = "INSERT INTO Events "
                + "(Organizer_ID, Category_ID, Title, Description, Location, Event_Date, Capacity) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, organizerId);
            stmt.setInt(2, categoryId);
            stmt.setString(3, title);
            stmt.setString(4, description);
            stmt.setString(5, location);
            stmt.setTimestamp(6, Timestamp.valueOf(eventDate));
            stmt.setInt(7, capacity);

            stmt.executeUpdate();
        }

        response.sendRedirect("my-events.jsp");
    }


    // ------------------------------------------------------------
    // Edit an event's details
    // ------------------------------------------------------------
    private void editEvent(HttpServletRequest request, HttpServletResponse response, int organizerId)
            throws SQLException, IOException {

        int eventId = Integer.parseInt(request.getParameter("eventId"));

        // Confirm this organizer actually owns the event they're trying to edit.
        if (!organizerOwnsEvent(eventId, organizerId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this event.");
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String location = request.getParameter("location");
        String eventDate = request.getParameter("eventDate");
        int categoryId = Integer.parseInt(request.getParameter("categoryId"));
        int capacity = Integer.parseInt(request.getParameter("capacity"));

        String sql = "UPDATE Events "
                + "SET Title = ?, Description = ?, Location = ?, Event_Date = ?, Capacity = ?, Category_ID = ? "
                + "WHERE Event_ID = ? AND Organizer_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, location);
            stmt.setTimestamp(4, Timestamp.valueOf(eventDate));
            stmt.setInt(5, capacity);
            stmt.setInt(6, categoryId);
            stmt.setInt(7, eventId);
            stmt.setInt(8, organizerId);

            stmt.executeUpdate();
        }

        response.sendRedirect("my-events.jsp");
    }


    // ------------------------------------------------------------
    // Cancel an event
    // ------------------------------------------------------------
    private void cancelEvent(HttpServletRequest request, HttpServletResponse response, int organizerId)
            throws SQLException, IOException {

        int eventId = Integer.parseInt(request.getParameter("eventId"));

        if (!organizerOwnsEvent(eventId, organizerId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this event.");
            return;
        }

        // Sets Is_Cancelled to TRUE.
        // I want to keep cancelled events around in the database so
        // students who signed up can still see the event was cancelled.
        String sql = "UPDATE Events SET Is_Cancelled = TRUE WHERE Event_ID = ? AND Organizer_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, organizerId);
            stmt.executeUpdate();
        }

        response.sendRedirect("my-events.jsp");
    }


    // ------------------------------------------------------------
    // View the attendee list for one of the organizer's events
    // ------------------------------------------------------------
    private void viewAttendees(HttpServletRequest request, HttpServletResponse response, int organizerId)
            throws SQLException, ServletException, IOException {

        int eventId = Integer.parseInt(request.getParameter("eventId"));

        // Same ownership check applies here. An organizer should only
        // be able to see the attendee list for their own events, not
        // anyone else's.
        if (!organizerOwnsEvent(eventId, organizerId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not own this event.");
            return;
        }


        String sql = "SELECT u.First_Name, u.Last_Name, u.Email, sg.Status, sg.Signed_Up_At "
                + "FROM Events e, Signups sg, Users u "
                + "WHERE e.Event_ID = sg.Event_ID "
                + "  AND sg.Student_ID = u.User_ID "
                + "  AND e.Event_ID = ? "
                + "  AND e.Organizer_ID = ? "
                + "  AND sg.Status = 'registered' "
                + "ORDER BY sg.Signed_Up_At";


        List<Attendee> attendees = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, organizerId);

            try (ResultSet rs = stmt.executeQuery()) {
                // Read every row here and copy each row into an Attendee object.
                while (rs.next()) {
                    Attendee attendee = new Attendee(
                            rs.getString("First_Name"),
                            rs.getString("Last_Name"),
                            rs.getString("Email"),
                            rs.getString("Status"),
                            rs.getTimestamp("Signed_Up_At")
                    );
                    attendees.add(attendee);
                }
            }
        }

        request.setAttribute("attendees", attendees);
        request.getRequestDispatcher("organizer-attendees.jsp").forward(request, response);
    }


    // ------------------------------------------------------------
    // Checks if this organizer actually owns this event
    // ------------------------------------------------------------
    // Both editEvent and cancelEvent need this same check, and
    // viewAttendees needs it too, so instead of copying the same
    // SELECT statement three times, I pull it out into one method
    // and call it from all three places.
    private boolean organizerOwnsEvent(int eventId, int organizerId) throws SQLException {
        String sql = "SELECT Event_ID FROM Events WHERE Event_ID = ? AND Organizer_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, organizerId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
