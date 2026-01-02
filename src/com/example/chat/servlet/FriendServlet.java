package com.example.chat.servlet;

import com.example.chat.model.User;
import com.example.chat.util.DbUtil;
import com.example.chat.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/friends/*"})
public class FriendServlet extends HttpServlet {

    private static final int RELATION_TYPE_FRIEND = 1;
    private static final int RELATION_TYPE_BLOCK = 3;
    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_REMOVED = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/status")) {
            //  查询两用户关系状态
            handleGetRelationStatus(request, response);
        } else if (pathInfo != null && pathInfo.equals("/list")) {
            // 获取好友列表
            handleGetFriendsList(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的请求路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/add")) {
            // 添加好友
            handleAddFriend(request, response);
        } else if (pathInfo != null && pathInfo.equals("/remove")) {
            // 解除好友
            handleRemoveFriend(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的请求路径");
        }
    }

    /**
     * 查询两用户关系状态
     */
    private void handleGetRelationStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        int currentUserId = currentUser.getId();

        // 获取目标用户ID
        String targetUserIdParam = request.getParameter("targetUserId");
        if (targetUserIdParam == null || targetUserIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "缺少目标用户ID");
            return;
        }

        int targetUserId;
        try {
            targetUserId = Integer.parseInt(targetUserIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的目标用户ID");
            return;
        }

        try {
            boolean isFriend = isFriend(currentUserId, targetUserId);
            boolean isBlocked = isBlocked(currentUserId, targetUserId);
            boolean isBlockedByTarget = isBlocked(targetUserId, currentUserId);

            Map<String, Object> statusData = new HashMap<>();
            statusData.put("isFriend", isFriend);
            statusData.put("isBlocked", isBlocked);
            statusData.put("isBlockedByTarget", isBlockedByTarget);

            JsonUtil.writeJsonResponse(response, statusData);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "查询关系状态失败");
        }
    }

    /**
     * 添加好友
     */
    private void handleAddFriend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        int currentUserId = currentUser.getId();

        // 解析请求体
        String requestBody = JsonUtil.readRequestBody(request);
        Map<String, Object> requestData = JsonUtil.fromJson(requestBody, Map.class);
        Object targetUserIdObj = requestData.get("targetUserId");
        Integer targetUserId = null;
        if (targetUserIdObj != null) {
            if (targetUserIdObj instanceof Double) {
                targetUserId = ((Double) targetUserIdObj).intValue();
            } else if (targetUserIdObj instanceof Integer) {
                targetUserId = (Integer) targetUserIdObj;
            } else if (targetUserIdObj instanceof String) {
                try {
                    targetUserId = Integer.parseInt((String) targetUserIdObj);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (targetUserId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "缺少目标用户ID");
            return;
        }

        if (currentUserId == targetUserId) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "不能添加自己为好友");
            return;
        }

        try {
            // 检查是否已经是好友
            if (isFriend(currentUserId, targetUserId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "已经是好友关系");
                return;
            }

            // 检查是否存在拉黑关系
            if (isBlocked(currentUserId, targetUserId) || isBlocked(targetUserId, currentUserId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "存在拉黑关系，无法添加好友");
                return;
            }

            // 添加好友关系
            boolean success = addFriendRelation(currentUserId, targetUserId);
            if (success) {
                JsonUtil.writeJsonSuccess(response, "好友添加成功");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "好友添加失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "好友添加失败: " + e.getMessage());
        }
    }

    /**
     * 解除好友
     */
    private void handleRemoveFriend(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        int currentUserId = currentUser.getId();

        // 解析请求体
        String requestBody = JsonUtil.readRequestBody(request);
        Map<String, Object> requestData = JsonUtil.fromJson(requestBody, Map.class);
        Object targetUserIdObj = requestData.get("targetUserId");
        Integer targetUserId = null;
        if (targetUserIdObj != null) {
            // 处理Gson将数字转换为Double的问题
            if (targetUserIdObj instanceof Double) {
                targetUserId = ((Double) targetUserIdObj).intValue();
            } else if (targetUserIdObj instanceof Integer) {
                targetUserId = (Integer) targetUserIdObj;
            } else if (targetUserIdObj instanceof String) {
                try {
                    targetUserId = Integer.parseInt((String) targetUserIdObj);
                } catch (NumberFormatException e) {
                    // 字符串转换失败
                }
            }
        }

        if (targetUserId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "缺少目标用户ID");
            return;
        }

        try {
            // 检查是否是好友
            if (!isFriend(currentUserId, targetUserId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "不是好友关系");
                return;
            }

            // 解除好友关系（双向）
            boolean success1 = removeFriendRelation(currentUserId, targetUserId);
            boolean success2 = removeFriendRelation(targetUserId, currentUserId);

            if (success1 && success2) {
                JsonUtil.writeJsonSuccess(response, "好友关系已解除");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "解除好友关系失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "解除好友关系失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否是好友关系
     */
    public static boolean isFriend(int userId, int targetUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM friend_relations ";
        sql += "WHERE user_id = ? AND target_user_id = ? ";
        sql += "AND relation_type = ? AND status = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, targetUserId);
            stmt.setInt(3, RELATION_TYPE_FRIEND);
            stmt.setInt(4, STATUS_NORMAL);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * 检查是否存在拉黑关系
     */
    public static boolean isBlocked(int userId, int targetUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM friend_relations ";
        sql += "WHERE user_id = ? AND target_user_id = ? ";
        sql += "AND relation_type = ? AND status = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, targetUserId);
            stmt.setInt(3, RELATION_TYPE_BLOCK);
            stmt.setInt(4, STATUS_NORMAL);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * 添加好友关系
     */
    private boolean addFriendRelation(int userId, int targetUserId) throws SQLException {
        boolean success = false;
        
        // 使用事务确保双向关系同时成功或失败
        try (Connection conn = DbUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO friend_relations (user_id, target_user_id, relation_type, status, created_at) ";
            sql += "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) ";
            sql += "ON DUPLICATE KEY UPDATE status = ?";

            // 添加从userId到targetUserId的关系
            try (PreparedStatement stmt1 = conn.prepareStatement(sql)) {
                stmt1.setInt(1, userId);
                stmt1.setInt(2, targetUserId);
                stmt1.setInt(3, RELATION_TYPE_FRIEND);
                stmt1.setInt(4, STATUS_NORMAL);
                stmt1.setInt(5, STATUS_NORMAL);
                stmt1.executeUpdate();
            }

            // 添加从targetUserId到userId的关系
            try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                stmt2.setInt(1, targetUserId);
                stmt2.setInt(2, userId);
                stmt2.setInt(3, RELATION_TYPE_FRIEND);
                stmt2.setInt(4, STATUS_NORMAL);
                stmt2.setInt(5, STATUS_NORMAL);
                stmt2.executeUpdate();
            }

            conn.commit();
            success = true;
        } catch (SQLException e) {
            // 事务失败，会自动回滚
            throw e;
        }

        return success;
    }

    /**
     * 解除好友关系
     */
    private boolean removeFriendRelation(int userId, int targetUserId) throws SQLException {
        // 先检查是否存在相同的记录但状态为REMOVED的情况
        String checkSql = "SELECT COUNT(*) FROM friend_relations " +
                         "WHERE user_id = ? AND target_user_id = ? " +
                         "AND relation_type = ? AND status = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, targetUserId);
            checkStmt.setInt(3, RELATION_TYPE_FRIEND);
            checkStmt.setInt(4, STATUS_REMOVED);
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // 如果已经存在状态为REMOVED的记录，先删除它
                String deleteSql = "DELETE FROM friend_relations " +
                                  "WHERE user_id = ? AND target_user_id = ? " +
                                  "AND relation_type = ? AND status = ?";
                
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, userId);
                    deleteStmt.setInt(2, targetUserId);
                    deleteStmt.setInt(3, RELATION_TYPE_FRIEND);
                    deleteStmt.setInt(4, STATUS_REMOVED);
                    deleteStmt.executeUpdate();
                }
            }
        }
        
        // 然后更新原记录的状态
        String sql = "UPDATE friend_relations SET status = ? " +
                   "WHERE user_id = ? AND target_user_id = ? " +
                   "AND relation_type = ? AND status = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, STATUS_REMOVED);
            stmt.setInt(2, userId);
            stmt.setInt(3, targetUserId);
            stmt.setInt(4, RELATION_TYPE_FRIEND);
            stmt.setInt(5, STATUS_NORMAL);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 处理获取好友列表请求
     */
    private void handleGetFriendsList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        int currentUserId = currentUser.getId();

        try {
            // 获取当前用户的所有好友
            List<User> friends = getFriendList(currentUserId);
            
            // 构造响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("friends", friends);
            responseData.put("total", friends.size());
            
            JsonUtil.writeJsonResponse(response, responseData);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "获取好友列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的好友列表
     */
    private List<User> getFriendList(int userId) throws SQLException {
        List<User> friends = new ArrayList<>();
        
        String sql = "SELECT u.id, u.username, u.display_name, u.email, u.created_at, u.is_online, u.last_seen, u.avatar_url " +
                     "FROM friend_relations fr " +
                     "JOIN users u ON fr.target_user_id = u.id " +
                     "WHERE fr.user_id = ? AND fr.relation_type = ? AND fr.status = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, RELATION_TYPE_FRIEND);
            stmt.setInt(3, STATUS_NORMAL);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User friend = new User();
                friend.setId(rs.getInt("id"));
                friend.setUsername(rs.getString("username"));
                friend.setDisplayName(rs.getString("display_name"));
                friend.setEmail(rs.getString("email"));
                friend.setCreatedAt(rs.getTimestamp("created_at"));
                friend.setOnline(rs.getBoolean("is_online"));
                friend.setLastSeen(rs.getTimestamp("last_seen"));
                try { friend.setAvatarUrl(rs.getString("avatar_url")); } catch (SQLException ignored) {}

                friends.add(friend);
            }
        }
        
        return friends;
    }
}
