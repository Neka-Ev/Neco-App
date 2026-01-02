package com.example.chat.servlet;

import com.example.chat.dao.ChatSessionDao;
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

@WebServlet("/api/clear-unread")
public class ClearUnreadServlet extends HttpServlet {

    private ChatSessionDao chatSessionDao;

    @Override
    public void init() throws ServletException {
        super.init();
        chatSessionDao = new ChatSessionDao();
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
            // 从请求参数中获取会话ID
            String sessionIdParam = request.getParameter("sessionId");
            if (sessionIdParam == null || sessionIdParam.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonResponse(response, "error", "缺少会话ID参数");
                return;
            }

            long sessionId;
            try {
                sessionId = Long.parseLong(sessionIdParam);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonResponse(response, "error", "无效的会话ID");
                return;
            }

            chatSessionDao.resetUnreadCount(sessionId, currentUser.getId());

            JsonUtil.writeJsonResponse(response, "success", "未读计数已清除");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonResponse(response, "error", "清除未读计数失败");
        }
    }
}
