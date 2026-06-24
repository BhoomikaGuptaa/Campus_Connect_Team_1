<%-- JSP comment: hidden from browser, only visible in source code --%>
<%-- Import all Java SQL classes needed for database connection --%>
<%@ page import="java.sql.*" %>
<%-- Tell Tomcat: use Java, send HTML with UTF-8 encoding --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Campus Connect</title>
    <style>
        /* Reset default margin/padding, set font and background for whole page */
        body { font-family: Arial, sans-serif; margin: 0; background: #f9f9f9; }

        /* Top navigation bar - blue background, logo left, links right */
        nav {
            background: #0055A2;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        /* Logo text styling */
        nav h1 { color: white; margin: 0; font-size: 1.3rem; }
        /* Nav link styling */
        nav a { color: white; text-decoration: none; margin-left: 15px; }

        /* Hero banner - big blue section at top of page */
        .hero {
            background: #0055A2;
            color: white;
            text-align: center;
            padding: 60px 20px;
        }
        .hero h2 { font-size: 2rem; margin-bottom: 10px; }
        .hero p { font-size: 1rem; margin-bottom: 20px; }
        /* Get Started button - yellow with dark blue text */
        .hero a {
            background: #FFB800;
            color: #003d7a;
            padding: 10px 25px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
        }

        /* Events section wrapper */
        .events { padding: 40px 30px; }
        .events h3 { font-size: 1.4rem; color: #0055A2; margin-bottom: 20px; }

        /* Events table - full width, no gaps between cells */
        table { width: 100%; border-collapse: collapse; background: white; }
        /* Table header row - blue background, white text */
        th { background: #0055A2; color: white; padding: 12px; text-align: left; }
        /* Table data cells - padding and bottom border */
        td { padding: 12px; border-bottom: 1px solid #ddd; }
        /* Highlight row light blue on hover */
        tr:hover { background: #f0f5ff; }

        /* Footer - centered, gray, small text */
        footer { text-align: center; padding: 20px; color: #888; font-size: 0.85rem; }
    </style>
</head>
<body>

<%-- Navigation bar: logo on left, Home and Login links on right --%>
<nav>
    <h1 style ="text-decorationL underline;">CampusConnect</h1>
    <div>
        <a href="index.jsp"> Home  </a> 
        <a href="login.jsp">Login</a>
    </div>
</nav>

<%-- Hero section: big banner with tagline and Get Started button --%>
<div class = "hero"> 
	<h2>🌐 DISCOVER SJSU EVENTS 🌟</h2>
	<p>👨‍💻Find Hackathons, Career Fairs, Workshops and More👩‍💻 </p>
	<a href = "Login.jsp">Get Started</a>
</div>

<%-- Events section: table of upcoming events pulled from MySQL --%>
<div class ="events">
	<h3>Upcoming Events</h3>
	<table>
		<%-- Table Header row --%>
		<tr> 
			<th>Event</th>
			<th>Category</th>
			<th>Date</th>
			<th>Location</th>
			<th>Spots</th>
			<th>Action</th>
		</tr>
<%-- connect to MySQL and query upcoming events --%>
<%
    /* Declare connection outside try block so we can close it in finally */
    Connection con = null;
    try {
        /* Load the MySQL JDBC driver into memory */
        Class.forName("com.mysql.cj.jdbc.Driver");

        /* Open connection to campusconnect database on localhost */
        con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/campusconnect?autoReconnect=true&useSSL=false",
            "root", "1234Desa$");

        /* Create a statement object to send SQL queries */
        Statement stmt = con.createStatement();
        /* Query: join events and categories tables, skip cancelled events, sort by date */
        ResultSet rs = stmt.executeQuery(
        		"SELECT e.title, e.location,e.event_date, e.capacity, c.name " +
        		"FROM events e JOIN categories c ON e.category_id = c.category_id " + 
        		"WHERE e.is_cancelled = 0 ORDER BY e.event_date ASC"); 
        /* Loop through each row returned by the query */
        while(rs.next()){
%>
 			<%-- Print one table row per event --%>
 			<tr> 
 				<%-- Event title from events table --%>
 				<td><%= rs.getString("title") %></td>
 				<%-- Category name from categories table --%>
 				<td><%= rs.getString("name") %></td>
                <%-- Trim timestamp to just date and time e.g. 2026-07-08 17:00 --%>
                <td><%= rs.getTimestamp("event_date").toString().substring(0, 16) %></td>
                <%-- Event location --%>
                <td><%= rs.getString("location") %></td>
                <%-- Max capacity (number of spots) --%>
                <td><%= rs.getInt("capacity") %></td>
                <%-- Sign Up link goes to login page --%>
                <td><a href="login.jsp">Sign Up</a></td>
            </tr>
<%
		}/* end while loop */ 
        
        /* Close result set and statement to free resources */
        rs.close();
        stmt.close();
    } catch (Exception e) {
        /* If anything goes wrong, print the error inside the table */
        out.println("<tr><td colspan='6'>Error: " + e.getMessage() + "</td></tr>");

    } finally {
        /* Always close the connection, even if an error occurred */
        if (con != null) try { con.close(); } catch (SQLException ex) {}
    }
   
%>	
	</table>
 </div>
 <%-- Footer --%>
<footer>
    &copy; 2026 CampusConnect — San José State University
</footer>

</body>
</html>