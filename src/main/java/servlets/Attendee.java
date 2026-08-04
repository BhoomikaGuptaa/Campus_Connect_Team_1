package servlets;

import java.sql.Timestamp;

// This is a plain data holder for one row of attendee information.
// I create this so viewAttendees can read everything out of the
// ResultSet while the connection is still open, and then hand the
// JSP a list of these simple objects instead of the ResultSet
// itself. The JSP never touches the database directly, it just
// reads the fields on this class.
public class Attendee {

    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private Timestamp signedUpAt;

    public Attendee(String firstName, String lastName, String email, String status, Timestamp signedUpAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
        this.signedUpAt = signedUpAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getSignedUpAt() {
        return signedUpAt;
    }
}
