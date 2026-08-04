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

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null
                || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        List<Map<String, Object>> organizerRequests = new ArrayList<>();
        List<Map<String, Object>> users = new ArrayList<>();
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Map<String, Object>> activityLogs = new ArrayList<>();

        String requestSql =
                "SELECT r.Request_ID, r.User_ID, u.First_Name, u.Last_Name, " +
                "       u.Email, r.Submitted_At, r.Status " +
                "FROM OrganizerRequests r " +
                "JOIN Users u ON r.User_ID = u.User_ID " +
                "ORDER BY r.Submitted_At DESC";

        String userSql =
                "SELECT User_ID, First_Name, Last_Name, Email, Is_Active " +
                "FROM Users " +
                "ORDER BY Last_Name, First_Name";

        String categorySql =
                "SELECT Category_ID, Name " +
                "FROM Categories " +
                "ORDER BY Name";

        String logSql =
                "SELECT l.Log_ID, l.Action, l.Target_Type, l.Target_ID, " +
                "       l.Created_At, u.First_Name, u.Last_Name " +
                "FROM ActivityLogs l " +
                "JOIN Users u ON l.Admin_ID = u.User_ID " +
                "ORDER BY l.Created_At DESC";

        try (Connection connection = DBConnection.getConnection()) {
            loadRows(connection, requestSql, organizerRequests,
                    new String[]{"Request_ID", "User_ID", "First_Name", "Last_Name",
                            "Email", "Submitted_At", "Status"});

            loadRows(connection, userSql, users,
                    new String[]{"User_ID", "First_Name", "Last_Name", "Email", "Is_Active"});

            loadRows(connection, categorySql, categories,
                    new String[]{"Category_ID", "Name"});

            loadRows(connection, logSql, activityLogs,
                    new String[]{"Log_ID", "Action", "Target_Type", "Target_ID",
                            "Created_At", "First_Name", "Last_Name"});

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        request.setAttribute("organizerRequests", organizerRequests);
        request.setAttribute("users", users);
        request.setAttribute("categories", categories);
        request.setAttribute("activityLogs", activityLogs);
        request.getRequestDispatcher("admin.jsp").forward(request, response);
    }

    private void loadRows(Connection connection, String sql,
                          List<Map<String, Object>> rows, String[] columns)
            throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();

                for (String column : columns) {
                    row.put(column, resultSet.getObject(column));
                }

                rows.add(row);
            }
        }
    }
}
