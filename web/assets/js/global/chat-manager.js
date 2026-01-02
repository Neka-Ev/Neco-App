// 聊天功能模块

class ChatManager {
    constructor() {
        this.currentUserId = null;
        this.currentUsername = null;
        this.chatSessions = [];
        this.currentSessionId = null;
        this.messageHandlerRegistered = false;
        this.processedMessages = new Set();
        this.privateChatListeners = new Map(); // 新增：私聊监听器
    }

    /**
     * 初始化聊天管理器
     */
    init(userId, username) {
        this.currentUserId = userId;
        this.currentUsername = username;


        // 只注册一次消息处理器
        if (!this.messageHandlerRegistered) {
            wsManager.on('message', this.handleNewMessage.bind(this));
            wsManager.on('session_update', this.handleSessionUpdate.bind(this));
            this.messageHandlerRegistered = true;
        }

        this.loadChatSessions();
    }

    /**
     * 注册私聊消息监听器
     */
    registerPrivateChatListener(privateUserId, listener) {
        this.privateChatListeners.set(privateUserId, listener);
    }

    /**
     * 移除私聊消息监听器
     */
    unregisterPrivateChatListener(privateUserId) {
        this.privateChatListeners.delete(privateUserId);
    }


    /**
     * 连接建立后的处理
     */
    onConnected() {
        console.log('聊天管理器已连接');
        // 重新加载会话列表
        this.loadChatSessions();
    }

    /**
     * 连接断开后的处理
     */
    onDisconnected() {
        console.log('聊天管理器已断开连接');
    }

    setCurrentSession(sessionId) {
        this.currentSessionId = sessionId;
    }

    /**
     * 加载聊天会话列表
     */
    async loadChatSessions() {
        try {
            const response = await fetch('/api/sessions');
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const sessions = await response.json();
            this.chatSessions = sessions;
            this.renderChatSessions();
            
            if (sessions.length > 0) {
                const container = document.getElementById('chat-sessions-container');
                if (container) {
                    container.style.display = 'block';
                }
            }
            
        } catch (error) {
            console.error('加载会话失败:', error);
        }
    }

