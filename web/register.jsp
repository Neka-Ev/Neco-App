<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户注册 - Neco</title>
</head>
<body class="auth-page">
    <div class="auth-container">
        <h3 class="auth-title">用户注册</h3>
        <form id="registerForm" method="POST" action="<%= request.getContextPath() %>/api/auth/register">
            <div class="form-group">
                <label class="form-label">用户名</label>
                <input type="text" name="username" required class="form-input" />
            </div>
            <div class="form-group">
                <label class="form-label">显示名称</label>
                <input type="text" name="displayName" class="form-input" />
            </div>
            <div class="form-group">
                <label class="form-label">邮箱地址</label>
                <input type="email" name="email" required class="form-input" />
            </div>
            <div class="form-group">
                <label class="form-label">密码</label>
                <input type="password" name="password" required class="form-input" />
            </div>
            <div class="form-submit">
                <button type="submit" class="submit-button">注册</button>
            </div>
            <%-- Display error message if any --%>
            <% String error = request.getParameter("error"); 
               if (error != null && !error.isEmpty()) { %>
                <div class="error-message"><%= error %></div>
            <% } %>
        </form>
        <div class="auth-link">
            已有账户？ <a href="<%= request.getContextPath() %>/index.jsp?page=login" class="auth-link-text">立即登录</a>
        </div>
    </div>
</body>
</html>