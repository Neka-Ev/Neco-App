<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.model.User" %>
<%@ page import="com.example.chat.dao.UserDao" %>
<%
    User currentUser = (User) session.getAttribute("user");
    String privateUserId = request.getParameter("userId");
    String privateUsername = request.getParameter("username");

    // 验证私聊对象参数
    if (privateUserId == null || privateUsername == null) {
        out.print("<div>无效的私聊对象</div>");
        return;
    }

    UserDao userDao = new UserDao();
    User targetUser = userDao.findById(Integer.parseInt(privateUserId));
    if (targetUser == null || !targetUser.getUsername().equals(privateUsername)) {
        out.print("<div>用户不存在</div>");
        return;
    }
    String privateUserDisplayName = targetUser.getDisplayName();
    // 不能与自己私聊
    if (currentUser.getId() == targetUser.getId()) {
        out.print("<div>不能与自己进行私聊</div>");
        return;
    }
%>
<div class="ai-chat-layout">
    <div class="private-chat-container">
        <div class="chat-header">
            <div class="partner-header">
                <h3 id="partner-title">与 <%= privateUserDisplayName %> 的私聊</h3>
                <span id="user-status" class="user-status">加载中...</span>
            </div>
            <div class="function-buttons">
                <button id="manage-chat-btn" class="function-button">
                    管理会话
                </button>
                <button id="delete-session-btn" class="function-button">
                    删除会话
                </button>
            </div>

        </div>

        <div id="messages" class="messages-container"></div>

        <form id="sendForm" class="message-form">
            <textarea id="msgInput" name="content" class="message-input message-input-multiline" rows="2" placeholder="发送私聊消息...（Enter 发送，Ctrl+Enter 换行）" required></textarea>
            <div class="submit-area" style="display: flex;flex-direction: column;align-items: center;">
                <div class="submit-area" style="display: flex;flex-direction: column;align-items: center;">
                    <button type="submit" class="send-button">发送</button>
                    <label class="message-code-flag">
                        <input type="checkbox" id="isCodeMessage" /> 代码消息
                    </label>
                </div>
            </div>
            
        </form>
    </div>
</div>
<script src="assets/js/extra/highlight.min.js"></script>
<link rel="stylesheet" href="assets/css/github-dark.min.css" />
<script src="assets/js/local/chat-private.js"></script>

<!-- 管理会话面板 -->
<div id="manage-chat-modal" class="modal" style="display: none;">
    <div class="modal-content manage-chat-modal-content">
        <div class="modal-header">
            <h2>管理会话记录</h2>
            <span class="close-modal">&times;</span>
        </div>
        <div class="modal-body manage-chat-modal-body">
            <!-- 查询表单 -->
            <form id="search-form" class="search-form manage-chat-search-form">
                <div class="manage-chat-search-row">
                    <input type="text" name="content" placeholder="按内容查询" class="form-input manage-chat-input-content">
                    <select name="senderId" class="form-select">
                        <option value="">所有发送者</option>
                        <option value="<%= currentUser.getId() %>">我</option>
                        <option value="<%= privateUserId %>"><%= privateUserDisplayName %></option>
                    </select>
                    <select name="isCode" class="form-select">
                        <option value="">所有类型</option>
                        <option value="0">普通消息</option>
                        <option value="1">代码消息</option>
                    </select>
                </div>
                <div class="manage-chat-search-row-bottom">
                    <input type="date" name="startTime" class="form-input" title="开始时间" style="width: 44%">
                    <span>至</span>
                    <input type="date" name="endTime" class="form-input manage-chat-input-time" title="结束时间" style="width: 44%">
                    <button type="submit" class="btn-primary">查询</button>
                    <button type="button" id="batch-delete-btn" class="btn-danger manage-chat-btn-batch-delete">批量删除</button>
                </div>
            </form>

            <!-- 结果表格 -->
            <div class="table-container manage-chat-table-container">
                <table class="data-table manage-chat-table">
                    <thead>
                        <tr>
                            <th class="manage-chat-th-checkbox"><input type="checkbox" id="select-all-msgs"></th>
                            <th class="manage-chat-th-sender">发送者</th>
                            <th class="manage-chat-th-time">时间</th>
                            <th>内容</th>
                            <th class="manage-chat-th-action">操作</th>
                        </tr>
                    </thead>
                    <tbody id="search-results-body">
                        <!-- 动态填充 -->
                    </tbody>
                </table>
            </div>

            <!-- 分页控件 -->
            <div class="pagination manage-chat-pagination" id="search-pagination">
                <!-- 动态填充 -->
            </div>
        </div>
    </div>
</div>


<input type="hidden" id="partnerId" value="<%= privateUserId %>">
<input type="hidden" id="partnerName" value="<%= privateUserDisplayName %>">
<input type="hidden" id="partnerAvatar" value="<%= targetUser.getAvatarUrl() != null ? targetUser.getAvatarUrl() : "assets/images/avatars/default.jpg" %>">


<script>
    // 页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', function() {
        let privateUserId = parseInt('<%= privateUserId %>');
        let privateUsername = '<%= privateUsername %>';
        let privateUserDisplayName = '<%= privateUserDisplayName %>';
        let currentUserId = <%= currentUser.getId() %>;
        let currentUsername = '<%= currentUser.getUsername() %>';
        window.privateChatManager = new PrivateChatManager(privateUserId, privateUsername, privateUserDisplayName, currentUserId, currentUsername);
        window.privateChatManager.init();

        // 初始化聊天管理器
        chatManager.init(<%= currentUser.getId() %>, '<%= currentUser.getUsername() %>');
    });
</script>
