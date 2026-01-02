package com.example.chat.service;

import com.example.chat.dao.AiChatSessionDao;
import com.example.chat.dao.AiMessageDao;
import com.example.chat.model.AiChatSession;
import com.example.chat.model.AiMessage;

import java.sql.SQLException;
import java.util.List;

public class AiChatService {

    private final AiChatSessionDao sessionDao;
    private final AiMessageDao messageDao;
    private final AiClient aiClient;

    public AiChatService(AiChatSessionDao sessionDao, AiMessageDao messageDao, AiClient aiClient) {
        this.sessionDao = sessionDao;
        this.messageDao = messageDao;
        this.aiClient = aiClient;
    }

    public AiChatSession createSession(int userId, String title, String model, String systemPrompt) throws SQLException {
        if (title == null || title.trim().isEmpty()) {
            title = "AI 会话"; // default title
        }
        if (model == null || model.trim().isEmpty()) {
            model = "deepseekv1";
        }
        return sessionDao.createSession(userId, title.trim(), model.trim(), systemPrompt);
    }

    public List<AiChatSession> listSessions(int userId, int limit, int offset) throws SQLException {
        return sessionDao.findSessionsByUserId(userId, limit, offset);
    }

    public List<AiMessage> getMessages(long sessionId, int userId, int limit, int offset) throws SQLException {
        return messageDao.findMessagesBySession(sessionId, userId, limit, offset);
    }

    public AiMessage sendAndReply(Long sessionId, int userId, String content) throws Exception {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("message content is empty");
        }
        String trimmed = content.trim();
        AiChatSession session;
        if (sessionId == null || sessionId <= 0) {
            String title = trimmed.length() > 20 ? trimmed.substring(0, 20) + "..." : trimmed;
            session = createSession(userId, title, "mock", null);
        } else {
            session = sessionDao.findByIdAndUserId(sessionId, userId);
            if (session == null) {
                throw new IllegalArgumentException("session not found");
            }
        }

        // save user message
        AiMessage userMsg = new AiMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setUserId(userId);
        userMsg.setRole("USER");
        userMsg.setContent(trimmed);
        userMsg.setErrorFlag(false);
        messageDao.insert(userMsg);

        // build context
        List<AiMessage> context = messageDao.findRecentMessagesForContext(session.getId(), userId, 30);

        // call AI client
        String replyContent;
        AiMessage aiMsg = new AiMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setUserId(userId);
        aiMsg.setRole("ASSISTANT");
        try {
            replyContent = aiClient.chat(context, trimmed);
            aiMsg.setContent(replyContent);
            aiMsg.setErrorFlag(false);
        } catch (Exception ex) {
            aiMsg.setContent("AI 错误: " + ex.getMessage());
            aiMsg.setErrorFlag(true);
            throw ex;
        } finally {
            messageDao.insert(aiMsg);
            sessionDao.updateLastUpdated(session.getId());
        }

        return aiMsg;
    }

    public void renameSession(long sessionId, int userId, String newTitle) throws SQLException {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("title is empty");
        }
        AiChatSession s = sessionDao.findByIdAndUserId(sessionId, userId);
        if (s == null) {
            throw new IllegalArgumentException("session not found");
        }
        sessionDao.updateSessionTitle(sessionId, userId, newTitle.trim());
    }

    public void deleteSession(long sessionId, int userId) throws SQLException {
        AiChatSession s = sessionDao.findByIdAndUserId(sessionId, userId);
        if (s == null) {
            throw new IllegalArgumentException("session not found");
        }
        sessionDao.deleteSession(sessionId, userId);
    }
}
