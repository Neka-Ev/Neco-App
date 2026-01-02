class GroupChatManager {
    constructor(groupId, groupName, currentUserId, currentUsername) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.currentUserId = currentUserId;
        this.currentUsername = currentUsername;
        this.myRole = 3; // 默认成员，稍后获取
        this.pageSize = 10;
        this.$deleteBtn = $('#delete-group-btn');
        this.needsChatRefresh = false;
    }

    // 初始化
    init() {
        this.fetchMyRole(() => {
            this.loadHistoryMessages();
        });
        this.bindEvents();
        this.initModal();
        this.setupWebSocket();
        this.setupEventListeners();
    }

    setupWebSocket() {
        if (window.wsManager) {
            window.wsManager.disconnect();
            window.wsManager.connect({ groupId: this.groupId });

            window.wsManager.on('message', (message) => {
                if (this.currentUserId === message.senderId) return; // 忽略自己发送的消息
                if (!message || message.messageType !== 'group') return;
                const incomingGroupId = message.groupId != null ? parseInt(message.groupId, 10) : null;
                if (incomingGroupId !== null && incomingGroupId === parseInt(this.groupId, 10)) {

                    // 标准化字段
                    message.messageId = message.messageId || message.id;
                    message.timestamp = message.timestamp || message.createdAt || message.sentAt;
                    this.addMessage(message);
                    this.scrollToBottom();
                }
            });
        }
    }

    fetchMyRole(callback) {
        $.get('/api/groups/role', { groupId: this.groupId }, (data) => {
            this.myRole = data.role;
            if (this.myRole === 1 || this.myRole === 2) {
                $('#btn-delete-batch').show();
            }
            if (this.myRole === 1) {
                this.$deleteBtn.show();
            }
            if (callback) callback();
        }).fail((xhr) => {
            this.handleGroupMissing(xhr);
        });
    }

    // 加载历史消息
    loadHistoryMessages() {
        const $container = $('#messages');
        $container.html('<div class="loading-indicator">加载历史消息中...</div>');

        $.get('/api/groups/messages', { groupId: this.groupId }, (messages) => {
            this.renderMessages(messages);
            this.scrollToBottom();
        }).fail((err) => {
            const handled = this.handleGroupMissing(err);
            if (!handled) {
                $container.html('<div class="error-state">加载历史消息失败</div>');
            }
        });
    }

    handleGroupMissing(xhr) {
        const status = xhr && xhr.status;
        const message = (xhr && xhr.responseJSON && (xhr.responseJSON.message || xhr.responseJSON.error)) || '';
        console.log(message);
        console.log(status);
        const notMember = status === 403 || (message && message.indexOf('您不是该群成员') !== -1);
        const isMissing = status === 404 || status === 410 || (message && message.indexOf('不存在') !== -1);
        if (notMember || isMissing) {
            const tip = notMember ? '你不是该群聊成员，即将返回认识的人列表' : '该群聊已被解散，即将返回认识的人列表';
            alert(tip);
            window.location.href = '?page=friends';
            return true;
        }
        return false;
    }

    // 渲染消息
    renderMessages(messages) {
        const $container = $('#messages');
        $container.empty();
        messages.forEach(message => this.addMessage(message));
    }

    // 添加消息
    addMessage(message) {
        const $container = $('#messages');
        const isMe = message.senderId === this.currentUserId;
        const msgClass = isMe ? 'my-private' : 'other-private';

        // 格式化时间
        const time = new Date(message.timestamp || message.sentAt || message.createdAt);
        const formattedTime = time.toLocaleTimeString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });

        // 处理代码消息，先将数据库中的 \n 转为实际换行
        const rawContent = (message.content || '').replace(/\\n/g, '\n');
        let contentHtml;
        if (message.isCode) {
            contentHtml = '<pre class="code-message"><code class="hljs">' + this.escapeHtml(rawContent) + '</code></pre>';
        } else {
            contentHtml = '<div class="message-content">' + this.escapeHtml(rawContent).replace(/\n/g, '<br>') + '</div>';
        }

        const senderName = message.senderUsername || message.sender || 'Unknown';

        const $msgDiv = $(
            '<div class="message private-message ' + msgClass + '" data-message-id="' + (message.messageId || message.id || '') + '">' +
                '<div class="message-info">' +
                    (!isMe ? '<div class="message-sender">' + senderName + '</div>' : '') +
                    contentHtml +
                    '<span class="message-time">' + formattedTime + '</span>' +
                '</div>' +
                '<span class="message-menu-toggle" style="cursor: pointer;">···</span>' +
            '</div>'
        );

        $container.append($msgDiv);
        const isCode = message.isCode === true;
        const hasHljs = typeof window !== 'undefined' && !!window.hljs;

        // 高亮代码
        if (isCode && hasHljs) {
            const codeBlock = $msgDiv.find('code')[0];
            if (codeBlock) {
                hljs.highlightElement(codeBlock);
            }
        }
    }

    deleteSingleMessage(msgId) {
        if (!confirm('确定删除这条消息吗？')) return;
        this.deleteMessages([msgId]);
    }

    deleteMessages(msgIds) {
        $.ajax({
            url: '/api/groups/messages/delete',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                groupId: this.groupId,
                messageIds: msgIds
            }),
            success: (data) => {
                if (data.success) {
                    msgIds.forEach(id => {
                        // 删除管理面板列表
                        $('.message-item[data-msg-id="' + id + '"]').remove();
                        // 同步删除聊天区气泡（即刻反馈）
                        $('#messages .message[data-message-id="' + id + '"]').remove();
                    });
                    this.needsChatRefresh = true;
                    if ($('#group-manage-modal').is(':visible')) {
                        this.searchMessages();
                    }
                } else {
                    alert(data.error || '删除失败');
                }
            },
            error: () => alert('删除失败')
        });
    }

    // 发送消息
    sendMessage(content, isCode) {
        const message = {
            type: 'message',
            chatType: 'group',
            targetId: this.groupId,
            content: content,
            messageType: isCode ? 'code' : 'text',
            isCode: isCode || false
        };

        // 立即显示消息 (Optimistic UI)
        const tempMessage = {
            senderId: this.currentUserId,
            senderUsername: this.currentUsername,
            content: content,
            timestamp: new Date().toISOString(),
            isCode: isCode || false
        };
        this.addMessage(tempMessage);
        this.scrollToBottom();

        if (window.wsManager && window.wsManager.isConnected()) {
            window.wsManager.send(JSON.stringify(message));
        } else {
            console.error('WebSocket未连接');
        }
    }

    scrollToBottom() {
        const container = document.getElementById('messages');
        if(container) container.scrollTop = container.scrollHeight;
    }

    bindEvents() {
        // 删除群聊（仅群主）
        this.$deleteBtn.on('click', () => {
            if (!confirm('删除群聊后将无法恢复，确定删除吗？')) return;
            $.ajax({
                url: '/api/groups/delete',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ groupId: this.groupId }),
                success: (res) => {
                    alert(res.message || '群聊已删除');
                    window.location.href = '?page=friends';
                },
                error: (xhr) => {
                    const msg = xhr.responseJSON && xhr.responseJSON.error ? xhr.responseJSON.error : '删除失败';
                    alert(msg);
                }
            });
        });

       // $('#group-manage-btn').on('click', () => this.initModal());

        $('#sendForm').on('submit', (e) => {
            e.preventDefault();
            this.handleSendMessage();
        });

        $('#msgInput').on('keydown', (e) => {
            if (e.ctrlKey && e.key === 'Enter') {
                e.preventDefault();
                this.handleSendMessage();
            }
        });

        $(document).on('click', () => {
            $('.action-menu.show').removeClass('show');
        });
    }

    handleSendMessage() {
        const $input = $('#msgInput');
        const $isCode = $('#isCodeMessage');
        const isCode = $isCode.is(':checked');
        const content = $input.val().trim();

        if (!content) return;

        this.sendMessage(content, isCode);

        $input.val('').attr('rows', 2);
        $isCode.prop('checked', false);
    }

    // Modal Logic
    initModal() {
        const $modal = $('#group-manage-modal');
        const hideModal = () => {
            $modal.hide();
            if (this.needsChatRefresh) {
                this.loadHistoryMessages();
                this.needsChatRefresh = false;
            }
        };

        $('#group-manage-btn').on('click', () => {
            $modal.show();
            this.loadMembersForSelect();
            this.searchMembers();
            this.searchMessages();
        });

        $('.close-modal').off('click').on('click', hideModal);

        $(window).off('click.groupModal').on('click.groupModal', (e) => {
            if ($(e.target).is($modal)) hideModal();
        });

        const self = this;
        $('.tab-btn').off('click').on('click', function() {
            const tabId = $(this).data('tab');
            $('.tab-btn').removeClass('active');
            $('.tab-content').removeClass('active');
            $(this).addClass('active');
            $('#' + tabId).addClass('active');

            if (tabId === 'member-manage') {
                self.searchMembers();
            }
        });

        $('#btn-search-msg').on('click', () => this.searchMessages());

        $('#btn-delete-batch').on('click', () => {
            const ids = [];
            $('.msg-checkbox:checked').each(function() {
                ids.push($(this).val());
            });

            if (ids.length === 0) return alert('请选择要删除的消息');
            if (!confirm('确定删除选中的 ' + ids.length + ' 条消息吗？')) return;

            this.deleteMessages(ids.map(Number));
        });

        $('#check-all-msg').on('change', function() {
            $('.msg-checkbox').prop('checked', this.checked);
            self.updateBatchDeleteBtn();
        });

        $('#msg-table-body').on('change', '.msg-checkbox', () => {
            const all = $('.msg-checkbox').length;
            const checked = $('.msg-checkbox:checked').length;
            $('#check-all-msg').prop('checked', all === checked);
            this.updateBatchDeleteBtn();
        });

        $('#btn-search-member').on('click', () => this.searchMembers());
    }

    loadMembersForSelect() {
        const $select = $('#search-sender');
        $select.html('<option value="">所有发送者</option>');

        $.get('/api/groups/members', { groupId: this.groupId }, (data) => {
            data.forEach(member => {
                const name = member.username + (member.displayName ? ' (' + member.displayName + ')' : '');
                $select.append('<option value="' + member.userId + '">' + name + '</option>');
            });
        });
    }

    updateBatchDeleteBtn() {
        const count = $('.msg-checkbox:checked').length;
        const $btn = $('#btn-delete-batch');
        if (count > 0 && (this.myRole === 1 || this.myRole === 2)) {
            $btn.show().text('批量删除 (' + count + ')');
        } else {
            $btn.hide();
        }
    }

    searchMessages(page = 1) {
        const params = {
            groupId: this.groupId,
            page: page,
            pageSize: this.pageSize,
            content: $('#search-content').val(),
            senderId: $('#search-sender').val(),
            startDate: $('#search-start-date').val(),
            endDate: $('#search-end-date').val(),
            isCode: $('#search-is-code').val()
        };

        $.get('/api/groups/messages/search', params, (data) => {
            const $tbody = $('#msg-table-body');
            $tbody.empty();
            $('#check-all-msg').prop('checked', false);
            this.updateBatchDeleteBtn();

            if (!data.messages || data.messages.length === 0) {
                $tbody.html('<tr><td colspan="5" style="text-align:center;">无记录</td></tr>');
                $('#msg-pagination').empty();
                return;
            }

            data.messages.forEach(msg => {
                const time = new Date(msg.sentAt || msg.timestamp).toLocaleString();
                const canDelete = this.myRole === 1 || this.myRole === 2;
                const deleteBtn = canDelete ?
                    '<button class="action-btn delete table-delete-btn" data-id="' + msg.messageId + '">删除</button>' : '';
                const encodedContent = encodeURIComponent(msg.content);
                const contentDisplay = msg.content.length > 50 ? msg.content.substring(0, 50) + '...' : msg.content;
                const isCodeLabel = msg.isCode ? '<span style="color:#58a6ff">[代码]</span> ' : '';

                const tr =
                    '<tr>' +
                        '<td><input type="checkbox" class="msg-checkbox" value="' + msg.messageId + '"></td>' +
                        '<td>' + (msg.senderUsername || msg.senderId) + '</td>' +
                        '<td>' + time + '</td>' +
                        '<td title="' + (msg.isCode ? '[代码消息]' : '') + '">' + isCodeLabel + this.escapeHtml(contentDisplay) + '</td>' +
                        '<td>' +
                            '<button class="action-btn table-copy-btn" data-content="' + encodedContent + '">复制</button>' +
                            deleteBtn +
                        '</td>' +
                    '</tr>';
                $tbody.append(tr);
            });

            const self = this;
            $('.table-delete-btn').on('click', function() {
                self.deleteSingleMessage($(this).data('id'));
            });
            $('.table-copy-btn').on('click', function() {
                self.copyToClipboard($(this).data('content'));
            });

            this.renderPagination(data.page, Math.ceil(data.total / data.pageSize));
        });
    }

    renderPagination(current, totalPages) {
        const $container = $('#msg-pagination');
        $container.empty();

        if (totalPages <= 1) return;

        const $prev = $('<button>上一页</button>')
            .prop('disabled', current <= 1)
            .on('click', () => this.searchMessages(current - 1));

        const $next = $('<button>下一页</button>')
            .prop('disabled', current >= totalPages)
            .on('click', () => this.searchMessages(current + 1));

        $container.append($prev)
                  .append('<span> 第 ' + current + ' / ' + totalPages + ' 页 </span>')
                      .append($next);
    }

    escapeHtml(text) {
        if (!text) return '';
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    copyToClipboard(encodedContent) {
        const text = decodeURIComponent(encodedContent);
        navigator.clipboard.writeText(text).then(() => {
            alert('已复制到剪贴板');
        }).catch(err => {
            console.error('复制失败', err);
            alert('复制失败');
        });
    }

    searchMembers() {
        const query = $('#search-member-input').val();
        $.get('/api/groups/members', { groupId: this.groupId, query: query }, (data) => {
            const $tbody = $('#member-table-body');
            $tbody.empty();

            data.forEach(member => {
                const roleName = member.role === 1 ? '群主' : (member.role === 2 ? '管理员' : '成员');
                const canKick = (this.myRole === 1 && member.userId !== this.currentUserId) ||
                              (this.myRole === 2 && member.role === 3);
                const kickBtn = canKick ?
                    '<button class="danger-btn small-btn kick-btn" data-id="' + member.userId + '">踢出</button>' : '';

                const tr =
                    '<tr>' +
                        '<td><img src="' + (member.avatarUrl || 'assets/images/avatars/default.jpg') + '" class="avatar-small"></td>' +
                        '<td>' + member.username + '</td>' +
                        '<td>' + (member.displayName || '-') + '</td>' +
                        '<td>' + roleName + '</td>' +
                        '<td>' + kickBtn + '</td>' +
                    '</tr>';
                $tbody.append(tr);
            });

            const self = this;
            $('.kick-btn').on('click', function() {
                self.kickMember($(this).data('id'));
            });
        });
    }

    openInviteDialog() {
        const self = this;
        $('#invite-modal').show();
        const $list = $('#invite-friends-list');
        $list.html('<div class="loading-indicator">加载好友...</div>');

        // 先获取现有成员，避免重复邀请
        $.when(
            $.get('/api/groups/members', { groupId: this.groupId }),
            $.get('/friends/list')
        ).done((membersRes, friendsRes) => {
            const members = membersRes[0] || [];
            const friends = (friendsRes[0] && friendsRes[0].friends) ? friendsRes[0].friends : [];
            const memberIds = new Set(members.map(m => m.userId));
            const candidates = friends.filter(f => !memberIds.has(f.id));

            if (!candidates.length) {
                $list.html('<div class="empty-state">暂无可邀请成员</div>');
                return;
            }

            const html = candidates.map(f => {
                const avatar = f.avatarUrl || 'assets/images/avatars/default.jpg';
                const displayName = f.displayName ? ' (' + f.displayName + ')' : '';
                return '' +
                    '<div class="invite-item" data-id="' + f.id + '" data-name="' + f.username + '">' +
                    '<img class="avatar-small" src="' + avatar + '" onerror="this.src=\'assets/images/avatars/default.jpg\'" />' +
                    '<span class="invite-name">' + f.username + displayName + '</span>' +
                    '<button class="invite-btn">邀请</button>' +
                    '</div>';
            }).join('');
            $list.html(html);
            $list.find('.invite-btn').on('click', function() {
                const $item = $(this).closest('.invite-item');
                const uid = $item.data('id');
                self.inviteMember(uid);
            });
        }).fail(() => {
            $list.html('<div class="error-state">加载好友失败</div>');
        });

        $('#invite-close').off('click').on('click', () => $('#invite-modal').hide());
    }

    inviteMember(userId) {
        $.ajax({
            url: '/api/groups/member',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ groupId: this.groupId, userId: userId, action: 'invite' })
        }).done(() => {
            alert('已邀请加入群聊');
            $('#invite-modal').hide();
            this.searchMembers();
        }).fail((xhr) => {
            const msg = xhr.responseJSON && xhr.responseJSON.error ? xhr.responseJSON.error : '邀请失败';
            alert(msg);
        });
    }

    kickMember(userId) {
        if (!confirm('确定要将该成员踢出群聊吗？')) return;
        $.ajax({
            url: '/api/groups/member',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ groupId: this.groupId, userId: userId, action: 'kick' })
        }).done(() => {
            alert('已踢出该成员');
            this.needsChatRefresh = true;
            this.searchMembers();
        }).fail((xhr) => {
            const msg = xhr.responseJSON && (xhr.responseJSON.error || xhr.responseJSON.message) ? (xhr.responseJSON.error || xhr.responseJSON.message) : '操作失败';
            alert(msg);
        });
    }

    destroy() {
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

            // 将面板移动到按钮旁边
            panel.style.left = (rect.right + scrollX + 8) + 'px';
            panel.style.top = (rect.top + scrollY - 4) + 'px';

            // 根据权限显示/隐藏删除按钮
            const deleteBtn = panel.querySelector('.message-menu-item-delete');
            if (deleteBtn) {
                if (window.groupChatManager.myRole === 1 || window.groupChatManager.myRole === 2) {
                    deleteBtn.style.display = 'block';
                } else {
                    deleteBtn.style.display = 'none';
                }
            }

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
            // 获取消息内容，优先获取 data-content，否则从 DOM 获取
            // 需要处理代码消息和普通消息
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
                window.groupChatManager.copyToClipboard(text);
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

            window.groupChatManager.deleteSingleMessage(messageId);
            panel.style.display = 'none';
        });

        // 点击页面其他地方关闭面板
        $(document).on('click', function () {
            const panel = document.getElementById('message-menu-panel');
            if (panel) {
                panel.style.display = 'none';
            }
        });
    }

    ensureGlobalMessageMenuPanel() {
        if (document.getElementById('message-menu-panel')) {
            return;
        }
        const panel = document.createElement('div');
        panel.id = 'message-menu-panel';
        panel.className = 'message-menu-panel';
        panel.style.display = 'none';

        panel.innerHTML =
            '<div class="message-menu-item message-menu-item-copy">复制</div>' +
            '<div class="message-menu-item message-menu-item-delete">删除</div>';
        document.body.appendChild(panel);
    }
}

// 片段运行初始化
window.initFragment = function() {
    if (window.groupChatManager) {
        window.groupChatManager.init();
    }
};

// 卸载时清理资源
window.addEventListener('beforeunload', function() {
    if (window.groupChatManager) {
        window.groupChatManager.destroy();
    }
});
