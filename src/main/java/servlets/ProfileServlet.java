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

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String profileSql =
                "SELECT u.First_Name, u.Last_Name, u.Email, " +
                "       s.Major, s.Grad_Year, s.Bio " +
                "FROM Users u " +
                "JOIN Students s ON u.User_ID = s.User_ID " +
                "WHERE u.User_ID = ?";

        String skillsSql =
                "SELECT sk.Skill_ID, sk.Name, hs.User_ID " +
                "FROM Skills sk " +
                "LEFT JOIN HasSkill hs " +
                "  ON sk.Skill_ID = hs.Skill_ID AND hs.User_ID = ? " +
                "ORDER BY sk.Name";

        try (Connection connection = DBConnection.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(profileSql)) {
                statement.setInt(1, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        request.setAttribute("First_Name", resultSet.getString("First_Name"));
                        request.setAttribute("Last_Name", resultSet.getString("Last_Name"));
                        request.setAttribute("Email", resultSet.getString("Email"));
                        request.setAttribute("Major", resultSet.getString("Major"));
                        request.setAttribute("Grad_Year", resultSet.getInt("Grad_Year"));
                        request.setAttribute("Bio", resultSet.getString("Bio"));
                    }
                }
            }

            List<Map<String, Object>> skills = new ArrayList<>();

            try (PreparedStatement statement = connection.prepareStatement(skillsSql)) {
                statement.setInt(1, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, Object> skill = new HashMap<>();
                        skill.put("id", resultSet.getInt("Skill_ID"));
                        skill.put("name", resultSet.getString("Name"));
                        skill.put("selected", resultSet.getObject("User_ID") != null);
                        skills.add(skill);
                    }
                }
            }

            request.setAttribute("skills", skills);

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String firstName = clean(request.getParameter("firstName"));
        String lastName = clean(request.getParameter("lastName"));
        String major = clean(request.getParameter("major"));
        String bio = clean(request.getParameter("bio"));
        String[] skillIds = request.getParameterValues("skills");

        int gradYear = 0;
        try {
            gradYear = Integer.parseInt(request.getParameter("gradYear"));
        } catch (NumberFormatException ignored) {
        }

        if (firstName.isEmpty() || lastName.isEmpty()
                || bio.length() > 500 || gradYear < 2025 || gradYear > 2035) {
            session.setAttribute("flashError", "Please check your profile fields.");
            response.sendRedirect("profile");
            return;
        }

        String updateUserSql =
                "UPDATE Users " +
                "SET First_Name = ?, Last_Name = ? " +
                "WHERE User_ID = ?";

        String updateStudentSql =
                "UPDATE Students " +
                "SET Major = ?, Grad_Year = ?, Bio = ? " +
                "WHERE User_ID = ?";

        String deleteSkillsSql =
                "DELETE FROM HasSkill WHERE User_ID = ?";

        String insertSkillSql =
                "INSERT INTO HasSkill (User_ID, Skill_ID) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement = connection.prepareStatement(updateUserSql)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setInt(3, userId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(updateStudentSql)) {
                    statement.setString(1, major);
                    statement.setInt(2, gradYear);
                    statement.setString(3, bio);
                    statement.setInt(4, userId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(deleteSkillsSql)) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }

                if (skillIds != null) {
                    try (PreparedStatement statement = connection.prepareStatement(insertSkillSql)) {
                        for (String skillId : skillIds) {
                            statement.setInt(1, userId);
                            statement.setInt(2, Integer.parseInt(skillId));
                            statement.executeUpdate();
                        }
                    }
                }

                connection.commit();
                session.setAttribute("firstName", firstName);
                session.setAttribute("flashSuccess", "Profile updated successfully.");

            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }

        response.sendRedirect("profile");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
