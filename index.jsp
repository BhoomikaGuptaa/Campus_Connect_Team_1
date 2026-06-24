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
    <h1>CampusConnect</h1>
    <div>
        <a href="index.jsp">Home</a>
        <a href="login.jsp">Login</a>
    </div>
</nav>

<%-- Hero section: big banner with tagline and Get Started button --%>
<div class = "hero"> 
	<h2>Discover SJSU Events</h2>
	<p>Find hackathons, career fairs, workshops and more. </p>
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
<%-- Java scriptlet: connect to MySQL and query upcoming events --%>

	
	
	
	
	
	
	
	
	</table>
 </div>