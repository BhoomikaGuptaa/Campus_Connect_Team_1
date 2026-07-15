<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login — Campus Connect</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f9f9f9; color: #222; }
        nav { background: #0055A2; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        nav h1 { color: white; margin: 0; font-size: 1.3rem; }
        nav a { color: white; text-decoration: none; margin-left: 15px; font-weight: bold; }
        .form-wrap { max-width: 400px; margin: 60px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .form-wrap h2 { color: #0055A2; margin-top: 0; }
        .form-wrap label { display: block; margin-top: 14px; margin-bottom: 5px; font-weight: bold; font-size: 0.9rem; }
        .form-wrap input { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px; box-sizing: border-box; }
        .form-wrap button { margin-top: 22px; width: 100%; padding: 12px; background: #0055A2; color: white; border: none; border-radius: 5px; font-weight: bold; cursor: pointer; }
        .form-wrap button:hover { background: #003d7a; }
        .error-box { background: #fdecea; color: #b3261e; padding: 10px 14px; border-radius: 5px; margin-top: 16px; font-size: 0.9rem; }
        .success-box { background: #e6f4ea; color: #137333; padding: 10px 14px; border-radius: 5px; margin-top: 16px; font-size: 0.9rem; }
        .switch-link { text-align: center; margin-top: 16px; font-size: 0.9rem; }
        .switch-link a { color: #0055A2; font-weight: bold; text-decoration: none; }
        .switch-link a:hover { text-decoration: underline; }
    </style>
</head>
<body>

<nav>
    <h1>CampusConnect</h1>
    <div><a href="index.jsp">Home</a></div>
</nav>

<div class="form-wrap">
    <h2>Log In</h2>

    <% if (request.getParameter("registered") != null) { %>
        <div class="success-box">Account created! Please log in below.</div>
    <% } %>

    <% if (request.getAttribute("error") != null) { %>
        <div class="error-box"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="login">
        <label for="email">SJSU Email</label>
        <input type="email" id="email" name="email" placeholder="you@sjsu.edu" required>

        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>

        <button type="submit">Log In</button>
    </form>

    <div class="switch-link">
        Don't have an account? <a href="register.jsp">Sign up</a>
    </div>
</div>

</body>
</html>