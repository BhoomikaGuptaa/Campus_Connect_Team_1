package servlets;

import db.DBConnection;

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
import java.sql.Types;

/**
 * FR5 — Event Signup with automatic waitlist management.
 * I handle both signing up for an event and cancelling a signup here,
 * picking which one to run based on the "action" parameter. Every write
 * happens inside one transaction, with FOR UPDATE locking the row I'm
 * about to check, so two students can't both grab the last open spot
 * at the same time.
 */
@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

 @Override
 protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws IOException {

  HttpSession session = request.getSession(false);
  if (session == null || session.getAttribute("userId") == null) {
   response.sendRedirect("login.jsp");
   return;
  }

  int studentId = (Integer) session.getAttribute("userId");

  int eventId;
  try {
   eventId = Integer.parseInt(request.getParameter("eventId"));
  } catch (Exception e) {
   response.sendRedirect("index.jsp");
   return;
  }

  String action = request.getParameter("action");

  try {
   if ("cancel".equals(action)) {
    cancel(studentId, eventId);
    session.setAttribute("flashSuccess", "Signup cancelled. Waitlist updated automatically.");
   } else {
    signup(studentId, eventId);
    session.setAttribute("flashSuccess", "Event signup updated.");
   }
  } catch (SQLException e) {
   session.setAttribute("flashError", "Could not update signup: " + e.getMessage());
  }

  // Send the student back to whichever page they signed up from.
  String returnTo = request.getParameter("returnTo");
  if ("my-events".equals(returnTo)) {
   response.sendRedirect("my-events");
  } else if ("events.jsp".equals(returnTo)) {
   response.sendRedirect("events.jsp");
  } else {
   response.sendRedirect("index.jsp");
  }
 }


 // ------------------------------------------------------------
 // Sign a student up for an event, or waitlist them if it's full
 // ------------------------------------------------------------
 private void signup(int studentId, int eventId) throws SQLException {

  try (Connection con = DBConnection.getConnection()) {
   con.setAutoCommit(false);

   try {
    // Check whether this student already has a signup row for
    // this event, and lock it if so.
    String existingSql =
            "SELECT Signup_ID, Status " +
                    "FROM Signups " +
                    "WHERE Student_ID = ? AND Event_ID = ? " +
                    "FOR UPDATE";

    Integer signupId = null;
    String oldStatus = null;

    try (PreparedStatement stmt = con.prepareStatement(existingSql)) {
     stmt.setInt(1, studentId);
     stmt.setInt(2, eventId);
     try (ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
       signupId = rs.getInt("Signup_ID");
       oldStatus = rs.getString("Status");
      }
     }
    }

    // A student who is already registered or waitlisted can't
    // sign up again for the same event.
    if ("registered".equals(oldStatus) || "waitlisted".equals(oldStatus)) {
     con.rollback();
     return;
    }

    // Lock the event row and read its capacity.
    String capacitySql =
            "SELECT Capacity " +
                    "FROM Events " +
                    "WHERE Event_ID = ? AND Is_Cancelled = 0 " +
                    "FOR UPDATE";

    int capacity;
    try (PreparedStatement stmt = con.prepareStatement(capacitySql)) {
     stmt.setInt(1, eventId);
     try (ResultSet rs = stmt.executeQuery()) {
      if (!rs.next()) {
       // No such event, or it's been cancelled.
       con.rollback();
       return;
      }
      capacity = rs.getInt("Capacity");
     }
    }

    // Count how many students are currently registered.
    String countSql =
            "SELECT COUNT(*) AS RegisteredCount " +
                    "FROM Signups " +
                    "WHERE Event_ID = ? AND Status = 'registered'";

    int registeredCount;
    try (PreparedStatement stmt = con.prepareStatement(countSql)) {
     stmt.setInt(1, eventId);
     try (ResultSet rs = stmt.executeQuery()) {
      rs.next();
      registeredCount = rs.getInt("RegisteredCount");
     }
    }

    String status = registeredCount < capacity ? "registered" : "waitlisted";
    Integer waitlistPosition = null;

    if ("waitlisted".equals(status)) {
     // Put this student at the back of the waitlist.
     String nextPositionSql =
             "SELECT COALESCE(MAX(Waitlist_Position), 0) + 1 AS NextPosition " +
                     "FROM Signups " +
                     "WHERE Event_ID = ? AND Status = 'waitlisted'";

     try (PreparedStatement stmt = con.prepareStatement(nextPositionSql)) {
      stmt.setInt(1, eventId);
      try (ResultSet rs = stmt.executeQuery()) {
       rs.next();
       waitlistPosition = rs.getInt("NextPosition");
      }
     }
    }

    if (signupId == null) {
     // No existing row for this student and event, so I insert one.
     String insertSql =
             "INSERT INTO Signups (Student_ID, Event_ID, Status, Waitlist_Position) " +
                     "VALUES (?, ?, ?, ?)";

     try (PreparedStatement stmt = con.prepareStatement(insertSql)) {
      stmt.setInt(1, studentId);
      stmt.setInt(2, eventId);
      stmt.setString(3, status);
      if (waitlistPosition == null) {
       stmt.setNull(4, Types.INTEGER);
      } else {
       stmt.setInt(4, waitlistPosition);
      }
      stmt.executeUpdate();
     }
    } else {
     // A cancelled row already exists for this student and
     // event, so I reuse it.
     String updateSql =
             "UPDATE Signups " +
                     "SET Status = ?, Waitlist_Position = ?, Signed_Up_At = CURRENT_TIMESTAMP " +
                     "WHERE Signup_ID = ?";

     try (PreparedStatement stmt = con.prepareStatement(updateSql)) {
      stmt.setString(1, status);
      if (waitlistPosition == null) {
       stmt.setNull(2, Types.INTEGER);
      } else {
       stmt.setInt(2, waitlistPosition);
      }
      stmt.setInt(3, signupId);
      stmt.executeUpdate();
     }
    }

    con.commit();

   } catch (SQLException e) {
    con.rollback();
    throw e;
   }
  }
 }


 // ------------------------------------------------------------
 // Cancel a student's signup and promote the next waitlisted student
 // ------------------------------------------------------------
 private void cancel(int studentId, int eventId) throws SQLException {

  try (Connection con = DBConnection.getConnection()) {
   con.setAutoCommit(false);

   try {
    // Look up and lock this student's signup row.
    String existingSql =
            "SELECT Signup_ID, Status, Waitlist_Position " +
                    "FROM Signups " +
                    "WHERE Student_ID = ? AND Event_ID = ? " +
                    "FOR UPDATE";

    int signupId = 0;
    String status = null;
    Integer position = null;

    try (PreparedStatement stmt = con.prepareStatement(existingSql)) {
     stmt.setInt(1, studentId);
     stmt.setInt(2, eventId);
     try (ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
       signupId = rs.getInt("Signup_ID");
       status = rs.getString("Status");
       position = (Integer) rs.getObject("Waitlist_Position");
      }
     }
    }

    // Nothing to cancel if there's no signup, or it's already cancelled.
    if (signupId == 0 || "cancelled".equals(status)) {
     con.rollback();
     return;
    }

    String cancelSql =
            "UPDATE Signups " +
                    "SET Status = 'cancelled', Waitlist_Position = NULL " +
                    "WHERE Signup_ID = ?";

    try (PreparedStatement stmt = con.prepareStatement(cancelSql)) {
     stmt.setInt(1, signupId);
     stmt.executeUpdate();
    }

    if ("registered".equals(status)) {
     // A registered spot opened up, so I look for whoever is
     // first in line on the waitlist.
     String nextInLineSql =
             "SELECT Signup_ID " +
                     "FROM Signups " +
                     "WHERE Event_ID = ? AND Status = 'waitlisted' " +
                     "ORDER BY Waitlist_Position " +
                     "LIMIT 1 " +
                     "FOR UPDATE";

     Integer nextSignupId = null;
     try (PreparedStatement stmt = con.prepareStatement(nextInLineSql)) {
      stmt.setInt(1, eventId);
      try (ResultSet rs = stmt.executeQuery()) {
       if (rs.next()) {
        nextSignupId = rs.getInt("Signup_ID");
       }
      }
     }

     if (nextSignupId != null) {
      // Promote that student to registered
      String promoteSql =
              "UPDATE Signups " +
                      "SET Status = 'registered', Waitlist_Position = NULL " +
                      "WHERE Signup_ID = ?";

      try (PreparedStatement stmt = con.prepareStatement(promoteSql)) {
       stmt.setInt(1, nextSignupId);
       stmt.executeUpdate();
      }

      // and shift everyone else on the waitlist up by one spot.
      String shiftSql =
              "UPDATE Signups " +
                      "SET Waitlist_Position = Waitlist_Position - 1 " +
                      "WHERE Event_ID = ? AND Status = 'waitlisted'";

      try (PreparedStatement stmt = con.prepareStatement(shiftSql)) {
       stmt.setInt(1, eventId);
       stmt.executeUpdate();
      }
     }

    } else if (position != null) {
     // The student who cancelled was on the waitlist, so I
     // move everyone behind them up by one spot.
     String shiftSql =
             "UPDATE Signups " +
                     "SET Waitlist_Position = Waitlist_Position - 1 " +
                     "WHERE Event_ID = ? AND Status = 'waitlisted' AND Waitlist_Position > ?";

     try (PreparedStatement stmt = con.prepareStatement(shiftSql)) {
      stmt.setInt(1, eventId);
      stmt.setInt(2, position);
      stmt.executeUpdate();
     }
    }

    con.commit();

   } catch (SQLException e) {
    con.rollback();
    throw e;
   }
  }
 }
}

