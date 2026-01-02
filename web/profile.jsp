<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.model.User" %>
<%@ page import="com.example.chat.dao.UserDao" %>
<%@ page import="com.example.chat.servlet.FriendServlet" %>
<%
    User currentUser = (User) session.getAttribute("user");
    String userIdParam = request.getParameter("userId");
    User targetUser = currentUser;
    boolean isSelf = true;
    if (userIdParam != null) {
        try {
            int targetId = Integer.parseInt(userIdParam);
            if (targetId != currentUser.getId()) {

                UserDao userDaoView = new UserDao();
                targetUser = userDaoView.findById(targetId);
                isSelf = false;
            }
        } catch (Exception ignored) {}
    }
    if (targetUser == null) {
        out.print("<div>用户不存在</div>");
        return;
    }
    String avatarUrl = targetUser.getAvatarUrl();
    if (avatarUrl == null || avatarUrl.isEmpty()) {
        avatarUrl = "assets/images/avatars/default.jpg";
    }
    boolean isFriend = false;
    try { isFriend = FriendServlet.isFriend(currentUser.getId(), targetUser.getId()); } catch (Exception ignore) {}
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人主页 - Neco</title>
    <script>
        (function() {
            try {
                var savedTheme = localStorage.getItem('necoTheme');
                if (savedTheme && savedTheme.replace(/"/g, '') === 'dark') {
                    document.documentElement.classList.add('theme-dark');
                }
            } catch (e) {}
        })();
    </script>
    <link rel="stylesheet" href="assets/css/common.css">
    <link rel="stylesheet" href="assets/css/profile.css">
    <script src="assets/js/extra/jquery-3.7.1.min.js"></script>
</head>
<body>
    <div class="container">
        <a href="index.jsp" class="back-link">← 返回首页</a>

        <div class="profile-container">
            <div class="profile-header">
                <div class="profile-avatar-wrapper">
                    <img src="<%= avatarUrl %>" alt="Avatar" class="profile-avatar" id="avatar-preview">
                </div>
                <div class="profile-info">
                    <div style="display: flex; flex-direction:row; ">
                        <div class="profile-username"><%= targetUser.getDisplayName() %></div>
                        <% if (!isSelf) { %>
                        <button id="add-friend-btn" class="btn-save" style="<%= isFriend?"background-color:var(--color-scroll-thumb)":"" %>"
                                <%= isFriend ? "disabled" : "" %> data-id="<%= targetUser.getId() %>" data-name="<%= targetUser.getUsername() %>"><%= isFriend ? "已认识" : "认识一下" %></button>
                        <% } %>
                    </div>
                    <div class="profile-display-name">@<%= targetUser.getUsername() %></div>
                    <% if (targetUser.getBio() != null && !targetUser.getBio().isEmpty()) { %>
                        <div class="profile-bio"><%= targetUser.getBio() %></div>
                    <% } else { %>
                        <div class="profile-bio" style="font-style: italic; color: var(--color-text-muted);">这个人很懒，什么都没有写...</div>
                    <% } %>

                </div>
            </div>

            <% if (isSelf) { %>
            <form id="profile-form" class="profile-form" enctype="multipart/form-data">
                <div class="form-group">
                    <label class="form-label">用户名</label>
                    <input type="text" class="form-control" value="<%= currentUser.getUsername() %>" readonly>
                    <small style="color: var(--color-text-muted); font-size: 0.8rem;">用户名不可修改</small>
                </div>

                <div class="form-group">
                    <label class="form-label" for="displayName">显示名称</label>
                    <input type="text" id="displayName" name="displayName" class="form-control" value="<%= currentUser.getDisplayName() %>" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="bio">个人简介 / 爱好</label>
                    <textarea id="bio" name="bio" class="form-control" placeholder="介绍一下你自己..."><%= currentUser.getBio() != null ? currentUser.getBio() : "" %></textarea>
                </div>

                <div class="form-group">
                    <label class="form-label">更换头像</label>
                    <label for="avatar-upload" class="avatar-upload-label">选择图片...</label>
                    <input type="file" id="avatar-upload" name="avatar" accept="image/*" style="display: none;">
                    <span id="file-name" style="margin-left: 10px; font-size: 0.9rem; color: var(--color-text-muted);"></span>
                </div>
                <div class="form-group">
                    <label class="form-label" for="oldPassword">原密码</label>
                    <input type="password" id="oldPassword" name="oldPassword" class="form-control" placeholder="输入原密码以修改密码">
                </div>
                <div class="form-group">
                    <label class="form-label" for="newPassword">新密码</label>
                    <input type="password" id="newPassword" name="newPassword" class="form-control" placeholder="输入新密码">
                </div>

                <button type="submit" class="btn-save">保存修改</button>
            </form>
            <% } %>
        </div>
    </div>

    <script>
        // 头像预览
        <% if (isSelf) { %>
        document.getElementById('avatar-upload').addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                document.getElementById('file-name').textContent = file.name;
                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('avatar-preview').src = e.target.result;
                }
                reader.readAsDataURL(file);
            }
        });

        // 表单提交
        document.getElementById('profile-form').addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = new FormData(this);
            const btn = this.querySelector('.btn-save');
            const originalText = btn.textContent;

            btn.disabled = true;
            btn.textContent = '保存中...';

            fetch('api/profile/update', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('保存成功！');
                    window.location.reload();
                } else {
                    alert('保存失败: ' + (data.error || '未知错误'));
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('发生错误，请重试');
            })
            .finally(() => {
                btn.disabled = false;
                btn.textContent = originalText;
            });
        });
        <% } %>

        // 认识一下（仅访客看到）
        const addBtn = document.getElementById('add-friend-btn');
        if (addBtn) {
            addBtn.addEventListener('click', function() {
                if (addBtn.disabled) return;
                const uid = addBtn.getAttribute('data-id');
                const uname = addBtn.getAttribute('data-name');
                if (!confirm('确定要添加 @' + uname + ' 为好友吗？')) return;
                addBtn.disabled = true;
                addBtn.textContent = '请求中...';
                fetch('/friends/add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ targetUserId: parseInt(uid) })
                }).then(res => res.json())
                  .then(data => {
                      if (data.success) {
                          addBtn.textContent = '已认识';
                          window.location.reload();
                      } else {
                          alert('添加好友失败: ' + (data.message || '未知错误'));
                          addBtn.disabled = false;
                          addBtn.textContent = '认识一下';
                      }
                  }).catch(() => {
                      alert('添加好友失败');
                      addBtn.disabled = false;
                      addBtn.textContent = '认识一下';
                  });
            });
        }
    </script>
</body>
</html>
