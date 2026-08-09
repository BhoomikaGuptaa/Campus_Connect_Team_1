package servlets;

import java.sql.Timestamp;

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

