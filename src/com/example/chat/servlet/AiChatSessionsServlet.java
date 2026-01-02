package com.example.chat.servlet;

import com.example.chat.dao.AiChatSessionDao;
import com.example.chat.dao.AiMessageDao;
import com.example.chat.model.AiChatSession;
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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/ai/sessions")
public class AiChatSessionsServlet extends HttpServlet {

    private AiChatService aiChatService;

    @Override
    public void init() throws ServletException {
        // 读取配置
        AiConfigUtil config = new AiConfigUtil();
        AiClient client = new DeepSeekAiClient(config.getKey(), config.getUrl(), config.getModel());
        this.aiChatService = new AiChatService(new AiChatSessionDao(), new AiMessageDao(), client);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userId = currentUser.getId();
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int size = parseIntOrDefault(req.getParameter("pageSize"), 50);
        int offset = (page - 1) * size;
        try {
            List<AiChatSession> list = aiChatService.listSessions(userId, size, offset);
            JsonUtil.writeJsonResponse(resp, list);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String contentType = req.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            // JSON 请求，用于重命名
            try {
                String body = JsonUtil.readRequestBody(req);
                JsonObject obj = JsonUtil.fromJson(body, JsonObject.class);
                long sessionId = obj.get("sessionId").getAsLong();
                String newTitle = obj.get("title").getAsString();

                aiChatService.renameSession(sessionId, currentUser.getId(), newTitle);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            // 表单请求，创建新会话
            String title = req.getParameter("title");
            String model = req.getParameter("model");
            String systemPrompt = req.getParameter("systemPrompt");
            try {
                AiChatSession session = aiChatService.createSession(currentUser.getId(), title, model, systemPrompt);
                JsonUtil.writeJsonResponse(resp, session);
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            long sessionId = Long.parseLong(req.getParameter("sessionId"));
            aiChatService.deleteSession(sessionId, currentUser.getId());
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private int parseIntOrDefault(String v, int def) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }
}
