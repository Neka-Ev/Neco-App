package com.example.chat.servlet;

import com.example.chat.dao.AiChatSessionDao;
import com.example.chat.dao.AiMessageDao;
import com.example.chat.model.AiMessage;
import com.example.chat.model.User;
import com.example.chat.service.AiChatService;
import com.example.chat.service.AiClient;
import com.example.chat.service.DeepSeekAiClient;
import com.example.chat.util.AiConfigUtil;
import com.example.chat.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/ai/send")
public class AiSendMessageServlet extends HttpServlet {

    private AiChatService aiChatService;

    // Simple DTO for response
    public static class AiSendResponse {
        private long sessionId;
        private AiMessage userMessage;
        private AiMessage aiMessage;

        public long getSessionId() {
            return sessionId;
        }

        public void setSessionId(long sessionId) {
            this.sessionId = sessionId;
        }

        public AiMessage getUserMessage() {
            return userMessage;
        }

        public void setUserMessage(AiMessage userMessage) {
            this.userMessage = userMessage;
        }

        public AiMessage getAiMessage() {
            return aiMessage;
        }

        public void setAiMessage(AiMessage aiMessage) {
            this.aiMessage = aiMessage;
        }
    }

    @Override
    public void init() throws ServletException {
        // 读取配置
        AiConfigUtil config = new AiConfigUtil();
        AiClient client;
        client = new DeepSeekAiClient(config.getKey(), config.getUrl(), config.getModel());

        this.aiChatService = new AiChatService(new AiChatSessionDao(), new AiMessageDao(), client);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String body = JsonUtil.readRequestBody(req);
            JsonObject obj = com.example.chat.util.JsonUtil.fromJson(body, JsonObject.class);
            Long sessionId = obj.has("sessionId") && !obj.get("sessionId").isJsonNull() ? obj.get("sessionId").getAsLong() : null;
            String message = obj.get("message").getAsString();

            // 调用 service，获得 AI 回复
            AiMessage aiReply = aiChatService.sendAndReply(sessionId, currentUser.getId(), message);
            // 构造用户消息对象
            AiMessage userMsg = new AiMessage();
            userMsg.setSessionId(aiReply.getSessionId());
            userMsg.setUserId(currentUser.getId());
            userMsg.setRole("USER");
            userMsg.setContent(message);

            AiSendResponse out = new AiSendResponse();
            out.setSessionId(aiReply.getSessionId());
            out.setUserMessage(userMsg);
            out.setAiMessage(aiReply);

            JsonUtil.writeJsonResponse(resp, out);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
