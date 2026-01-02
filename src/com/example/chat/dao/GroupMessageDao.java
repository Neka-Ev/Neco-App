package com.example.chat.dao;

import com.example.chat.model.GroupMessage;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMessageDao {

    public long saveMessage(GroupMessage message) {
        String sql = "INSERT INTO group_messages (group_id, sender_id, content, sent_at, is_code) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getGroupId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, message.getSentAt());
            stmt.setBoolean(5, message.isCode());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            // Fallback if is_code column doesn't exist
            if (e.getMessage().contains("Unknown column 'is_code'")) {
                 return saveMessageFallback(message);
            }
            e.printStackTrace();
        }
        return -1;
    }

    private long saveMessageFallback(GroupMessage message) {
        String sql = "INSERT INTO group_messages (group_id, sender_id, content, sent_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getGroupId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, message.getSentAt());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public boolean deleteMessage(long messageId) {
        String sql = "DELETE FROM group_messages WHERE message_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<GroupMessage> getHistory(int groupId, int offset, int limit) {
        List<GroupMessage> messages = new ArrayList<>();
        // 关联 users 表获取 username
        String sql = "SELECT gm.*, u.username FROM group_messages gm JOIN users u ON gm.sender_id = u.id WHERE gm.group_id = ? ORDER BY gm.sent_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToGroupMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 因为是倒序查出来的（最新的在前），为了前端显示习惯（旧在上新在下），需要反转
        // 或者前端使用 prepend。但前端代码是 appendChild。
        // 所以这里反转列表。
        java.util.Collections.reverse(messages);
        return messages;
    }

    public List<GroupMessage> searchMessages(int groupId, String content, Integer senderId, String startDate, String endDate, Boolean isCode, int offset, int limit) {
        List<GroupMessage> messages = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT gm.*, u.username FROM group_messages gm JOIN users u ON gm.sender_id = u.id WHERE gm.group_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(groupId);

        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND gm.content LIKE ?");
            params.add("%" + content + "%");
        }
        if (senderId != null) {
            sql.append(" AND gm.sender_id = ?");
            params.add(senderId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND gm.sent_at >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND gm.sent_at <= ?");
            params.add(endDate);
        }
        if (isCode != null) {
             // Check if column exists or just ignore if not sure?
             // Assuming we added it or it exists.
             sql.append(" AND gm.is_code = ?");
             params.add(isCode);
        }

        sql.append(" ORDER BY gm.sent_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToGroupMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public int countMessages(int groupId, String content, Integer senderId, String startDate, String endDate, Boolean isCode) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM group_messages gm WHERE gm.group_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(groupId);

        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND gm.content LIKE ?");
            params.add("%" + content + "%");
        }
        if (senderId != null) {
            sql.append(" AND gm.sender_id = ?");
            params.add(senderId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND gm.sent_at >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND gm.sent_at <= ?");
            params.add(endDate);
        }
        if (isCode != null) {
             sql.append(" AND gm.is_code = ?");
             params.add(isCode);
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private GroupMessage mapResultSetToGroupMessage(ResultSet rs) throws SQLException {
        GroupMessage message = new GroupMessage();
        message.setMessageId(rs.getLong("message_id"));
        message.setGroupId(rs.getInt("group_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setContent(rs.getString("content"));
        message.setSentAt(rs.getTimestamp("sent_at"));
        message.setSenderUsername(rs.getString("username"));
        try {
            message.setCode(rs.getBoolean("is_code"));
        } catch (SQLException e) {
            // column might not exist
            message.setCode(false);
        }
        return message;
    }
}