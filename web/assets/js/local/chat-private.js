// 私聊页面专用功能
class PrivateChatManager {
    constructor(pUserId, pUsername, pUserDisplayName, cUserId, cUserName ) {
        this.currentPrivateUserId =  pUserId;
        this.currentPrivateUsername = pUsername;
        this.currentPrivateUserDisplayName = pUserDisplayName;
        this.currentUserId = cUserId;
        this.currentUsername = cUserName;
        this.statusInterval = null;
        this.pageSize = 5; // 管理面板每页显示的消息数量
    }

    init() {
        // 初始化聊天管理器
        chatManager.init(this.currentUserId, this.currentUsername);
        // 注册私聊消息监听器
        chatManager.registerPrivateChatListener(this.currentPrivateUserId, this.handlePrivateMessage.bind(this));
        // 设置标题头像与昵称
        this.updateHeaderTitle();
        // 设置事件监听器
        this.setupEventListeners();
        // 加载历史消息
        this.loadMessages();
        // 连接WebSocket
        wsManager.connect();
        // 设置连接成功后的回调
        wsManager.on('connected', this.onConnected.bind(this));
    }

    updateHeaderTitle() {
        const partnerName = this.currentPrivateUserDisplayName || $('#partnerName').val();
        const partnerAvatar = $('#partnerAvatar').val() || 'assets/images/avatars/default.jpg';
        if (partnerName) {
            const avatarHtml = partnerAvatar ? '<img class="avatar-small" src="' + partnerAvatar + '" onerror="this.src=\'assets/images/avatars/default.jpg\'" /> ' : '';
            $('.chat-header h3').html(avatarHtml + '与 ' + partnerName + ' 的私聊');
        }
    }

