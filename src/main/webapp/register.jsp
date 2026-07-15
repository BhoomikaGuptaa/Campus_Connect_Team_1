<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Register — Campus Connect</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f9f9f9; color: #222; }
        nav { background: #0055A2; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        nav h1 { color: white; margin: 0; font-size: 1.3rem; }
        nav a { color: white; text-decoration: none; margin-left: 15px; font-weight: bold; }
        .form-wrap { max-width: 440px; margin: 50px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
        .form-wrap h2 { color: #0055A2; margin-top: 0; }
        .form-wrap label { display: block; margin-top: 14px; margin-bottom: 5px; font-weight: bold; font-size: 0.9rem; }
        .form-wrap input { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px; box-sizing: border-box; }
        .form-row { display: flex; gap: 12px; }
        .form-row > div { flex: 1; }
        .form-wrap button { margin-top: 22px; width: 100%; padding: 12px; background: #0055A2; color: white; border: none; border-radius: 5px; font-weight: bold; cursor: pointer; }
        .form-wrap button:hover { background: #003d7a; }
        .error-box { background: #fdecea; color: #b3261e; padding: 10px 14px; border-radius: 5px; margin-top: 16px; font-size: 0.9rem; }
        .hint { color: #777; font-size: 0.8rem; margin-top: 4px; }
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
    <h2>Create Your Account</h2>

    <% if (request.getAttribute("error") != null) { %>
        <div class="error-box"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="register">
        <div class="form-row">
            <div>
                <label for="firstName">First Name</label>
                <input type="text" id="firstName" name="firstName" required>
            </div>
            <div>
                <label for="lastName">Last Name</label>
                <input type="text" id="lastName" name="lastName" required>
            </div>
        </div>

        <label for="email">SJSU Email</label>
        <input type="email" id="email" name="email" placeholder="you@sjsu.edu" required>

        <label for="password">Password</label>
        <input type="password" id="password" name="password" minlength="8" required>
        <div class="hint">At least 8 characters.</div>

        <div class="form-row">
            <div>
                <label for="major">Major (optional)</label>
                <input type="text" id="major" name="major">
            </div>
            <div>
                <label for="gradYear">Grad Year (optional)</label>
                <input type="number" id="gradYear" name="gradYear" min="2025" max="2032">
            </div>
        </div>

        <button type="submit">Sign Up</button>
    </form>

    <div class="switch-link">
        Already have an account? <a href="login.jsp">Log in</a>
    </div>
</div>

</body>
</html>