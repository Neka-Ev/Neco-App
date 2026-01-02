function loadFragment(page) {
    var url = new URL(window.location.href);
    url.searchParams.set('page', page);
    window.location.href = url.toString();
}

// 渲染私聊会话列表
function renderChatSessions() {
    const container = document.getElementById('chat-sessions-list');
    container.innerHTML = '';

    if (window.chatManager && chatManager.chatSessions) {
        chatManager.chatSessions.forEach(session => {
            const otherUser = chatManager.getOtherUser(session);
            const unreadCount = chatManager.getUnreadCount(session);
            const displayName = "@" + otherUser.username;

            const sessionElement = document.createElement('a');
            sessionElement.className = 'nav-item session-item';
            sessionElement.href = '#';
            sessionElement.innerHTML =
                '<div style="display: flex; justify-content: space-between; align-items: center;">' +
                '<span>' +  ( displayName || '未知用户') + '</span>' +
                (unreadCount > 0 ? '<span class="badge">' + unreadCount + '</span>' : '') +
                '</div>';

            sessionElement.onclick = (e) => {
                e.preventDefault();
                openPrivateChat(otherUser.id, otherUser.username, session.id);
            };

            container.appendChild(sessionElement);
        });
    }
}

// 打开私聊界面
function openPrivateChat(userId, username, sessionId) {
    if (window.chatManager) {
        chatManager.setCurrentSession(sessionId);
        chatManager.clearUnreadCount(sessionId);
    }

    var url = new URL(window.location.href);
    url.searchParams.set('page', 'private-chat');
    url.searchParams.set('userId', userId);
    url.searchParams.set('username', username);
    url.searchParams.set('sessionId', sessionId);
    window.location.href = url.toString();
}

// 定期刷新会话列表
setInterval(function() {
    if (window.chatManager) {
        // 检查是否在私聊界面中，如果是则跳过刷新
        const isInPrivateChat = window.location.search.includes('page=private-chat');
        if (!isInPrivateChat) {
            chatManager.loadChatSessions().then(renderChatSessions);
        } else {
            console.log('在私聊界面中，跳过会话列表刷新');
        }
    }
    if (window.groupChatManager && window.groupChatManager.loadGroupSessions && window.groupChatManager.renderGroupSessions) {
        const isInGroupChat = window.location.search.includes('page=group-chat');
        if (!isInGroupChat) {
            window.groupChatManager.loadGroupSessions().then(() => window.groupChatManager.renderGroupSessions());
        } else {
            console.log('在群聊界面中，跳过群聊会话刷新');
        }
    }
}, 10000);// 每10秒刷新一次


// 请求通知权限
function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
}