    setupEventListeners() {
        // 发送消息表单
        document.getElementById('sendForm').addEventListener('submit', this.handleSendMessage.bind(this));

        // 键盘行为：Enter 换行，Ctrl+Enter 发送
        const self = this;
        $('#msgInput').on('keydown', function (e) {
            if (e.key === 'Enter') {
                if (e.ctrlKey || e.metaKey) {
                    // Ctrl+Enter / Cmd+Enter 换行
                    e.preventDefault();
                    const start = this.selectionStart;
                    const end = this.selectionEnd;
                    const val = this.value;
                    this.value = val.substring(0, start) + '\n' + val.substring(end);
                    // 将光标移动到新行之后
                    this.selectionStart = this.selectionEnd = start + 1;
                } else {
                    // 普通 Enter 发送
                    e.preventDefault();
                    self.handleSendMessage(e);
                }
            }
        });

        // 确保存在全局独立的菜单面板（浮层）
        this.ensureGlobalMessageMenuPanel();

        // 使用 jQuery 事件委托处理三点菜单点击，显示独立的浮动面板
        $(document).on('click', '#messages .message-menu-toggle', function (e) {
            e.stopPropagation();
            const $bubble = $(this).closest('.message.private-message');
            const messageId = $bubble.data('message-id');
            if (!messageId) {
                return;
            }

            const panel = document.getElementById('message-menu-panel');
            if (!panel) return;

            // 在面板上记录当前消息 ID
            panel.dataset.messageId = messageId;

            // 计算按钮相对于视口的位置
            const rect = this.getBoundingClientRect();
            const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
            const scrollY = window.pageYOffset || document.documentElement.scrollTop;

            // 将面板移动到按钮旁边（右侧稍微往下）
            panel.style.left = (rect.right + scrollX + 8) + 'px';
            panel.style.top = (rect.top + scrollY - 4) + 'px';

            // 显示面板
            panel.style.display = 'block';
        });

        // 全局面板中的“复制”按钮
        $(document).on('click', '#message-menu-panel .message-menu-item-copy', function (e) {
            e.stopPropagation();
            const panel = document.getElementById('message-menu-panel');
            const messageId = panel ? panel.dataset.messageId : null;
            if (!messageId) {
                panel.style.display = 'none';
                return;
            }
            const $bubble = $('#messages .message.private-message[data-message-id="' + messageId + '"]').first();
            let text = '';
            const $codeBlock = $bubble.find('code');
            if ($codeBlock.length > 0) {
                text = $codeBlock.text();
            } else {
                const html = $bubble.find('.message-content').html();
                if (html) {
                    const div = document.createElement('div');
                    div.innerHTML = html.replace(/<br\s*\/?>/gi, '\n');
                    text = div.textContent || div.innerText || '';
                }
            }
            if (text) {
                window.privateChatManager.copyToClipboard(text);
            }
            panel.style.display = 'none';
        });

        // 全局面板中的“删除”按钮
        $(document).on('click', '#message-menu-panel .message-menu-item-delete', function (e) {
            e.stopPropagation();
            const panel = document.getElementById('message-menu-panel');
            const messageId = panel ? panel.dataset.messageId : null;
            if (!messageId) {
                panel.style.display = 'none';
                return;
            }
            if (!confirm('确定删除该条消息吗？')) {
                panel.style.display = 'none';
                return;
            }
            window.privateChatManager.deleteMessage(messageId);
            panel.style.display = 'none';
        });

        // 点击页面其他地方关闭面板
        $(document).on('click', function () {
            const panel = document.getElementById('message-menu-panel');
            if (panel) {
                panel.style.display = 'none';
            }
        });

        // 管理会话按钮
        $('#manage-chat-btn').on('click', () => {
            $('#manage-chat-modal').show();
            this.searchMessages(1); // 默认加载第一页
        });

        // 删除会话按钮
        $('#delete-session-btn').on('click', () => {
            if (confirm('确定要删除此会话吗？删除后将无法恢复，且会话记录也将被删除。')) {
                this.deleteCurrentSession();
            }
        });

        // 关闭模态框
        $('.close-modal').on('click', () => {
            $('#manage-chat-modal').hide();
        });

        // 点击模态框外部关闭
        $(window).on('click', (e) => {
            if ($(e.target).is('#manage-chat-modal')) {
                $('#manage-chat-modal').hide();
            }
        });

        // 查询表单提交
        $('#search-form').on('submit', (e) => {
            e.preventDefault();
            this.searchMessages(1);
        });

        // 全选/取消全选
        $('#select-all-msgs').on('change', function() {
            $('.msg-checkbox').prop('checked', this.checked);
            window.privateChatManager.updateBatchDeleteBtn();
        });

        // 单个复选框变化
        $(document).on('change', '.msg-checkbox', function() {
            const allChecked = $('.msg-checkbox').length === $('.msg-checkbox:checked').length;
            $('#select-all-msgs').prop('checked', allChecked);
            window.privateChatManager.updateBatchDeleteBtn();
        });

        // 批量删除按钮
        $('#batch-delete-btn').on('click', () => {
            const ids = [];
            $('.msg-checkbox:checked').each(function() {
                ids.push($(this).val());
            });
            if (ids.length === 0) return;

            if (confirm(`确定要删除选中的 ${ids.length} 条消息吗？`)) {
                this.batchDeleteMessages(ids);
            }
        });

        // 表格中的复制按钮
        $(document).on('click', '.table-copy-btn', function() {
            const content = $(this).data('content');
            window.privateChatManager.copyToClipboard(decodeURIComponent(content));
        });

        // 表格中的删除按钮
        $(document).on('click', '.table-delete-btn', function() {
            const id = $(this).data('id');
            if (confirm('确定删除该条消息吗？')) {
                window.privateChatManager.deleteMessage(id, true); // true 表示从管理面板删除
            }
        });
    }

    deleteCurrentSession() {
        // 获取当前会话ID
        const sessionId = new URLSearchParams(window.location.search).get('sessionId');
        if (!sessionId) {
            alert('无法获取当前会话ID，请刷新页面重试');
            return;
        }

        $.ajax({
            url: '/api/sessions?id=' + sessionId,
            method: 'DELETE',
            success: () => {
                alert('会话已删除');
                window.location.href = '../../../index.jsp';
            },
            error: () => {
                alert('删除会话失败');
            }
        });
    }

    updateBatchDeleteBtn() {
        const count = $('.msg-checkbox:checked').length;
        if (count > 0) {
            $('#batch-delete-btn').show().text(`批量删除 (${count})`);
        } else {
            $('#batch-delete-btn').hide();
        }
    }

