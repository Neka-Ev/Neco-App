<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>用户登录</title>
</head>
<body class="auth-page">
    <div class="auth-container">
        <h3 class="auth-title">用户登录</h3>
        <form id="loginForm" method="POST" action="<%= request.getContextPath() %>/api/auth/login">
            <div class="form-group">
                <label class="form-label">用户名</label>
                <input type="text" name="username" required class="form-input" />
            </div>
            <div class="form-group">
                <label class="form-label">密码</label>
                <input type="password" name="password" required class="form-input" />
            </div>
            <div class="form-submit">
                <button type="submit" class="submit-button">登录</button>
            </div>
            <%-- Display error message if any --%>
            <% String error = request.getParameter("error"); 
               if (error != null && !error.isEmpty()) { %>
                <div class="error-message"><%= error %></div>
            <% } %>
        </form>
        <p class="auth-link">没有账户？ <a href="<%= request.getContextPath() %>/index.jsp?page=register" class="auth-link-text">立即注册</a></p>
    </div>
</body>
</html>