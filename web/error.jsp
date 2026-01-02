<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>错误页面 - Neco</title>
    <link rel="stylesheet" href="./assets/css/common.css">
    <link rel="stylesheet" href="./assets/css/error.css">
</head>
<body class="error-page">
    <div class="error-container">
        <div class="error-icon">⚠️</div>
        <h1 class="error-title">
            <% if (response.getStatus() == 404) { %>
                页面未找到
            <% } else if (response.getStatus() == 500) { %>
                服务器内部错误
            <% } else { %>
                发生错误
            <% } %>
        </h1>
        
        <div class="error-code">
            错误代码: <%= response.getStatus() %>
        </div>
        
        <p class="error-details">
            <% if (response.getStatus() == 404) { %>
                抱歉，您访问的页面不存在。请检查URL是否正确。
            <% } else if (response.getStatus() == 500) { %>
                服务器遇到了一个内部错误。请稍后重试。
            <% } else { %>
                抱歉，处理您的请求时发生了错误。
            <% } %>
        </p>
        
        <div class="error-buttons">
            <a href="<%= request.getContextPath() %>/index.jsp" class="error-btn">返回首页</a>
            <button onclick="history.back()" class="error-btn error-btn-secondary">返回上页</button>
        </div>
        <% if (exception != null && "true".equals(request.getServletContext().getInitParameter("debug"))) { %>
            <div class="error-debug">
                <strong>错误详情:</strong><br>
                <%= exception.getMessage() %>
                <% 
                StackTraceElement[] stackTrace = exception.getStackTrace();
                if (stackTrace != null && stackTrace.length > 0) {
                    out.println("<br><strong>堆栈跟踪:</strong><br>");
                    for (int i = 0; i < Math.min(stackTrace.length, 10); i++) {
                        out.println(stackTrace[i].toString() + "<br>");
                    }
                }
                %>
            </div>
        <% } %>
    </div>
</body>
</html>