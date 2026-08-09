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

/**
 * FR1 — User Authentication.
 * I check the email and password against the Users table, then figure out
 * the user's role by checking which ISA subclass table (Administrator,
 * EventOrganizer, or Students) their User_ID shows up in. I use three
 * SELECT FROM WHERE queries for this.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        // I make sure both fields are filled in before touching the database.
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Please enter both email and password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // Users has no Role column.
        // A user's role is determined by which subclass table their User_ID appears in.
        // I just pull the basic account info here; role comes later.
        String userSql =
                "SELECT User_ID, First_Name, Last_Name, Email, Password, Is_Active " +
                        "FROM Users " +
                        "WHERE Email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(userSql)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                // No row means no account with this email.
                if (!rs.next()) {
                    request.setAttribute("error", "Invalid email or password.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                int userId        = rs.getInt("User_ID");
                String storedHash = rs.getString("Password");
                boolean isActive  = rs.getBoolean("Is_Active");

                // Stop suspended accounts here before even checking the password.
                if (!isActive) {
                    request.setAttribute("error", "This account has been suspended. Please contact an administrator.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                // Compare the typed password against the stored BCrypt hash.
                if (!BCrypt.checkpw(password, storedHash)) {
                    request.setAttribute("error", "Invalid email or password.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }

                // Credentials are good, so now I look up which role this user has.
                String role = determineRole(con, userId);

                HttpSession session = request.getSession(true);
                session.setAttribute("userId", userId);
                session.setAttribute("firstName", rs.getString("First_Name"));
                session.setAttribute("lastName", rs.getString("Last_Name"));
                session.setAttribute("email", rs.getString("Email"));
                session.setAttribute("role", role);
                session.setMaxInactiveInterval(30 * 60);
            }

        } catch (SQLException e) {
            request.setAttribute("error", "Something went wrong logging you in. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        response.sendRedirect("index.jsp");
    }

    /**
     * I check the three ISA subclass tables one at a time to find this
     * user's role. I stop at the first match — admin, then organizer,
     * then student — since a User_ID only ever appears in one of them.
     */
    private String determineRole(Connection con, int userId) throws SQLException {

        String adminSql     = "SELECT User_ID FROM Administrator WHERE User_ID = ?";
        String organizerSql = "SELECT User_ID FROM EventOrganizer WHERE User_ID = ?";
        String studentSql   = "SELECT User_ID FROM Students WHERE User_ID = ?";

        // Check Administrator first.
        try (PreparedStatement stmt = con.prepareStatement(adminSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "admin";
                }
            }
        }

        // Not an admin, so I check EventOrganizer next.
        try (PreparedStatement stmt = con.prepareStatement(organizerSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "organizer";
                }
            }
        }

        // Not an organizer either, so I check Students.
        try (PreparedStatement stmt = con.prepareStatement(studentSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "student";
                }
            }
        }

        // I shouldn't reach this if every user has a subclass row, but I
        // return something safe instead of throwing an error.
        return "unknown";
    }
}