    searchMessages(page) {
        const form = $('#search-form');
        const data = {
            action: 'search',
            private: this.currentPrivateUserId,
            page: page,
            pageSize: this.pageSize,
            content: form.find('input[name="content"]').val(),
            senderId: form.find('select[name="senderId"]').val(),
            isCode: form.find('select[name="isCode"]').val(),
            startTime: form.find('input[name="startTime"]').val(),
            endTime: form.find('input[name="endTime"]').val()
        };

        $.ajax({
            url: '/api/messages',
            method: 'GET',
            data: data,
            success: (res) => {
                this.renderSearchResults(res);
            },
            error: (err) => {
                console.error('Search failed', err);
                alert('查询失败');
            }
        });
    }

    renderSearchResults(res) {
        const tbody = $('#search-results-body');
        tbody.empty();
        $('#select-all-msgs').prop('checked', false);
        this.updateBatchDeleteBtn();

        if (!res.messages || res.messages.length === 0) {
            tbody.html('<tr><td colspan="5" style="text-align:center;">无记录</td></tr>');
            $('#search-pagination').empty();
            return;
        }

        res.messages.forEach(m => {
            const isMe = m.senderId == this.currentUserId;
            const senderName = isMe ? '我' : (m.senderUsername || '未知');
            const contentDisplay = m.content.length > 50 ? m.content.substring(0, 50) + '...' : m.content;
            const encodedContent = encodeURIComponent(m.content);

            const tr = `
                <tr>
                    <td><input type="checkbox" class="msg-checkbox" value="${m.id}"></td>
                    <td>${senderName}</td>
                    <td>${m.createdAt}</td>
                    <td title="${m.isCode ? '[代码消息]' : ''}">${m.isCode ? '<span style="color:#58a6ff">[代码]</span> ' : ''}${this.escapeHtml(contentDisplay)}</td>
                    <td>
                        <button class="action-btn table-copy-btn" data-content="${encodedContent}">复制</button>
                        <button class="action-btn delete table-delete-btn" data-id="${m.id}">删除</button>
                    </td>
                </tr>
            `;
            tbody.append(tr);
        });

        this.renderPagination(res.page, Math.ceil(res.total / res.pageSize));
    }

    renderPagination(current, total) {
        const container = $('#search-pagination');
        container.empty();

        if (total <= 1) return;

        // 上一页
        const prevBtn = $('<button>上一页</button>')
            .prop('disabled', current === 1)
            .click(() => this.searchMessages(current - 1));
        container.append(prevBtn);

        // 页码 (简单实现，显示所有页码或部分)
        let start = Math.max(1, current - 2);
        let end = Math.min(total, current + 2);

        if (start > 1) container.append('<span>...</span>');

        for (let i = start; i <= end; i++) {
            const btn = $(`<button>${i}</button>`)
                .addClass(i === current ? 'active' : '')
                .click(() => this.searchMessages(i));
            container.append(btn);
        }

        if (end < total) container.append('<span>...</span>');

        // 下一页
        const nextBtn = $('<button>下一页</button>')
            .prop('disabled', current === total)
            .click(() => this.searchMessages(current + 1));
        container.append(nextBtn);
    }

    batchDeleteMessages(ids) {
        $.ajax({
            url: '/api/messages?ids=' + ids.join(','),
            method: 'DELETE',
            success: () => {
                alert('删除成功');
                // 刷新当前页
                const currentPage = $('#search-pagination button.active').text() || 1;
                this.searchMessages(parseInt(currentPage));
                this.loadMessages();
            },
            error: () => {
                alert('删除失败');
            }
        });
    }

    deleteMessage(messageId, fromManager = false) {
        if (!fromManager) {
             $.ajax({
                url: '/api/messages?id=' + messageId,
                method: 'DELETE',
                success: function() {
                    // 移除 DOM
                    const $bubble = $('#messages .message.private-message[data-message-id="' + messageId + '"]');
                    $bubble.fadeOut(300, function() { $(this).remove(); });
                },
                error: function() {
                    alert('删除失败');
                }
            });
        } else {
            // 管理面板逻辑
            $.ajax({
                url: '/api/messages?id=' + messageId,
                method: 'DELETE',
                success: () => {
                    const currentPage = $('#search-pagination button.active').text() || 1;
                    this.searchMessages(parseInt(currentPage));
                    this.loadMessages();
                },
                error: () => {
                    alert('删除失败');
                }
            });
        }
    }