    /**
     * 渲染聊天会话列表
     */
    renderChatSessions() {
        const container = document.getElementById('chat-sessions-list');
        if (!container) return;

        container.innerHTML = '';

        this.chatSessions.forEach(session => {
            const otherUser = this.getOtherUser(session);
            const unreadCount = this.getUnreadCount(session);

            const sessionElement = document.createElement('a');
            sessionElement.className = 'nav-item session-item';
            sessionElement.href = '#';
            sessionElement.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>${otherUser.username || '未知用户'}</span>
                    ${unreadCount > 0 ? `<span class="badge" style="background: #e74c3c; color: white; border-radius: 10px; padding: 2px 6px; font-size: 12px;">${unreadCount}</span>` : ''}
                </div>
            `;
            
            sessionElement.onclick = (e) => {
                e.preventDefault();
                this.openPrivateChat(otherUser.id, otherUser.username, session.id);
            };

            container.appendChild(sessionElement);
        });
    }

    /**
     * 获取会话中的对方用户信息
     */
    getOtherUser(session) {
        return session.user1Id === this.currentUserId ? 
            { id: session.user2Id, username: session.user2Username } : 
            { id: session.user1Id, username: session.user1Username };
    }

    /**
     * 获取当前用户的未读消息数
     */
    getUnreadCount(session) {
        return session.user1Id === this.currentUserId ? 
            session.unreadCountUser1 : session.unreadCountUser2;
    }

    /**
     * 打开私聊界面
     */
    openPrivateChat(userId, username, sessionId) {
        this.currentSessionId = sessionId;

        // 清除该会话的未读计数
        if (sessionId) {
            this.clearUnreadCount(sessionId);
        }
        
        const url = new URL(window.location.href);
        url.searchParams.set('page', 'private-chat');
        url.searchParams.set('userId', userId);
        url.searchParams.set('username', username);
        url.searchParams.set('sessionId', sessionId);

        window.location.href = url.toString();
    }

    /**
     * 创建或更新会话
     */
    async createOrUpdateSession(otherUserId, otherUsername, messageId) {
        try {
            const response = await fetch('/api/sessions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    otherUserId: otherUserId,
                    otherUsername: otherUsername,
                    messageId: messageId
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const session = await response.json();
            // 更新会话列表
            await this.loadChatSessions();
            
            return session;
            
        } catch (error) {
            console.error('创建会话失败:', error);
            throw error;
        }
    }

    /**
     * 清除会话的未读消息计数
     */
    async clearUnreadCount(sessionId) {
        try {
            const response = await fetch('/api/clear-unread', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: 'sessionId=' + sessionId
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            // 更新本地会话列表
            const session = this.chatSessions.find(s => s.id === sessionId);
            if (session) {
                if (session.user1Id === this.currentUserId) {
                    session.unreadCountUser1 = 0;
                } else {
                    session.unreadCountUser2 = 0;
                }
                this.renderChatSessions();
            }

            console.log('已清除会话未读计数:', sessionId);

        } catch (error) {
            console.error('清除未读计数失败:', error);
        }
    }

    /**
     * 处理新消息
     */
    handleNewMessage(message) {
        // 消息去重检查
        if (this.processedMessages.has(message.id)) {
            return;
        }
        this.processedMessages.add(message.id);

        // 处理私聊消息和代码消息
        if ((message.messageType === 'private' || message.messageType === 'code') && message.receiverId === this.currentUserId) {
            // 确保 isCode 标记正确设置
            if (message.messageType === 'code') {
                message.isCode = true;
            }

            // 检查是否在当前私聊界面中
            const isInCurrentPrivateChat = this.isInPrivateChatWith(message.senderId);

            // 只有在不在当前私聊界面时才更新未读计数
            if (!isInCurrentPrivateChat) {
                // 更新会话状态
                this.updateSessionForMessage(message);
                console.log('不在私聊界面，更新会话状态:', message);
            }

            // 通知相关的私聊监听器
            this.notifyPrivateChatListeners(message);

            // 如果当前不在私聊界面，显示通知
            if (!isInCurrentPrivateChat) {
                showNotification(`新消息来自 ${message.senderUsername}`, message.content);
            }
        }
    }

    /**
     * 更新会话状态
     */
    updateSessionForMessage(message) {
        const session = this.findSession(message);

        if (session) {
            // 更新未读计数
            if (session.user1Id === this.currentUserId) {
                session.unreadCountUser1++;
            } else {
                session.unreadCountUser2++;
            }
            this.renderChatSessions();
        } else {
            // 创建新会话
            this.createOrUpdateSession(message.senderId, message.senderUsername, message.id);
        }
    }

    /**
     * 通知私聊监听器
     */
    notifyPrivateChatListeners(message) {
        // 通知发送方相关的监听器（如果是自己发送的消息）
        if (message.senderId === this.currentUserId) {
            const senderListener = this.privateChatListeners.get(message.receiverId);
            if (senderListener && typeof senderListener === 'function') {
                senderListener(message);
            }
        }

        // 通知接收方相关的监听器
        const receiverListener = this.privateChatListeners.get(message.senderId);
        if (receiverListener && typeof receiverListener === 'function') {
            receiverListener(message);
        }
    }

    /**
     * 检查是否在指定的私聊中
     */
    isInPrivateChatWith(userId) {
        return this.currentSessionId &&
            this.chatSessions.some(session =>
                (session.user1Id === this.currentUserId && session.user2Id === userId) ||
                (session.user2Id === this.currentUserId && session.user1Id === userId)
            );
    }

    /**
     * 根据消息查找对应的会话
     */
    findSession(message) {
        return this.chatSessions.find(session =>
            (session.user1Id === message.senderId && session.user2Id === message.receiverId) ||
            (session.user2Id === message.senderId && session.user1Id === message.receiverId)
        );
    }


    /**
     * 处理会话更新
     */
    handleSessionUpdate(sessionData) {
        // 检查是否在当前私聊界面中
        const otherUserId = sessionData.user1Id === this.currentUserId ? sessionData.user2Id : sessionData.user1Id;
        const isInCurrentPrivateChat = this.isInPrivateChatWith(otherUserId);

        // 只有在不在当前私聊界面时才更新未读计数
        if (!isInCurrentPrivateChat) {
            // 查找现有会话
            const existingSessionIndex = this.chatSessions.findIndex(s => s.id === sessionData.id);

            if (existingSessionIndex !== -1) {
                // 更新现有会话
                this.chatSessions[existingSessionIndex] = {
                    ...this.chatSessions[existingSessionIndex],
                    lastMessageId: sessionData.lastMessageId,
                    unreadCountUser1: sessionData.user1Id === this.currentUserId ?
                        sessionData.unreadCount : this.chatSessions[existingSessionIndex].unreadCountUser1,
                    unreadCountUser2: sessionData.user2Id === this.currentUserId ?
                        sessionData.unreadCount : this.chatSessions[existingSessionIndex].unreadCountUser2
                };
            } else {
                // 添加新会话
                this.chatSessions.push({
                    id: sessionData.id,
                    user1Id: sessionData.user1Id,
                    user2Id: sessionData.user2Id,
                    user1Username: sessionData.user1Username,
                    user2Username: sessionData.user2Username,
                    lastMessageId: sessionData.lastMessageId,
                    unreadCountUser1: sessionData.user1Id === this.currentUserId ?
                        sessionData.unreadCount : 0,
                    unreadCountUser2: sessionData.user2Id === this.currentUserId ?
                        sessionData.unreadCount : 0,
                    createdAt: sessionData.createdAt,
                    updatedAt: sessionData.updatedAt
                });
            }

            this.renderChatSessions();
        } else {
            console.log('在当前私聊界面中，跳过服务器会话更新:', sessionData);
        }
    }

    /**
     * 发送消息
     * @param {string} content 内容
     * @param {number|string} receiverId 接收者ID
     * @param {string} receiverUsername 接收者用户名
     * @param {string} messageType 消息类型
     * @param {object} options 额外选项，例如 { isCode: true }
     */
    sendMessage(content, receiverId, receiverUsername, messageType, options = {}) {
        if (!wsManager.isConnected()) {
            console.error('WebSocket未连接，无法发送消息');
            return false;
        }

        const isCode = !!options.isCode;

        // 创建消息数据用于立即显示
        const messageData = {
            id: Date.now().toString(),
            content: content,
            messageType: isCode ? 'code' : 'private',
            isCode: isCode,
            senderId: this.currentUserId,
            senderUsername: this.currentUsername,
            receiverId: parseInt(receiverId),
            receiverUsername: receiverUsername,
            createdAt: new Date().toLocaleString()
        };

        // 立即通知私聊监听器显示消息
        this.notifyPrivateChatListeners(messageData);

        const payload = {
            type: 'message',
            content: content,
            messageType: isCode ? 'code' : 'private',
            receiverId: parseInt(receiverId),
            receiverUsername: receiverUsername
        };

        console.log('发送消息给用户ID:', receiverId, 'messageType:', payload.messageType, 'isCode:', isCode);
        return wsManager.send(payload);
    }


    /**
     * 设置私聊会话
     */
    setPrivateChat(receiverId, receiverUsername) {
        // 检查不能与自己私聊
        if (parseInt(receiverId) === this.currentUserId) {
            console.error('不能与自己进行私聊');
            return false;
        }

        if (!wsManager.isConnected()) {
            console.error('WebSocket未连接，无法设置私聊会话');
            return false;
        }

        const payload = {
            type: 'set_private_chat',
            content:'set_private_chat',
            messageType: 'set_private_chat',
            receiverId: parseInt(receiverId),
            receiverUsername: receiverUsername
        };

        return wsManager.send(payload);
    }
}

// 创建全局聊天管理器实例
const chatManager = new ChatManager();

// 导出供其他模块使用
window.ChatManager = ChatManager;
window.chatManager = chatManager;