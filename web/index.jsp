<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.model.User" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link id="favicon" rel="icon" href="assets/images/icons/logo-light.png" />
    <title>Neco</title>
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
    <link rel="stylesheet" href="./assets/css/common.css">
    <link rel="stylesheet" href="./assets/css/chat.css">
    <link rel="stylesheet" href="./assets/css/auth.css">
    <script src="assets/js/extra/jquery-3.7.1.min.js"></script>
</head>
<%!
    private String getTimeBriefNow() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hours = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hours >= 0 && hours < 5) return "深夜了，注意休息...";
        if (hours >= 5 && hours < 8) return "早上好，新的一天开始了~";
        if (hours >= 8 && hours < 11) return "上午好，新的一天加油哦~";
        if (hours >= 11 && hours < 13) return "中午好，记得吃饭~";
        if (hours >= 13 && hours < 17) return "下午好，继续努力！";
        if (hours >= 17 && hours < 19) return "傍晚了，下班收工！";
        return "晚上好，放松一下吧~";
    }
%>
<%
    String timeBriefNow = getTimeBriefNow();
%>
<body>
    <div class="container">
        <div class="header">
            <a class="logo" href="index.jsp">
                <img id="logo-image-small"
                     src="assets/images/icons/logo-light.png"
                     alt="Neco Logo"
                     class="logo-image-small"
                     data-src-light="assets/images/icons/logo-light.png"
                     data-src-dark="assets/images/icons/logo-dark.png" />
                Neco
            </a>
            <script>
                (function(){
                    const imgSmall = document.getElementById('logo-image-small');
                    if (!imgSmall) return;
                    function updateLogoSmall(){
                        const saved = (localStorage.getItem('necoTheme')||'').replace(/"/g,'');
                        const isDark = document.documentElement.classList.contains('theme-dark') || saved === 'dark';
                        imgSmall.src = isDark ? imgSmall.dataset.srcDark : imgSmall.dataset.srcLight;
                    }
                    updateLogoSmall();
                    new MutationObserver(updateLogoSmall).observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
                })();
            </script>
            <div class="time-message"><%= timeBriefNow %></div>
            <div class="header-tools">
                <button type="button" class="theme-toggle-btn" data-theme-toggle>
                    <span class="theme-toggle-icon">🌙</span>
                    <span data-theme-label>切换至暗色</span>
                </button>
                <div class="auth-section">
                 <% 
                     User currentUser = (User) session.getAttribute("user");
                     if (currentUser != null) {
                         String headerAvatarUrl = currentUser.getAvatarUrl();
                         if (headerAvatarUrl == null || headerAvatarUrl.isEmpty()) {
                             headerAvatarUrl = "assets/images/avatars/default.jpg";
                         }
                 %>
                     <div class="user-info" style="display: flex; align-items: center; gap: 10px;">
                         <a href="profile.jsp" title="个人主页" style="display: flex; align-items: center;">
                             <img src="<%= headerAvatarUrl %>" alt="Avatar" style="width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 2px solid var(--color-border);">
                         </a>
                         <span>欢迎，<strong><%= currentUser.getUsername() %></strong></span>
                     </div>
                     <a href="<%= request.getContextPath() %>/api/auth/logout" class="auth-btn logout-btn">登出</a>
                 <% } else { %>
                     <a href="#" onclick="loadFragment('login')" class="auth-btn login-btn">登录</a>
                     <a href="#" onclick="loadFragment('register')" class="auth-btn register-btn">注册</a>
                 <% } %>
                </div>
             </div>
         </div>

        <div class="main-content">
            <% if (currentUser != null) { %> 
            <div class="sidebar">
            <a href="#" class="nav-item <%= "users".equals(request.getParameter("page")) ? "active" : "" %>" onclick="loadFragment('users')">寻找码友</a>
                <a href="#" class="nav-item <%= "friends".equals(request.getParameter("page")) ? "active" : "" %>" onclick="loadFragment('friends')">认识的人</a>
                <a href="#" class="nav-item <%= "square".equals(request.getParameter("page")) ? "active" : "" %>" onclick="loadFragment('square')">技术广场</a>
                <a href="#" class="nav-item <%= "ai-chat".equals(request.getParameter("page")) ? "active" : "" %>" onclick="loadFragment('ai-chat')">问问Neco</a>

                <!-- 私聊会话列表 -->
                <div id="chat-sessions-container" style="display: none;">
                    <h3 style="padding: 0 20px 10px 20px; margin: 15px 0 10px 0; border-bottom: 1px solid #4a6278; font-size: 14px; color: #bdc3c7;">
                        正在进行的聊天
                    </h3>
                    <div id="chat-sessions-list">
                        <!-- 动态加载的私聊会话将显示在这里 -->
                    </div>
                </div>
                
                <!-- 群组会话列表 -->
                <div id="group-sessions-container" style="display: none;">
                    <h3 style="padding: 0 20px 10px 20px; margin: 15px 0 10px 0; border-bottom: 1px solid #4a6278; font-size: 14px; color: #bdc3c7;">
                        我的群组
                    </h3>
                    <div id="group-sessions-list">
                        <!-- 动态加载的群组会话将显示在这里 -->
                    </div>
                </div>
            </div>
            <% } %> 

            <% 
                String pageParam = request.getParameter("page");
                if (currentUser == null && pageParam == null) {
            %> 
            <div class="content-area" id="content">
                    <div class="welcome-message">
                        <h2>欢迎来到 Neco</h2>
                        <p>技术分享信息流社交平台</p>
                        <div class="feature-grid">
                        <div class="feature-card">
                            <h4>技术分享</h4>
                            <p>分享你的技术见解和经验</p>
                        </div>
                        <div class="feature-card">
                            <h4>实时交流</h4>
                            <p>与技术爱好者实时讨论</p>
                        </div>
                        <div class="feature-card">
                            <h4>关注互动</h4>
                            <p>关注感兴趣的技术话题和用户</p>
                        </div>
                        <div class="feature-card">
                            <h4>知识沉淀</h4>
                            <p>记录和整理技术学习成果</p>
                        </div>
                    </div>
                    </div>
                <% 
                    } else if (pageParam != null) {
                        if (pageParam.equals("private-chat")) {
                %> 
                    <div class="content-area" id="content">
                    <jsp:include page="chat-private.jsp" />
                <% 
                        } else if (pageParam.equals("users")) {
                %> 
                    <div class="content-area-auth" id="content">
                    <jsp:include page="users.jsp" />
                <% 
                        } else if (pageParam.equals("login")) {
                %> 
                    <div class="content-area-auth" id="content">
                    <jsp:include page="login.jsp" />
                <% 
                        } else if (pageParam.equals("register")) {
                %> 
                    <div class="content-area-auth" id="content">
                    <jsp:include page="register.jsp">
                        <jsp:param name="embedded" value="true" />
                    </jsp:include>
                <% 
                        } else if (pageParam.equals("ai-chat")) {
                %> 
                    <div class="content-area" id="content">
                    <jsp:include page="ai-chat.jsp" />
                <% 
                        } else if (pageParam.equals("friends")) {
                %> 
                    <div class="content-area" id="content">
                    <jsp:include page="friends.jsp" />
                <% 
                        } else if (pageParam.equals("square")) {
                %> 
                    <div class="content-area" id="content">
                    <jsp:include page="square.jsp" />
                <% 
                        } else if (pageParam.equals("group-chat")) {
                %> 
                    <div class="content-area" id="content">
                    <jsp:include page="chat-group.jsp" />
                <% 
                        } else if (pageParam.equals("create-group")) {
                %> 
                    <div class="content-area" id="content">
                    <jsp:include page="create-group.jsp" />
                <% 
                        }
                    } else {
                %> 
                   <div class="content-area" id="content">
                    <div class="welcome-message">
                        <img id="logo-image-large"
                             src="assets/images/icons/logo-light.png"
                             alt="Neco Logo"
                             class="logo-image-large"
                             data-src-light="assets/images/icons/logo-light.png"
                             data-src-dark="assets/images/icons/logo-dark.png" />
                        <script>
                            (function(){
                                const imgLarge = document.getElementById('logo-image-large');
                                if (!imgLarge) return;
                                function updateLogoSmall(){
                                    const saved = (localStorage.getItem('necoTheme')||'').replace(/"/g,'');
                                    const isDark = document.documentElement.classList.contains('theme-dark') || saved === 'dark';
                                    imgLarge.src = isDark ? imgLarge.dataset.srcDark : imgLarge.dataset.srcLight;
                                }
                                updateLogoSmall();
                                new MutationObserver(updateLogoSmall).observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
                            })();
                        </script>
                        <h2>欢迎回来，<%= currentUser.getUsername() %>！</h2>
                        <p>探索技术分享，参与交流讨论</p>
                    </div>
                <% } %> 
            </div>
        </div>
    </div>

    <script src="assets/js/local/index.js"></script>
    <script>
        // 页面加载完成后初始化
        document.addEventListener('DOMContentLoaded', function() {
            <% if (currentUser != null) { %> 
            // 初始化聊天管理器
            chatManager.init(parseInt('<%= currentUser.getId() %>'), '<%= currentUser.getUsername() %>');

            // 请求通知权限
            requestNotificationPermission();

            // 连接WebSocket
            wsManager.connect();

            // 加载会话列表
            chatManager.loadChatSessions().then(renderChatSessions);
            
            // 加载群组列表
            loadGroupSessions();
            <% } %> 
        });
        
        // 加载群组会话列表
        async function loadGroupSessions() {
            try {
                const response = await fetch('/api/groups/my');
                if (!response.ok) {
                    throw new Error(`HTTP \${response.status}`);
                }
                
                const groups = await response.json();
                renderGroupSessions(groups);
                
                if (groups.length > 0) {
                    const container = document.getElementById('group-sessions-container');
                    if (container) {
                        container.style.display = 'block';
                    }
                }
                
            } catch (error) {
                console.error('加载群组失败:', error);
            }
        }
        
        // 渲染群组会话列表
        function renderGroupSessions(groups) {
            const container = document.getElementById('group-sessions-list');
            if (!container) return;

            container.innerHTML = '';

            groups.forEach(group => {
                const sessionElement = document.createElement('a');
                sessionElement.className = 'nav-item session-item';
                sessionElement.href = '#';
                sessionElement.innerHTML = `
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>📣 \${group.groupName}</span>
                    </div>
                `;
                
                sessionElement.onclick = (e) => {
                    e.preventDefault();
                    openGroupChat(group.groupId, group.groupName);
                };

                container.appendChild(sessionElement);
            });
        }
        
        // 打开群聊界面
        function openGroupChat(groupId, groupName) {
            const url = new URL(window.location.href);
            url.searchParams.set('page', 'group-chat');
            url.searchParams.set('groupId', groupId);
            url.searchParams.set('groupName', groupName);

            window.location.href = url.toString();
        }
    </script>
    <script src="assets/js/global/theme.js"></script>
    <script src="assets/js/global/utils.js"></script>
    <script src="assets/js/global/websocket.js"></script>
    <script src="assets/js/global/chat-manager.js"></script>
</body>
</html>

