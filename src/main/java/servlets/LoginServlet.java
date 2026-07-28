package servlets;

import db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Please enter both email and password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // Users has no Role column under the ISA/E-R separation method —
        // a user's role is determined by which subclass table their User_ID appears in.
        String sql =
                "SELECT u.User_ID, u.First_Name, u.Last_Name, u.Email, u.Password, u.Is_Active, " +
                        "       CASE " +
                        "           WHEN a.User_ID IS NOT NULL THEN 'admin' " +
                        "           WHEN o.User_ID IS NOT NULL THEN 'organizer' " +
                        "           WHEN s.User_ID IS NOT NULL THEN 'student' " +
                        "           ELSE 'unknown' " +
                        "       END AS Role " +
                        "FROM Users u " +
                        "LEFT JOIN Students s ON u.User_ID = s.User_ID " +
                        "LEFT JOIN EventOrganizer o ON u.User_ID = o.User_ID " +
                        "LEFT JOIN Administrator a ON u.User_ID = a.User_ID " +
                        "WHERE u.Email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    request.setAttribute("error", "Invalid email or password.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                String storedHash = rs.getString("Password");
                boolean isActive  = rs.getBoolean("Is_Active");

                if (!isActive) {
                    request.setAttribute("error", "This account has been suspended. Please contact an administrator.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                if (!BCrypt.checkpw(password, storedHash)) {
                    request.setAttribute("error", "Invalid email or password.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("userId", rs.getInt("User_ID"));
                session.setAttribute("firstName", rs.getString("First_Name"));
                session.setAttribute("lastName", rs.getString("Last_Name"));
                session.setAttribute("email", rs.getString("Email"));
                session.setAttribute("role", rs.getString("Role"));
                session.setMaxInactiveInterval(30 * 60);
            }

        } catch (SQLException e) {
            request.setAttribute("error", "Something went wrong logging you in. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        response.sendRedirect("index.jsp");
    }
}