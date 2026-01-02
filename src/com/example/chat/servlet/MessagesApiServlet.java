package com.example.chat.servlet;

import com.example.chat.dao.MessageDao;
import com.example.chat.model.Message;
import com.example.chat.model.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/api/messages"})
public class MessagesApiServlet extends HttpServlet {
    private MessageDao messageDao = new MessageDao();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        String action = req.getParameter("action");
        if ("search".equals(action)) {
            handleSearch(req, resp, currentUser);
            return;
        }

        int limit = 50;
        String limitStr = req.getParameter("limit");
        if (limitStr != null) {
            try { limit = Integer.parseInt(limitStr); } catch(NumberFormatException ignored){}
        }
        
        String privateUserId = req.getParameter("private");
        
        try {
            List<Message> list;
            if (privateUserId != null && !privateUserId.isEmpty()) {
                // Load private messages between current user and specified user
                int targetUserId = Integer.parseInt(privateUserId);
                list = messageDao.findPrivateMessages(currentUser.getId(), targetUserId, limit);
            } else {
                // No public messages available anymore
                list = new ArrayList<>();
            }
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print('[');
            boolean first = true;
            for (Message m : list) {
                if (!first) out.print(',');
                first = false;
                out.print('{');
                out.print("\"id\":"+m.getId()+",");
                out.print("\"senderId\":"+m.getSenderId()+",");
                out.print("\"senderUsername\":\""+escape(m.getSenderUsername())+"\",");
                if (m.getReceiverId() != null) {
                    out.print("\"receiverId\":"+m.getReceiverId()+",");
                    out.print("\"receiverUsername\":\""+escape(m.getReceiverUsername())+"\",");
                }
                out.print("\"content\":\""+escape(m.getContent())+"\",");
                out.print("\"messageType\":\""+escape(m.getMessageType())+"\",");
                out.print("\"isCode\":"+m.isCode()+",");
                out.print("\"isRead\":"+m.isRead()+",");
                out.print("\"createdAt\":\""+(m.getCreatedAt() != null ? sdf.format(m.getCreatedAt()) : "")+"\"");
                out.print('}');
            }
            out.print(']');
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"Server error\"}");
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"Invalid user ID\"}");
        }
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String privateUserIdStr = req.getParameter("private");
        if (privateUserIdStr == null) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Missing private user id\"}");
            return;
        }
        int targetUserId = Integer.parseInt(privateUserIdStr);

        String content = req.getParameter("content");
        String senderIdStr = req.getParameter("senderId");
        Integer senderId = (senderIdStr != null && !senderIdStr.isEmpty()) ? Integer.parseInt(senderIdStr) : null;

        String startTimeStr = req.getParameter("startTime");
        Timestamp startTime = (startTimeStr != null && !startTimeStr.isEmpty()) ? Timestamp.valueOf(startTimeStr + " 00:00:00") : null;

        String endTimeStr = req.getParameter("endTime");
        Timestamp endTime = (endTimeStr != null && !endTimeStr.isEmpty()) ? Timestamp.valueOf(endTimeStr + " 23:59:59") : null;

        String isCodeStr = req.getParameter("isCode");
        Boolean isCode = (isCodeStr != null && !isCodeStr.isEmpty()) ? "1".equals(isCodeStr) || "true".equals(isCodeStr) : null;

        int page = 1;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}

        int pageSize = 10;
        try {
            String pageSizeStr = req.getParameter("pageSize");
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                pageSize = Integer.parseInt(pageSizeStr);
            }
        } catch (Exception ignored) {}

        int offset = (page - 1) * pageSize;

        try {
            List<Message> list = messageDao.searchPrivateMessages(currentUser.getId(), targetUserId, content, senderId, startTime, endTime, isCode, offset, pageSize);
            int total = messageDao.countSearchPrivateMessages(currentUser.getId(), targetUserId, content, senderId, startTime, endTime, isCode);

            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"total\":" + total + ",\"page\":" + page + ",\"pageSize\":" + pageSize + ",\"messages\":[");
            boolean first = true;
            for (Message m : list) {
                if (!first) out.print(',');
                first = false;
                out.print('{');
                out.print("\"id\":"+m.getId()+",");
                out.print("\"senderId\":"+m.getSenderId()+",");
                out.print("\"senderUsername\":\""+escape(m.getSenderUsername())+"\",");
                if (m.getReceiverId() != null) {
                    out.print("\"receiverId\":"+m.getReceiverId()+",");
                    out.print("\"receiverUsername\":\""+escape(m.getReceiverUsername())+"\",");
                }
                out.print("\"content\":\""+escape(m.getContent())+"\",");
                out.print("\"messageType\":\""+escape(m.getMessageType())+"\",");
                out.print("\"isCode\":"+m.isCode()+",");
                out.print("\"isRead\":"+m.isRead()+",");
                out.print("\"createdAt\":\""+(m.getCreatedAt() != null ? sdf.format(m.getCreatedAt()) : "")+"\"");
                out.print('}');
            }
            out.print("]}");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"Unauthorized\"}");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        String idsParam = req.getParameter("ids");
        if (idsParam != null) {
            // Batch delete
            try {
                List<Long> ids = Arrays.stream(idsParam.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                // Note: In a real app, we should verify ownership of these messages
                messageDao.deleteMessages(ids);
                resp.setStatus(200);
                resp.getWriter().print("{\"success\":true}");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(500);
                resp.getWriter().print("{\"error\":\"Delete failed\"}");
            }
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(400);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"Missing message id\"}");
            return;
        }

        long messageId;
        try {
            messageId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"Invalid message id\"}");
            return;
        }

        try {
            Message m = messageDao.findById(messageId);
            if (m == null) {
                resp.setStatus(404);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"error\":\"Message not found\"}");
                return;
            }

            int currentUserId = currentUser.getId();
            Integer receiverId = m.getReceiverId();
            if (currentUserId != m.getSenderId() && (receiverId == null || currentUserId != receiverId)) {
                resp.setStatus(403);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"error\":\"Forbidden\"}");
                return;
            }

            int affected = messageDao.deleteById(messageId);
            if (affected == 0) {
                resp.setStatus(404);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().print("{\"error\":\"Message not found\"}");
                return;
            }

            resp.setStatus(200);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"success\":true}");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print("{\"error\":\"Server error\"}");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
}