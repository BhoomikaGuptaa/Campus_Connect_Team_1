package servlets;

import db.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<Map<String, String>> events = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT event_id, name, category, " +
                    "event_date, event_time, location, " +
                    "capacity, status FROM Events " +
                    "WHERE status = 'open' " +
                    "ORDER BY event_date ASC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> event = new HashMap<>();
                event.put("event_id", rs.getString("event_id"));
                event.put("name", rs.getString("name"));
                event.put("category", rs.getString("category"));
                event.put("event_date", rs.getString("event_date"));
                event.put("event_time", rs.getString("event_time"));
                event.put("location", rs.getString("location"));
                event.put("capacity", rs.getString("capacity"));
                event.put("status", rs.getString("status"));
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("events", events);
        request.getRequestDispatcher("/index.jsp")
                .forward(request, response);
    }
}