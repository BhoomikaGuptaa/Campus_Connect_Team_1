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
import java.sql.Statement;

@WebServlet("/admin-action")
public class AdminActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null
                || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        int adminId = (Integer) session.getAttribute("userId");
        String action = clean(request.getParameter("action"));

        try (Connection connection = DBConnection.getConnection()) {

            if ("approveOrganizer".equals(action)) {
                approveOrganizer(connection, adminId,
                        readNumber(request.getParameter("requestId")));
                session.setAttribute("flashSuccess", "Organizer request approved.");

            } else if ("rejectOrganizer".equals(action)) {
                rejectOrganizer(connection, adminId,
                        readNumber(request.getParameter("requestId")));
                session.setAttribute("flashSuccess", "Organizer request rejected.");

            } else if ("suspendUser".equals(action)) {
                int userId = readNumber(request.getParameter("userId"));

                if (userId == adminId) {
                    session.setAttribute("flashError", "You cannot suspend your own account.");
                } else {
                    changeUserStatus(connection, adminId, userId, false);
                    session.setAttribute("flashSuccess", "User account suspended.");
                }

            } else if ("activateUser".equals(action)) {
                int userId = readNumber(request.getParameter("userId"));
                changeUserStatus(connection, adminId, userId, true);
                session.setAttribute("flashSuccess", "User account reactivated.");

            } else if ("addCategory".equals(action)) {
                addCategory(connection, adminId,
                        clean(request.getParameter("categoryName")));
                session.setAttribute("flashSuccess", "Category added.");

            } else if ("deleteCategory".equals(action)) {
                int categoryId = readNumber(request.getParameter("categoryId"));

                if (categoryIsUsed(connection, categoryId)) {
                    session.setAttribute("flashError",
                            "This category cannot be deleted because an event is using it.");
                } else {
                    deleteCategory(connection, adminId, categoryId);
                    session.setAttribute("flashSuccess", "Category deleted.");
                }
            }

        } catch (Exception e) {
            session.setAttribute("flashError", "The administrator action could not be completed.");
        }

        response.sendRedirect("admin");
    }

    private void approveOrganizer(Connection connection, int adminId, int requestId)
            throws Exception {

        connection.setAutoCommit(false);

        try {
            String updateSql =
                    "UPDATE OrganizerRequests " +
                    "SET Status = 'Approved', Reviewed_At = CURRENT_TIMESTAMP " +
                    "WHERE Request_ID = ?";

            try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                statement.setInt(1, requestId);
                statement.executeUpdate();
            }

            int userId = 0;
            String findUserSql =
                    "SELECT User_ID FROM OrganizerRequests WHERE Request_ID = ?";

            try (PreparedStatement statement = connection.prepareStatement(findUserSql)) {
                statement.setInt(1, requestId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getInt("User_ID");
                    }
                }
            }

            String checkSql =
                    "SELECT User_ID FROM EventOrganizer WHERE User_ID = ?";

            boolean alreadyOrganizer = false;

            try (PreparedStatement statement = connection.prepareStatement(checkSql)) {
                statement.setInt(1, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    alreadyOrganizer = resultSet.next();
                }
            }

            if (!alreadyOrganizer && userId > 0) {
                String insertSql =
                        "INSERT INTO EventOrganizer (User_ID) VALUES (?)";

                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }
            }

            addLog(connection, adminId, "Approved organizer request",
                    "OrganizerRequest", requestId);

            connection.commit();

        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void rejectOrganizer(Connection connection, int adminId, int requestId)
            throws Exception {

        String sql =
                "UPDATE OrganizerRequests " +
                "SET Status = 'Rejected', Reviewed_At = CURRENT_TIMESTAMP " +
                "WHERE Request_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();
        }

        addLog(connection, adminId, "Rejected organizer request",
                "OrganizerRequest", requestId);
    }

    private void changeUserStatus(Connection connection, int adminId,
                                  int userId, boolean active) throws Exception {

        String sql =
                "UPDATE Users SET Is_Active = ? WHERE User_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }

        String action = active ? "Reactivated user" : "Suspended user";
        addLog(connection, adminId, action, "User", userId);
    }

    private void addCategory(Connection connection, int adminId, String categoryName)
            throws Exception {

        if (categoryName.isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        String sql =
                "INSERT INTO Categories (Name) VALUES (?)";

        int categoryId = 0;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, categoryName);
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    categoryId = resultSet.getInt(1);
                }
            }
        }

        addLog(connection, adminId, "Added category", "Category", categoryId);
    }

    private boolean categoryIsUsed(Connection connection, int categoryId)
            throws Exception {

        String sql =
                "SELECT COUNT(*) AS Event_Count " +
                "FROM Events " +
                "WHERE Category_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("Event_Count") > 0;
            }
        }
    }

    private void deleteCategory(Connection connection, int adminId, int categoryId)
            throws Exception {

        String sql =
                "DELETE FROM Categories WHERE Category_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }

        addLog(connection, adminId, "Deleted category", "Category", categoryId);
    }

    private void addLog(Connection connection, int adminId, String action,
                        String targetType, int targetId) throws Exception {

        String sql =
                "INSERT INTO ActivityLogs " +
                "(Admin_ID, Action, Target_Type, Target_ID) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, adminId);
            statement.setString(2, action);
            statement.setString(3, targetType);
            statement.setInt(4, targetId);
            statement.executeUpdate();
        }
    }

    private int readNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
