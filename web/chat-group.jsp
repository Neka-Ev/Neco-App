<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    String groupId = request.getParameter("groupId");
    String groupName = request.getParameter("groupName");

    // 验证群聊参数
    if (groupId == null || groupName == null) {
        out.print("<div>无效的群聊参数</div>");
        return;
    }

    if (currentUser == null) {
        out.print("<div>请先登录</div>");
        return;
    }
%>
<div class="ai-chat-layout">
    <div class="private-chat-container">
        <div class="chat-header">
            <div>
                <h3>📣 <%= groupName %></h3>
                <span id="group-status" class="user-status">群聊</span>
            </div>
            <div class="function-buttons">
                <button id="group-manage-btn" class="function-button">
                    群聊管理
                </button>
                <button id="delete-group-btn" class="function-button" style="display:none;">
                    删除群聊
                </button>
            </div>
        </div>
        <div id="messages" class="messages-container"></div>

        <form id="sendForm" class="message-form">
            <textarea id="msgInput" name="content" class="message-input message-input-multiline" rows="2" placeholder="发送群消息...（Enter 换行，Ctrl+Enter 发送）" required></textarea>
            <div class="submit-area" style="display: flex;flex-direction: column;align-items: center;">
                <button type="submit" class="send-button">发送</button>
            <label class="message-code-flag">
                <input type="checkbox" id="isCodeMessage" /> 代码消息
            </label>
           </div>
        </form>
    </div>
</div>

<!-- 群聊管理面板 Modal -->
<div id="group-manage-modal" class="modal" style="display:none;">
    <div class="modal-content manage-chat-modal-content">
        <div class="modal-header">
            <h2>群管理</h2>
            <span class="close-modal">&times;</span>
        </div>
        <div class="manage-chat-modal-body">
            <div class="tabs">
                <button class="tab-btn active" data-tab="member-manage">成员管理</button>
                <button class="tab-btn" data-tab="message-manage">消息管理</button>
            </div>
            <div id="member-manage" class="tab-content active">
                <div class="search-panel">
                    <input id="search-member-input" type="text" placeholder="搜索成员" class="form-input" />
                    <button id="btn-search-member" class="btn-primary">搜索</button>
                    <button id="btn-invite-member" class="btn-primary" style="margin-left:8px;">邀请好友</button>
                </div>
                <div class="manage-chat-table-container">
                    <table class="manage-chat-table">
                        <thead>
                            <tr><th>头像</th><th>用户名</th><th>昵称</th><th>角色</th><th>操作</th></tr>
                        </thead>
                        <tbody id="member-table-body"></tbody>
                    </table>
                </div>
            </div>
            <div id="message-manage" class="tab-content">
                <!-- 消息管理 Tab -->
                <form id="search-form" class="search-form manage-chat-search-form">
                    <div class="manage-chat-search-row">
                        <input type="text" id="search-content" placeholder="按内容查询" class="form-input manage-chat-input-content">
                        <select id="search-sender" class="form-select">
                            <option value="">所有发送者</option>
                            <!-- 动态填充成员列表 -->
                        </select>
                        <select id="search-is-code" class="form-select">
                            <option value="">所有类型</option>
                            <option value="0">普通消息</option>
                            <option value="1">代码消息</option>
                        </select>
                    </div>
                    <div class="manage-chat-search-row-bottom">
                        <input type="date" id="search-start-date" class="form-input" title="开始时间" style="width: 44%">
                        <span>至</span>
                        <input type="date" id="search-end-date" class="form-input manage-chat-input-time" title="结束时间" style="width: 44%">
                        <button type="button" id="btn-search-msg" class="btn-primary">查询</button>
                        <button type="button" id="btn-delete-batch" class="btn-danger manage-chat-btn-batch-delete" style="display:none;">批量删除</button>
                    </div>
                </form>

                <div class="table-container manage-chat-table-container">
                    <table class="data-table manage-chat-table">
                        <thead>
                            <tr>
                                <th class="manage-chat-th-checkbox"><input type="checkbox" id="check-all-msg"></th>
                                <th class="manage-chat-th-sender">发送者</th>
                                <th class="manage-chat-th-time">时间</th>
                                <th>内容</th>
                                <th class="manage-chat-th-action">操作</th>
                            </tr>
                        </thead>
                        <tbody id="msg-table-body"></tbody>
                    </table>
                </div>
                <div class="pagination manage-chat-pagination" id="msg-pagination"></div>
            </div>
        </div>
    </div>
</div>

<div id="invite-modal" class="modal" style="display:none;">
    <div class="modal-content" style="max-width:480px;">
        <div class="modal-header">
            <h2>邀请好友入群</h2>
            <span id="invite-close" class="close-modal">&times;</span>
        </div>
        <div class="manage-chat-modal-body" style="max-height:60vh; overflow-y:auto;">
            <div id="invite-friends-list"></div>
        </div>
    </div>
</div>

<script src="assets/js/extra/highlight.min.js"></script>
<script src="assets/js/local/chat-group.js"></script>
<link rel="stylesheet" href="assets/css/github-dark.min.css" />
<script>
    // 页面加载完成后初始化
    $(document).ready(function() {
        window.groupChatManager = new GroupChatManager(
            parseInt('<%= groupId %>'),
            '<%= groupName %>',
            <%= currentUser.getId() %>,
            '<%= currentUser.getUsername() %>'
        );
        window.groupChatManager.init();
    });

    $('#btn-invite-member').on('click', function() { if (window.groupChatManager) window.groupChatManager.openInviteDialog(); });
</script>