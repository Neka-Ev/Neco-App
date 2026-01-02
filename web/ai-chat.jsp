<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="chat-layout ai-chat-layout">
    <div class="chat-main ai-main">
        <div class="ai-header">
            <h3 class="content-title">与Neco聊天</h3>
            <div class="ai-header-buttons">
                <button id="new-ai-session" class="primary-btn" type="button">新会话</button>
                <select id="ai-session-select" class="ai-session-select"></select>
                <button id="ai-manage-sessions" class="secondary-btn" type="button">管理会话</button>
            </div>
        </div>
        <div id="ai-messages" class="messages-container ai-messages-container">

        </div>
        <form id="ai-send-form" class="message-form">
            <input id="ai-input" type="text" class="message-input" placeholder="输入要与Neco对话的内容" required />
            <button type="submit" class="send-button">发送</button>
        </form>
    </div>
</div>

<!-- 会话管理面板 -->
<div id="ai-session-panel" class="ai-session-panel" style="display:none;">
    <div class="ai-session-panel-content">
        <div class="ai-session-panel-header">
            <h4>会话管理</h4>
            <button id="ai-session-panel-close" type="button" class="close-btn">×</button>
        </div>
        <div class="ai-session-panel-body">
            <table class="ai-session-table">
                <thead>
                    <tr>
                        <th>标题</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="ai-session-table-body">
                    <!-- 由 JS 动态填充 -->
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="assets/js/extra/markdown-it.min.js"></script>
<script src="assets/js/local/ai-chat.js"></script>