    /**
     * 确保文档中存在一个全局独立的消息操作面板
     */
    ensureGlobalMessageMenuPanel() {
        if (document.getElementById('message-menu-panel')) {
            return;
        }
        const panel = document.createElement('div');
        panel.id = 'message-menu-panel';
        panel.className = 'message-menu-panel';
        panel.style.position = 'absolute';
        panel.style.display = 'none';
        panel.style.zIndex = '9999';
        panel.innerHTML =
            '<div class="message-menu-item message-menu-item-copy">复制</div>' +
            '<div class="message-menu-item message-menu-item-delete">删除</div>';
        document.body.appendChild(panel);
    }

    onConnected() {
        console.log('私聊WebSocket连接已建立');

        // 设置当前私聊会话
        chatManager.setPrivateChat(this.currentPrivateUserId, this.currentPrivateUsername);

        // 设置当前会话
        const sessionId = new URLSearchParams(window.location.search).get('sessionId');
        if (sessionId) {
            chatManager.setCurrentSession(sessionId);
            chatManager.clearUnreadCount(sessionId); // 清除未读计数
        }

        this.updateUserStatus();
        this.statusInterval = setInterval(this.updateUserStatus.bind(this), 30000);
    }

    updateUserStatus() {
        fetch('/api/users/online', { credentials: 'same-origin' })
            .then(function(res) { return res.json(); })
            .then(function(users) {
                var targetUser = users.find(function(u) { return u.id == this.currentPrivateUserId; }.bind(this));
                var statusElement = document.getElementById('user-status');
                if (targetUser) {
                    statusElement.textContent = targetUser.online ? '🟢 在线' : '⚪ 离线';
                    statusElement.className = 'user-status ' + (targetUser.online ? 'online' : 'offline');
                } else {
                    statusElement.textContent = '⚪ 离线';
                    statusElement.className = 'user-status offline';
                }
            }.bind(this))
            .catch(function(err) {
                console.error('Failed to load user status', err);
            });
    }

    /**
     * 处理私聊消息（通过ChatManager事件驱动）
     */
    handlePrivateMessage(message) {
        console.log('私聊页面收到消息:', message);
        console.log('messageType =>', message.messageType, 'isCode =>', message.isCode);
        // 处理私聊消息和代码消息
        if (message.messageType === 'private' || message.messageType === 'code') {
            if (message.senderId == this.currentPrivateUserId || message.receiverId == this.currentPrivateUserId) {
                // 确保 isCode 标记正确设置
                if (message.messageType === 'code') {
                    message.isCode = true;
                }
                this.appendMessage(message);
            }
        }
    }

    createMessageHTML(message) {
        const isFromMe = message.senderUsername === this.currentUsername;
        const messageClass = 'message private-message ' + (isFromMe ? 'my-private' : 'other-private');

        // 格式化时间
        const messageTime = this.formatMessageTime(message.createdAt);

        // 使用 isCode 标记判断是否为代码消息
        const isCode = message.isCode === true;

        let contentHtml;
        const raw = (message.content || '').replace(/\\n/g, '\n');
        if (isCode) {
            const escaped = this.escapeHtml(raw);
            contentHtml = '<pre class="code-message"><code class="hljs">' + escaped + '</code></pre>';
        } else {
            const textWithBr = this.escapeHtml(raw).replace(/\n/g, '<br>');
            contentHtml = '<div class="message-content">' + textWithBr + '</div>';
        }

        const messageHTML =
            '<div class="' + messageClass + '" data-message-id="' + (message.id != null ? message.id : '') + '">' +
                '<div class="message-info">' +
                    (!isFromMe ? '<div class="message-sender">' + this.currentPrivateUserDisplayName + '</div>' : '') +
                    contentHtml +
                    '<span class="message-time">' + messageTime + '</span>' +
                '</div>' +
                '<span class="message-menu-toggle" style="cursor: pointer;">···</span>' +
            '</div>';

        return messageHTML;
    }

