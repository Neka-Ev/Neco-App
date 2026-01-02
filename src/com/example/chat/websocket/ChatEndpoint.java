package com.example.chat.websocket;

import com.example.chat.dao.ChatSessionDao;
import com.example.chat.dao.GroupMemberDao;
import com.example.chat.dao.GroupMessageDao;
import com.example.chat.dao.MessageDao;
import com.example.chat.model.GroupMessage;
import com.example.chat.model.Message;
import com.example.chat.model.User;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ServerEndpoint(value = "/ws/chat", configurator = HttpSessionConfigurator.class)
public class ChatEndpoint {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private MessageDao messageDao = new MessageDao();
    private ChatSessionDao chatSessionDao = new ChatSessionDao();
    private GroupMessageDao groupMessageDao = new GroupMessageDao();
    private GroupMemberDao groupMemberDao = new GroupMemberDao();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            session.close();
            return;
        }
        session.getUserProperties().put(HttpSession.class.getName(), httpSession);
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
            if (httpSession == null) return;

            User currentUser = (User) httpSession.getAttribute("user");
            if (currentUser == null) return;

            // 记录接收到的消息
            System.out.println("收到消息: " + message);

            // 解析JSON消息
            MessageData messageData = parseMessage(message);
            if (messageData == null || messageData.content == null || messageData.content.trim().isEmpty()) return;

            // 调试一下收到的消息进行的json解析结果
            System.out.println("解析消息 - 类型: " + messageData.type + ", 内容: " + messageData.content +
                    ", 消息类型: " + messageData.messageType + ", 聊天类型: " + messageData.chatType + ", 目标ID: " + messageData.targetId);

            // 处理不同类型的消息
            if ("set_private_chat".equals(messageData.type)) {
                // Set private chat mode for this session
                session.getUserProperties().put("privateChat", messageData);
                System.out.println(session.getUserProperties().get("privateChat"));

            } else if ("message".equals(messageData.type)) {
                // 确定聊天类型（默认为私聊）
                String chatType = messageData.chatType != null ? messageData.chatType : "private";
                
                if ("private".equals(chatType)) {
                    // 私聊消息处理
                    handlePrivateMessage(session, messageData, currentUser);
                } else if ("group".equals(chatType)) {
                    // 群聊消息处理
                    handleGroupMessage(session, messageData, currentUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateMessage(Session session, MessageData messageData, User currentUser) {
        try {
            // 处理消息发送
            Message m = new Message();
            m.setSenderId(currentUser.getId());
            m.setSenderUsername(currentUser.getUsername());
            m.setContent(messageData.content);

            // 是否为代码消息：前端通过 messageType='code' 表示
            boolean codeFlag = "code".equals(messageData.messageType);
            m.setCode(codeFlag);

            // 设置为私聊
            m.setMessageType("private");
            
            Integer receiverId = messageData.targetId != null ? messageData.targetId : messageData.receiverId;
            if (receiverId != null) {
                // 有指定接收者
                m.setReceiverId(receiverId);
                m.setReceiverUsername(messageData.receiverUsername);
                System.out.println((codeFlag ? "代码" : "普通") + "私聊消息发送给用户ID: " + receiverId);

                try {
                    boolean isBlockedByTarget = com.example.chat.servlet.FriendServlet.isBlocked(receiverId, currentUser.getId());
                    boolean isBlocked = com.example.chat.servlet.FriendServlet.isBlocked(currentUser.getId(), receiverId);
                    
                    if (isBlocked || isBlockedByTarget) {
                        System.out.println("消息发送失败：存在拉黑关系");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("好友关系检查失败，允许消息继续发送");
                }
            } else {
                System.out.println("无效消息：私聊消息必须指定接收者");
                return;
            }

            long id = messageDao.insert(m);

            // 处理私聊消息的聊天会话
            if ("private".equals(m.getMessageType()) && m.getReceiverId() != null) {
                try {
                    com.example.chat.model.ChatSession chatSession = chatSessionDao.findOrCreateSession(
                            currentUser.getId(), m.getReceiverId(), currentUser.getUsername(), m.getReceiverUsername());

                    chatSessionDao.updateLastMessage(chatSession.getId(), id);

                    boolean isReceiverInPrivateChat = isUserInPrivateChatWith(m.getReceiverId(), currentUser.getId());
                    System.out.println("接收方" + m.getReceiverId() + "是否在私聊界面中: " + isReceiverInPrivateChat);

                    // 只有在接收方不在私聊界面时才增加未读计数
                    if (currentUser.getId() == chatSession.getUser1Id()) {
                        if (!isReceiverInPrivateChat) {
                            chatSessionDao.incrementUnreadCount(chatSession.getId(), chatSession.getUser2Id());
                            System.out.println("增加用户" + chatSession.getUser2Id() + "的未读计数（接收方不在私聊界面）");
                        } else {
                            System.out.println("跳过增加未读计数（接收方在私聊界面）");
                        }
                    } else {
                        if (!isReceiverInPrivateChat) {
                            chatSessionDao.incrementUnreadCount(chatSession.getId(), chatSession.getUser1Id());
                            System.out.println("增加用户" + chatSession.getUser1Id() + "的未读计数（接收方不在私聊界面）");
                        } else {
                            System.out.println("跳过增加未读计数（接收方在私聊界面）");
                        }
                    }
                    // 向两个用户发送会话更新
                    sendSessionUpdate(chatSession, currentUser.getId());
                    sendSessionUpdate(chatSession, m.getReceiverId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String payload = createMessagePayload(m);
            synchronized (sessions) {
                for (Session s : sessions) {
                    if (shouldSendToSession(s, m)) {
                        try { s.getBasicRemote().sendText(payload); } catch (IOException ignored){}
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理群聊消息
     */
    private void handleGroupMessage(Session session, MessageData messageData, User currentUser) {
        try {
            if (messageData.targetId == null) {
                System.out.println("无效消息：群聊消息必须指定群ID");
                return;
            }

            int groupId = messageData.targetId;
            
            // 检查用户是否为群成员
            if (!groupMemberDao.isMember(groupId, currentUser.getId())) {
                System.out.println("消息发送失败：用户不是该群成员");
                return;
            }

            // 创建群消息对象
            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setGroupId(groupId);
            groupMessage.setSenderId(currentUser.getId());
            groupMessage.setContent(messageData.content);
            groupMessage.setSentAt(new Timestamp(System.currentTimeMillis()));

            // 设置代码消息标志
            boolean isCode = "code".equals(messageData.messageType);
            groupMessage.setCode(isCode);

            // 保存群消息
            long messageId = groupMessageDao.saveMessage(groupMessage);

            // 获取群内所有成员ID
            List<Integer> memberIds = groupMemberDao.getMemberIds(groupId);
            
            // 创建群消息的JSON负载
            String groupMessagePayload = createGroupMessagePayload(groupMessage, currentUser.getUsername(), isCode, null);

            // 向群内所有在线成员发送消息
            synchronized (sessions) {
                for (Session s : sessions) {
                    try {
                        HttpSession httpSession = (HttpSession) s.getUserProperties().get(HttpSession.class.getName());
                        if (httpSession != null) {
                            User sessionUser = (User) httpSession.getAttribute("user");
                            if (sessionUser != null && memberIds.contains(sessionUser.getId())) {
                                s.getBasicRemote().sendText(groupMessagePayload);
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            }
            
            System.out.println("群消息发送成功，群ID: " + groupId + ", 发送者: " + currentUser.getUsername() + ", 消息ID: " + messageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 检查用户是否在与特定用户的私聊界面中
    private boolean isUserInPrivateChatWith(int userId, int otherUserId) {
        synchronized (sessions) {
            for (Session s : sessions) {
                try {
                    HttpSession httpSession = (HttpSession) s.getUserProperties().get(HttpSession.class.getName());
                    if (httpSession == null) continue;

                    User sessionUser = (User) httpSession.getAttribute("user");
                    if (sessionUser == null || sessionUser.getId() != userId) continue;

                    MessageData privateChatData = (MessageData) s.getUserProperties().get("privateChat");
                    System.out.println(privateChatData);
                    if (privateChatData != null && privateChatData.receiverId != null &&
                            privateChatData.receiverId == otherUserId) {
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }

    private boolean shouldSendToSession(Session session, Message message) {
        try {
            HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
            if (httpSession == null) return false;

            User sessionUser = (User) httpSession.getAttribute("user");
            if (sessionUser == null) return false;

            return sessionUser.getId() == message.getSenderId() ||
                    (message.getReceiverId() != null && sessionUser.getId() == message.getReceiverId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String createMessagePayload(Message m) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"message\",\"payload\":{");
        sb.append("\"id\":").append(m.getId()).append(",");
        sb.append("\"senderId\":").append(m.getSenderId()).append(",");
        sb.append("\"senderUsername\":\"").append(escape(m.getSenderUsername())).append("\",");
        if (m.getReceiverId() != null) {
            sb.append("\"receiverId\":").append(m.getReceiverId()).append(",");
            sb.append("\"receiverUsername\":\"").append(escape(m.getReceiverUsername())).append("\",");
        }
        sb.append("\"content\":\"").append(escape(m.getContent())).append("\",");
        sb.append("\"messageType\":\"").append(escape(m.getMessageType())).append("\",");
        sb.append("\"isCode\":").append(m.isCode()).append(",");
        sb.append("\"isRead\":").append(m.isRead()).append(",");
        sb.append("\"createdAt\":\"").append(m.getCreatedAt()).append("\"");
        sb.append("}}");
        return sb.toString();
    }

    private MessageData parseMessage(String json) {
        try {
            MessageData data = new MessageData();

            // 解析type字段
            int typeIdx = json.indexOf("\"type\"");
            if (typeIdx != -1) {
                int col = json.indexOf(':', typeIdx);
                int start = json.indexOf('"', col);
                int end = json.indexOf('"', start+1);
                if (start != -1 && end != -1) {
                    data.type = json.substring(start+1, end);
                }
            }

            // 解析content字段
            int contentIdx = json.indexOf("\"content\"");
            if (contentIdx != -1) {
                int col = json.indexOf(':', contentIdx);
                int start = json.indexOf('"', col);
                int end = json.indexOf('"', start+1);
                if (start != -1 && end != -1) {
                    data.content = json.substring(start+1, end);
                }
            }

            // 解析messageType字段
            int messageTypeIdx = json.indexOf("\"messageType\"");
            if (messageTypeIdx != -1) {
                int col = json.indexOf(':', messageTypeIdx);
                int start = json.indexOf('"', col);
                int end = json.indexOf('"', start+1);
                if (start != -1 && end != -1) {
                    data.messageType = json.substring(start+1, end);
                }
            }

            // 解析chatType字段
            int chatTypeIdx = json.indexOf("\"chatType\"");
            if (chatTypeIdx != -1) {
                int col = json.indexOf(':', chatTypeIdx);
                int start = json.indexOf('"', col);
                int end = json.indexOf('"', start+1);
                if (start != -1 && end != -1) {
                    data.chatType = json.substring(start+1, end);
                }
            }

            // 解析targetId字段
            int targetIdIdx = json.indexOf("\"targetId\"");
            if (targetIdIdx != -1) {
                int col = json.indexOf(':', targetIdIdx);
                if (col != -1) {
                    // 跳过冒号后的空白字符
                    int start = col + 1;
                    while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                        start++;
                    }

                    if (start < json.length()) {
                        if (json.charAt(start) == '"') {
                            // 解析为带引号的字符串（引号中的数字）
                            int end = json.indexOf('"', start + 1);
                            if (end != -1) {
                                try {
                                    data.targetId = Integer.parseInt(json.substring(start + 1, end).trim());
                                } catch (NumberFormatException e) {
                                    // 忽略
                                }
                            }
                        } else {
                            // 解析为不带引号的数字
                            int end = json.indexOf(',', start);
                            if (end == -1) end = json.indexOf('}', start);
                            if (end != -1) {
                                try {
                                    data.targetId = Integer.parseInt(json.substring(start, end).trim());
                                } catch (NumberFormatException e) {
                                    // 忽略
                                }
                            }
                        }
                    }
                }
            }

            // 解析receiverId字段（保留向后兼容）
            int receiverIdIdx = json.indexOf("\"receiverId\"");
            if (receiverIdIdx != -1) {
                int col = json.indexOf(':', receiverIdIdx);
                if (col != -1) {
                    // 跳过冒号后的空白字符
                    int start = col + 1;
                    while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                        start++;
                    }

                    if (start < json.length()) {
                        if (json.charAt(start) == '"') {
                            // 解析为带引号的字符串（引号中的数字）
                            int end = json.indexOf('"', start + 1);
                            if (end != -1) {
                                try {
                                    data.receiverId = Integer.parseInt(json.substring(start + 1, end).trim());
                                } catch (NumberFormatException e) {
                                    // 忽略
                                }
                            }
                        } else {
                            // 解析为不带引号的数字
                            int end = json.indexOf(',', start);
                            if (end == -1) end = json.indexOf('}', start);
                            if (end != -1) {
                                try {
                                    data.receiverId = Integer.parseInt(json.substring(start, end).trim());
                                } catch (NumberFormatException e) {
                                    // 忽略
                                }
                            }
                        }
                    }
                }
            }

            // 解析receiverUsername字段（保留向后兼容）
            int receiverUsernameIdx = json.indexOf("\"receiverUsername\"");
            if (receiverUsernameIdx != -1) {
                int col = json.indexOf(':', receiverUsernameIdx);
                int start = json.indexOf('"', col);
                int end = json.indexOf('"', start+1);
                if (start != -1 && end != -1) {
                    data.receiverUsername = json.substring(start+1, end);
                }
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String escape(String s) {
        if (s==null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }

    private String createGroupMessagePayload(GroupMessage groupMessage, String senderUsername) {
        return createGroupMessagePayload(groupMessage, senderUsername, false, null);
    }

    private String createGroupMessagePayload(GroupMessage groupMessage, String senderUsername, boolean isCode, String language) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"message\",\"payload\":{");
        sb.append("\"id\":").append(groupMessage.getMessageId()).append(",");
        sb.append("\"groupId\":").append(groupMessage.getGroupId()).append(",");
        sb.append("\"senderId\":").append(groupMessage.getSenderId()).append(",");
        sb.append("\"senderUsername\":\"").append(escape(senderUsername)).append("\",");
        sb.append("\"content\":\"").append(escape(groupMessage.getContent())).append("\",");
        sb.append("\"messageType\":\"group\",");
        sb.append("\"isCode\":").append(isCode).append(",");
        if (isCode && language != null) {
            sb.append("\"language\":\"").append(escape(language)).append("\",");
        }
        sb.append("\"isRead\":false,");
        sb.append("\"createdAt\":\"").append(groupMessage.getSentAt()).append("\"");
        sb.append("}}");

        return sb.toString();
    }

    private static class MessageData {
        String type;
        String content;
        String messageType; // 'public' / 'private' / 'code' from client
        String chatType; // 'private' / 'group'
        Integer targetId; // 私聊时为用户ID，群聊时为群ID
        Integer receiverId;
        String receiverUsername;
    }

    private void sendSessionUpdate(com.example.chat.model.ChatSession chatSession, int userId) {
        try {
            String payload = createSessionPayload(chatSession, userId);
            synchronized (sessions) {
                for (Session s : sessions) {
                    HttpSession httpSession = (HttpSession) s.getUserProperties().get(HttpSession.class.getName());
                    if (httpSession != null) {
                        User sessionUser = (User) httpSession.getAttribute("user");
                        if (sessionUser != null && sessionUser.getId() == userId) {
                            try { s.getBasicRemote().sendText(payload); } catch (IOException ignored){}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createSessionPayload(com.example.chat.model.ChatSession chatSession, int userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"session_update\",\"payload\":{");
        sb.append("\"id\":").append(chatSession.getId()).append(",");
        sb.append("\"user1Id\":").append(chatSession.getUser1Id()).append(",");
        sb.append("\"user1Username\":\"").append(escape(chatSession.getUser1Username())).append("\",");
        sb.append("\"user2Id\":").append(chatSession.getUser2Id()).append(",");
        sb.append("\"user2Username\":\"").append(escape(chatSession.getUser2Username())).append("\",");
        sb.append("\"lastMessageId\":").append(chatSession.getLastMessageId()).append(",");

        // 确定要显示的未读计数
        int unreadCount = (userId == chatSession.getUser1Id()) ?
                chatSession.getUnreadCountUser1() : chatSession.getUnreadCountUser2();
        sb.append("\"unreadCount\":").append(unreadCount).append(",");

        sb.append("\"createdAt\":\"").append(chatSession.getCreatedAt()).append("\",");
        sb.append("\"updatedAt\":\"").append(chatSession.getUpdatedAt()).append("\"");
        sb.append("}}");
        return sb.toString();
    }
}









