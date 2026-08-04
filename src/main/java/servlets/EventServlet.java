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

// This servlet covers FR3, Event Management for Event Organizers.
// One servlet handles four different actions: create, edit, cancel,
// and viewing the attendee list. I split the work by reading an
// "action" parameter instead of writing four separate servlets,
// because all four actions are really just different things you
// can do to the same Events table, so it makes sense to keep them
// together in one place.

@WebServlet("/event")
public class EventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Every action here needs the person to be logged in as an
        // organizer, so I check the session first before doing
        // anything else. If there is no session, or the session
        // doesn't have a User_ID in it, I send them back to the
        // login page instead of letting the request go through.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("User_ID") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int organizerId = (int) session.getAttribute("User_ID");
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
                // request instead of silently doing nothing.
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (SQLException e) {
            // I wrap SQL exceptions in a ServletException so the
            // container's error handling takes over. Letting the raw
            // SQLException escape would expose database details to
            // whatever is looking at the stack trace.
            throw new ServletException("Database error in EventServlet.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("User_ID") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int organizerId = (int) session.getAttribute("User_ID");
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
        // database. The Events table also has a CHECK constraint for
        // this, but catching the problem early means I can send the
        // organizer back a clear message instead of a database error.
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

        // Before I touch anything, I confirm this organizer actually
        // owns the event they're trying to edit. This matches what the
        // proposal says: an organizer cannot edit someone else's
        // event. If ownership fails, I stop here and send back a
        // 403 instead of quietly doing nothing, so the organizer
        // actually knows their request was blocked.
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

        // This sets Is_Cancelled to TRUE instead of deleting the row.
        // I want to keep cancelled events around in the database so
        // students who signed up can still see the event was
        // cancelled, and so the Signups rows tied to this event don't
        // get orphaned by a foreign key problem.
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

        String sql = "SELECT Users.First_Name, Users.Last_Name, Users.Email, "
                + "Signups.Status, Signups.Signed_Up_At "
                + "FROM Events "
                + "JOIN Signups ON Events.Event_ID = Signups.Event_ID "
                + "JOIN Users ON Signups.Student_ID = Users.User_ID "
                + "WHERE Events.Event_ID = ? AND Events.Organizer_ID = ? AND Signups.Status = 'registered' "
                + "ORDER BY Signups.Signed_Up_At";

        // This list holds the finished, plain Java objects. I build it
        // up while the Connection is still open, so by the time I reach
        // the JSP, all the data I need is already sitting safely in
        // memory and doesn't depend on the database connection anymore.
        List<Attendee> attendees = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, organizerId);

            try (ResultSet rs = stmt.executeQuery()) {
                // I read every row here, while rs and conn are both
                // still open, and copy each row into an Attendee object.
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
        // By this point the try-with-resources block has already closed
        // the Connection, PreparedStatement, and ResultSet. That's fine,
        // because I don't need any of them anymore, everything I need
        // is now inside the attendees list.

        request.setAttribute("attendees", attendees);
        request.getRequestDispatcher("attendees.jsp").forward(request, response);
    }


    // ------------------------------------------------------------
    // Shared helper: checks if this organizer actually owns this event
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
