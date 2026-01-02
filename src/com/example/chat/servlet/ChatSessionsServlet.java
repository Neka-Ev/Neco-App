package com.example.chat.servlet;

import com.example.chat.dao.ChatSessionDao;
import com.example.chat.dao.MessageDao;
import com.example.chat.model.ChatSession;
import com.example.chat.model.User;
import com.example.chat.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/sessions")
public class ChatSessionsServlet extends HttpServlet {
    
    private ChatSessionDao chatSessionDao;
    private MessageDao messageDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        chatSessionDao = new ChatSessionDao();
        messageDao = new MessageDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonResponse(response, "error", "用户未登录");
            return;
        }
        
        User currentUser = (User) session.getAttribute("user");
        
        try {
            List<ChatSession> sessions = chatSessionDao.findSessionsByUserId(currentUser.getId());
            JsonUtil.writeJsonResponse(response, sessions);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonResponse(response, "error", "获取会话列表失败");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonResponse(response, "error", "用户未登录");
            return;
        }
        
        User currentUser = (User) session.getAttribute("user");
        
        try {
            String requestBody = JsonUtil.readRequestBody(request);
            ChatSessionRequest sessionRequest = JsonUtil.fromJson(requestBody, ChatSessionRequest.class);
            
            if (sessionRequest.getOtherUserId() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonResponse(response, "error", "无效的用户ID");
                return;
            }
            
            // 创建或更新会话
            ChatSession chatSession = chatSessionDao.findOrCreateSession(
                currentUser.getId(), 
                sessionRequest.getOtherUserId(),
                currentUser.getUsername(),
                sessionRequest.getOtherUsername()
            );
            
            // 如果有消息ID，更新最后消息和未读计数
            if (sessionRequest.getMessageId() > 0) {
                chatSessionDao.updateLastMessage(chatSession.getId(), sessionRequest.getMessageId());
                
                // 如果是当前用户发送的消息，重置对方的未读计数
                chatSessionDao.incrementUnreadCount(chatSession.getId(), sessionRequest.getOtherUserId());
            }
            
            JsonUtil.writeJsonResponse(response, chatSession);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonResponse(response, "error", "创建会话失败");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonResponse(response, "error", "请求格式错误");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonResponse(response, "error", "用户未登录");
            return;
        }

        String sessionIdStr = request.getParameter("id");
        if (sessionIdStr == null || sessionIdStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonResponse(response, "error", "缺少会话ID");
            return;
        }

        try {
            long sessionId = Long.parseLong(sessionIdStr);
            chatSessionDao.deleteSession(sessionId);
            JsonUtil.writeJsonResponse(response, "success", "true");
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonResponse(response, "error", "无效的会话ID");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonResponse(response, "error", "删除会话失败");
        }
    }

    // 内部请求类
    private static class ChatSessionRequest {
        private int otherUserId;
        private String otherUsername;
        private long messageId;
        
        public int getOtherUserId() { return otherUserId; }
        public void setOtherUserId(int otherUserId) { this.otherUserId = otherUserId; }
        
        public String getOtherUsername() { return otherUsername; }
        public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }
        
        public long getMessageId() { return messageId; }
        public void setMessageId(long messageId) { this.messageId = messageId; }


    }
}