    appendMessage(m) {
        const messagesContainer = document.getElementById('messages');
        const messageElement = document.createElement('div');

        // 创建消息HTML
        messageElement.innerHTML = this.createMessageHTML(m);

        // 添加发送动画类
        const messageDiv = messageElement.firstChild;
        messageDiv.classList.add('sending');

        // 添加到容器
        messagesContainer.appendChild(messageElement);

        const isCode = m.isCode === true;
        const hasHljs = typeof window !== 'undefined' && !!window.hljs;

        // 如果是代码消息且 highlight.js 已加载，触发一次高亮
        if (isCode && hasHljs) {
            const codeBlock = messageDiv.querySelector('pre.code-message code');
            if (codeBlock) {
                window.hljs.highlightElement(codeBlock);
            }
        }

        // 滚动到底部
        this.scrollToBottom();

        // 动画结束后移除动画类
        setTimeout(() => {
            messageDiv.classList.remove('sending');
        }, 400);
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('messages');
        messagesContainer.scrollTo({
            top: messagesContainer.scrollHeight,
            behavior: 'smooth'
        });
    }

    formatMessageTime(timeString) {
        try {
            if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(timeString)) {
                return timeString;
            }
            const date = new Date(timeString);
            return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        } catch (e) {
            return timeString;
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    loadMessages() {
        var url = '/api/messages?limit=10&private=' + this.currentPrivateUserId;

        fetch(url, { credentials: 'same-origin' })
            .then(function (res) {
                if (res.status === 401) {
                    if (window.parent && parent.loadFragment) parent.loadFragment('login.jsp');
                    return [];
                }
                return res.json();
            })
            .then(function (list) {
                if (!list) return;

                const messagesContainer = document.getElementById('messages');
                messagesContainer.innerHTML = '';

                // 按时间顺序显示消息
                list.reverse().forEach(message => {
                    this.appendMessage(message);
                });

                // 加载完成后滚动到底部
                setTimeout(() => {
                    this.scrollToBottom();
                }, 100);
            }.bind(this))
            .catch(function (err) { console.error('Failed to load messages', err); });
    }

    handleSendMessage(e) {
        e.preventDefault();
        var input = document.getElementById('msgInput');
        var text = input.value.trim();
        if (!text) return;

        // 代码消息勾选
        var isCode = document.getElementById('isCodeMessage')?.checked;

        // 根据是否为代码消息设置消息类型
        var messageType = isCode ? 'code' : 'private';

        var success = chatManager.sendMessage(text, this.currentPrivateUserId, this.currentPrivateUsername, messageType, {
            isCode: !!isCode
        });

        if (success) {
            input.value = '';
        } else {
            alert('发送失败，请检查连接状态');
        }
    }

    copyToClipboard(text) {
        if (!text) return;
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(function () {
                alert('内容已复制到剪贴板');
            }).catch(function () {
                // 回退方案
                PrivateChatManager.fallbackCopyTextToClipboard(text);
            });
        } else {
            PrivateChatManager.fallbackCopyTextToClipboard(text);
        }
    }

    static fallbackCopyTextToClipboard(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.top = '-1000px';
        textArea.style.left = '-1000px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        try {
            const successful = document.execCommand('copy');
            if (successful) {
                alert('内容已复制到剪贴板');
            } else {
                alert('复制失败，请手动复制');
            }
        } catch (err) {
            alert('复制失败，请手动复制');
        }
        document.body.removeChild(textArea);
    }

    destroy() {
        if (this.statusInterval) {
            clearInterval(this.statusInterval);
        }
        chatManager.unregisterPrivateChatListener(this.currentPrivateUserId);
    }
}


// 片段运行初始化
window.initFragment = function(){
    console.log('私聊初始化完成');
    if (window.privateChatManager) {
        window.privateChatManager.init();
    }
};

// 卸载时清理资源
window.addEventListener('beforeunload', function() {
    if (window.privateChatManager) {
        window.privateChatManager.destroy();
    }
});
