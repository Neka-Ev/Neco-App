// WebSocket管理模块

class WebSocketManager {
    constructor() {
        this.ws = null;
        this.messageHandlers = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000; // 3秒
        this.isConnecting = false;
    }

    /**
     * 连接到WebSocket服务器
     * @param {Object} options - 连接选项
     * @param {number} options.groupId - 群聊ID（可选）
     */
    connect(options = {}) {
        if (this.isConnecting || this.ws?.readyState === WebSocket.OPEN) {
            return;
        }

        this.isConnecting = true;
        
        try {
            const wsProto = location.protocol === 'https:' ? 'wss:' : 'ws:';
            let wsUrl = wsProto + '//' + location.host + window.location.pathname.replace(/\/[^\/]*$/, '') + '/ws/chat';
            
            // 如果有群聊ID，添加到URL参数中
            const params = new URLSearchParams();
            if (options.groupId) {
                params.append('groupId', options.groupId);
            }
            
            const paramsString = params.toString();
            if (paramsString) {
                wsUrl += '?' + paramsString;
            }
            
            this.ws = new WebSocket(wsUrl);
            
            this.ws.addEventListener('open', this.handleOpen.bind(this));
            this.ws.addEventListener('message', this.handleMessage.bind(this));
            this.ws.addEventListener('close', this.handleClose.bind(this));
            this.ws.addEventListener('error', this.handleError.bind(this));
            
        } catch (error) {
            console.error('WebSocket连接创建失败:', error);
            this.isConnecting = false;
            this.attemptReconnect();
        }
    }

    /**
     * 处理连接打开
     */
    handleOpen() {
        console.log('WebSocket连接已建立');
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        
        // 触发连接成功事件
        this.trigger('connected');
    }

    /**
     * 处理接收到的消息
     */
    handleMessage(event) {
        try {
            const data = JSON.parse(event.data);
            console.log('收到WebSocket消息:', data);
            
            // 根据消息类型分发处理
            this.trigger(data.type, data.payload || data);
            
        } catch (error) {
            console.error('WebSocket消息解析失败:', error, event.data);
        }
    }

    /**
     * 处理连接关闭
     */
    handleClose(event) {
        console.log('WebSocket连接已关闭:', event.code, event.reason);
        this.isConnecting = false;
        
        // 触发连接关闭事件
        this.trigger('disconnected', { code: event.code, reason: event.reason });
        
        // 如果不是正常关闭，尝试重连
        if (event.code !== 1000) {
            this.attemptReconnect();
        }
    }

    /**
     * 处理连接错误
     */
    handleError(error) {
        console.error('WebSocket连接错误:', error);
        this.isConnecting = false;
        
        // 触发错误事件
        this.trigger('error', error);
    }

    /**
     * 尝试重新连接
     */
    attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('WebSocket重连次数已达上限');
            this.trigger('reconnect_failed');
            return;
        }

        this.reconnectAttempts++;
        console.log(`尝试第${this.reconnectAttempts}次重连...`);
        
        setTimeout(() => {
            this.connect();
        }, this.reconnectInterval * this.reconnectAttempts);
    }

    /**
     * 发送消息
     */
    send(message) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.error('WebSocket未连接，无法发送消息');
            return false;
        }

        try {
            const messageString = typeof message === 'string' ? message : JSON.stringify(message);
            this.ws.send(messageString);
            return true;
        } catch (error) {
            console.error('WebSocket消息发送失败:', error);
            return false;
        }
    }

    /**
     * 注册消息处理器
     */
    on(messageType, handler) {
        if (!this.messageHandlers.has(messageType)) {
            this.messageHandlers.set(messageType, []);
        }
        this.messageHandlers.get(messageType).push(handler);
    }

    /**
     * 移除消息处理器
     */
    off(messageType, handler) {
        const handlers = this.messageHandlers.get(messageType);
        if (handlers) {
            const index = handlers.indexOf(handler);
            if (index > -1) {
                handlers.splice(index, 1);
            }
        }
    }

    /**
     * 触发消息处理
     */
    trigger(messageType, data) {
        const handlers = this.messageHandlers.get(messageType);
        if (handlers) {
            handlers.forEach(handler => {
                try {
                    handler(data);
                } catch (error) {
                    console.error(`消息处理器错误 (${messageType}):`, error);
                }
            });
        }
    }

    /**
     * 断开连接
     */
    disconnect() {
        if (this.ws) {
            this.ws.close(1000, '正常关闭');
            this.ws = null;
        }
        this.isConnecting = false;
    }

    /**
     * 获取连接状态
     */
    getState() {
        return this.ws ? this.ws.readyState : WebSocket.CONNECTING;
    }

    /**
     * 检查是否已连接
     */
    isConnected() {
        return this.ws?.readyState === WebSocket.OPEN;
    }


}

// 创建全局WebSocket管理器实例
const wsManager = new WebSocketManager();

// 导出供其他模块使用
window.WebSocketManager = WebSocketManager;
window.wsManager = wsManager;











