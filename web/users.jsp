<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.dao.UserDao" %>
<%@ page import="com.example.chat.model.User" %>
<%@ page import="com.example.chat.servlet.FriendServlet" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户列表 - Neco</title>
</head>
<body class="auth-page" style="padding-top: 0;">
    <div class="auth-container" style="max-width: 1000px; width: 100%;">
        <h1 class="content-title">用户列表</h1>

        <%
            Object userObj = session.getAttribute("user");
            User currentUser = (User) userObj;
            try {
                UserDao userDao = new UserDao();
                List<User> allUsers = userDao.findAllUsers();
        %>
        <div class="users-count">
            <p>当前用户总数：<strong><%= allUsers.size() %></strong> 人</p>
        </div>

        <div id="online-users-list" class="users-grid">
            <%
                for (User user : allUsers) {
                    if (currentUser != null && user.getId() == currentUser.getId()) {
                        continue;
                    }
                    boolean isFriend = false;
                    try { isFriend = FriendServlet.isFriend(currentUser.getId(), user.getId()); } catch (Exception ignore) {}
                    String avatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : "assets/images/avatars/default.jpg";
            %>
            <div class="user-card">
                <img class="user-avatar" src="<%= avatar %>" alt="avatar" onerror="this.src='assets/images/avatars/default.jpg'" />
                <h4 class="user-name-display"><%= user.getDisplayName() %></h4>
                <h5 class="user-name" style="">@<%= user.getUsername() %></h5>
                <div class="user-actions">
                    <button class="add-friend-btn" <%= isFriend ? "disabled": "" %> style="<%= isFriend?"background-color:var(--color-scroll-thumb)":"" %>"  onclick="addFriend(<%= user.getId() %>, '<%= user.getUsername() %>', event)">
                        <%= isFriend ? "已认识" : "认识一下" %>
                    </button>
                </div>
            </div>
            <%
                }
            %>
        </div>

        <% if (allUsers.isEmpty()) { %>
        <div class="no-users-message">
            <p>当前没有用户，稍后再试...</p>
        </div>
        <% } %>

        <%
        } catch (Exception e) {
            e.printStackTrace();
        %>
        <div class="error-message">
            <p>加载用户列表时发生错误。</p>
        </div>
        <%
            }
        %>
    </div>
    <script>
         document.addEventListener('DOMContentLoaded', function() {
             <% if (userObj != null) { %>
                chatManager.init(<%= ((User)userObj).getId() %>, '<%= ((User)userObj).getUsername() %>');
                wsManager.connect();
             <% } %>
         });

        function addFriend(userId, username, event) {
            event.stopPropagation();
            if (!confirm('确定要与 @' + username + ' 建立关系吗？')) {
                return;
            }
            fetch('/friends/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ targetUserId: userId })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('建立成功！');
                    const button = event.target;
                    button.disabled = true;
                    button.innerText = '已认识';
                    button.classList.add('friend-added');
                    location.reload();// 刷新界面
                } else {
                    alert('建立失败: ' + (data.message || '未知错误'));
                }
            })
            .catch(error => {
                console.error('建立失败:', error);
                alert('建立失败，请重试');
            });
        }
    </script>
</body>
</html>
