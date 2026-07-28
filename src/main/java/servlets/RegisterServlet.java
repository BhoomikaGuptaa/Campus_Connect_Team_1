package servlets;

import db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

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
import java.sql.Statement;
import java.sql.Types;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String firstName     = request.getParameter("firstName");
        String lastName      = request.getParameter("lastName");
        String email          = request.getParameter("email");
        String password       = request.getParameter("password");
        String majorParam     = request.getParameter("major");
        String gradYearParam  = request.getParameter("gradYear");

        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.length() < 8) {
            request.setAttribute("error", "Please fill in all required fields. Password must be at least 8 characters.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (!email.toLowerCase().endsWith("@sjsu.edu")) {
            request.setAttribute("error", "Please register with your SJSU email address.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

        // Self-registration always creates a Student account (ISA subclass).
        // Organizer access is granted later through an admin-approved OrganizerRequest.
        String checkSql          = "SELECT User_ID FROM Users WHERE Email = ?";
        String insertUserSql     = "INSERT INTO Users (First_Name, Last_Name, Email, Password, Is_Active) VALUES (?, ?, ?, ?, 1)";
        String insertStudentSql  = "INSERT INTO Students (User_ID, Major, Grad_Year) VALUES (?, ?, ?)";

        Integer gradYear = null;
        if (gradYearParam != null && !gradYearParam.trim().isEmpty()) {
            try {
                gradYear = Integer.parseInt(gradYearParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        try (Connection con = DBConnection.getConnection()) {

            // Reject duplicate emails
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setString(1, email.trim());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("error", "An account with this email already exists. Please log in instead.");
                        request.getRequestDispatcher("register.jsp").forward(request, response);
                        return;
                    }
                }
            }

            // Insert into Users (superclass) then Students (subclass) as ONE transaction —
            // both rows must exist together, or neither should.
            con.setAutoCommit(false);
            int newUserId;

            try (PreparedStatement insertUserStmt =
                         con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                insertUserStmt.setString(1, firstName.trim());
                insertUserStmt.setString(2, lastName.trim());
                insertUserStmt.setString(3, email.trim());
                insertUserStmt.setString(4, hashedPassword);
                insertUserStmt.executeUpdate();

                try (ResultSet keys = insertUserStmt.getGeneratedKeys()) {
                    keys.next();
                    newUserId = keys.getInt(1);
                }
            }

            try (PreparedStatement insertStudentStmt = con.prepareStatement(insertStudentSql)) {
                insertStudentStmt.setInt(1, newUserId);
                insertStudentStmt.setString(2, (majorParam == null || majorParam.trim().isEmpty()) ? null : majorParam.trim());
                if (gradYear != null) {
                    insertStudentStmt.setInt(3, gradYear);
                } else {
                    insertStudentStmt.setNull(3, Types.INTEGER);
                }
                insertStudentStmt.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {
            request.setAttribute("error", "Something went wrong creating your account. Please try again.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        response.sendRedirect("login.jsp?registered=true");
    }
}