package servlets;

import db.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/people")
public class SkillSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int selectedSkillId = readNumber(request.getParameter("skillId"));
        int selectedEventId = readNumber(request.getParameter("eventId"));

        List<Map<String, Object>> skillOptions = new ArrayList<>();
        List<Map<String, Object>> eventOptions = new ArrayList<>();
        List<Map<String, Object>> people = new ArrayList<>();

        String skillsSql =
                "SELECT Skill_ID, Name " +
                "FROM Skills " +
                "ORDER BY Name";

        String eventsSql =
                "SELECT Event_ID, Title " +
                "FROM Events " +
                "WHERE Is_Cancelled = FALSE " +
                "ORDER BY Event_Date";

        try (Connection connection = DBConnection.getConnection()) {

            try (PreparedStatement statement = connection.prepareStatement(skillsSql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Map<String, Object> skill = new HashMap<>();
                    skill.put("id", resultSet.getInt("Skill_ID"));
                    skill.put("name", resultSet.getString("Name"));
                    skillOptions.add(skill);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(eventsSql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", resultSet.getInt("Event_ID"));
                    event.put("title", resultSet.getString("Title"));
                    eventOptions.add(event);
                }
            }

            String peopleSql;

            if (selectedSkillId > 0 && selectedEventId > 0) {
                peopleSql =
                        "SELECT DISTINCT u.User_ID, u.First_Name, u.Last_Name, " +
                        "       s.Major, s.Grad_Year, s.Bio " +
                        "FROM Users u " +
                        "JOIN Students s ON u.User_ID = s.User_ID " +
                        "JOIN HasSkill hs ON u.User_ID = hs.User_ID " +
                        "JOIN Signups su ON u.User_ID = su.Student_ID " +
                        "WHERE hs.Skill_ID = ? " +
                        "  AND su.Event_ID = ? " +
                        "  AND su.Status = 'registered' " +
                        "  AND u.Is_Active = TRUE " +
                        "ORDER BY u.First_Name, u.Last_Name";

            } else if (selectedSkillId > 0) {
                peopleSql =
                        "SELECT DISTINCT u.User_ID, u.First_Name, u.Last_Name, " +
                        "       s.Major, s.Grad_Year, s.Bio " +
                        "FROM Users u " +
                        "JOIN Students s ON u.User_ID = s.User_ID " +
                        "JOIN HasSkill hs ON u.User_ID = hs.User_ID " +
                        "WHERE hs.Skill_ID = ? " +
                        "  AND u.Is_Active = TRUE " +
                        "ORDER BY u.First_Name, u.Last_Name";

            } else if (selectedEventId > 0) {
                peopleSql =
                        "SELECT DISTINCT u.User_ID, u.First_Name, u.Last_Name, " +
                        "       s.Major, s.Grad_Year, s.Bio " +
                        "FROM Users u " +
                        "JOIN Students s ON u.User_ID = s.User_ID " +
                        "JOIN Signups su ON u.User_ID = su.Student_ID " +
                        "WHERE su.Event_ID = ? " +
                        "  AND su.Status = 'registered' " +
                        "  AND u.Is_Active = TRUE " +
                        "ORDER BY u.First_Name, u.Last_Name";

            } else {
                peopleSql =
                        "SELECT u.User_ID, u.First_Name, u.Last_Name, " +
                        "       s.Major, s.Grad_Year, s.Bio " +
                        "FROM Users u " +
                        "JOIN Students s ON u.User_ID = s.User_ID " +
                        "WHERE u.Is_Active = TRUE " +
                        "ORDER BY u.First_Name, u.Last_Name";
            }

            try (PreparedStatement statement = connection.prepareStatement(peopleSql)) {

                if (selectedSkillId > 0 && selectedEventId > 0) {
                    statement.setInt(1, selectedSkillId);
                    statement.setInt(2, selectedEventId);
                } else if (selectedSkillId > 0) {
                    statement.setInt(1, selectedSkillId);
                } else if (selectedEventId > 0) {
                    statement.setInt(1, selectedEventId);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, Object> person = new HashMap<>();
                        int userId = resultSet.getInt("User_ID");

                        person.put("User_ID", userId);
                        person.put("First_Name", resultSet.getString("First_Name"));
                        person.put("Last_Name", resultSet.getString("Last_Name"));
                        person.put("Major", resultSet.getString("Major"));
                        person.put("Grad_Year", resultSet.getInt("Grad_Year"));
                        person.put("Bio", resultSet.getString("Bio"));
                        person.put("Skill_List", loadSkillList(connection, userId));

                        people.add(person);
                    }
                }
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }

        request.setAttribute("skillOptions", skillOptions);
        request.setAttribute("eventOptions", eventOptions);
        request.setAttribute("people", people);
        request.setAttribute("selectedSkillId", selectedSkillId);
        request.setAttribute("selectedEventId", selectedEventId);
        request.getRequestDispatcher("people.jsp").forward(request, response);
    }

    private String loadSkillList(Connection connection, int userId) throws SQLException {
        String sql =
                "SELECT sk.Name " +
                "FROM Skills sk " +
                "JOIN HasSkill hs ON sk.Skill_ID = hs.Skill_ID " +
                "WHERE hs.User_ID = ? " +
                "ORDER BY sk.Name";

        List<String> names = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    names.add(resultSet.getString("Name"));
                }
            }
        }

        return names.isEmpty() ? "No skills listed" : String.join(", ", names);
    }

    private int readNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
