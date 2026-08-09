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
 * FR5 — Event Signup (viewing).
 * I show a student every event they've signed up for, past and present,
 * along with their signup status. I pull in the event's title and location,
 * and the category name.
 */
@WebServlet("/my-events")
public class MyEventsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int studentId = (Integer) session.getAttribute("userId");

        String sql =
                "SELECT e.Event_ID, e.Title, e.Event_Date, e.Location, " +
                        "       c.Name AS Category, s.Status, s.Waitlist_Position, s.Signed_Up_At " +
                        "FROM Signups s, Events e, Categories c " +
                        "WHERE s.Event_ID = e.Event_ID " +
                        "  AND e.Category_ID = c.Category_ID " +
                        "  AND s.Student_ID = ? " +
                        "ORDER BY e.Event_Date DESC";

        List<Map<String, Object>> events = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                // Copy each row into a Map, keyed by column name, so I
                // don't have to write out getString/getInt for every field.
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    String[] columns = {
                            "Event_ID", "Title", "Event_Date", "Location",
                            "Category", "Status", "Waitlist_Position", "Signed_Up_At"
                    };
                    for (String column : columns) {
                        row.put(column, rs.getObject(column));
                    }
                    events.add(row);
                }
            }

        } catch (SQLException e) {
            throw new ServletException("Database error in MyEventsServlet.", e);
        }

        request.setAttribute("events", events);
        request.getRequestDispatcher("my-events.jsp").forward(request, response);
    }
}
