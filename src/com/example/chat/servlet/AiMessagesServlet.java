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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/ai/messages")
public class AiMessagesServlet extends HttpServlet {

    private AiChatService aiChatService;

    @Override
    public void init() throws ServletException {
        // 读取配置
        AiConfigUtil config = new AiConfigUtil();
        AiClient client;
        client = new DeepSeekAiClient(config.getKey(), config.getUrl(), config.getModel());

        this.aiChatService = new AiChatService(new AiChatSessionDao(), new AiMessageDao(), client);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long sessionId;
        try {
            sessionId = Long.parseLong(req.getParameter("sessionId"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int size = parseIntOrDefault(req.getParameter("pageSize"), 50);
        int offset = (page - 1) * size;
        try {
            List<AiMessage> list = aiChatService.getMessages(sessionId, currentUser.getId(), size, offset);
            JsonUtil.writeJsonResponse(resp, list);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

