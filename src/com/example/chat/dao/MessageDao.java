package com.example.chat.dao;

import com.example.chat.model.Message;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    public long insert(Message m) throws SQLException {
        try (Connection c = DbUtil.getConnection()) {
            // 新增 is_code 列，假定数据库已添加 BOOLEAN/TINYINT(1) 类型的 is_code 字段
            String sql = "INSERT INTO messages (sender_id, receiver_id, content, message_type, is_code) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, m.getSenderId());
                if (m.getReceiverId() != null) {
                    ps.setInt(2, m.getReceiverId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setString(3, m.getContent());
                ps.setString(4, m.getMessageType() != null ? m.getMessageType() : "private");
                ps.setBoolean(5, m.isCode());
                int affected = ps.executeUpdate();
                if (affected == 0) return -1;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        m.setId(id);
                        // set createdAt locally (DB also sets it); use current time for immediate use
                        m.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    public List<Message> findPrivateMessages(int userId1, int userId2, int limit) throws SQLException {
        List<Message> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection()) {
            String sql = "SELECT m.id, m.sender_id, m.receiver_id, m.content, m.message_type, m.is_code, m.is_read, m.created_at, " +
                    "u1.username as sender_username, u2.username as receiver_username " +
                    "FROM messages m " +
                    "LEFT JOIN users u1 ON m.sender_id = u1.id " +
                    "LEFT JOIN users u2 ON m.receiver_id = u2.id " +
                    "WHERE m.message_type = 'private' AND ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) " +
                    "ORDER BY m.created_at DESC LIMIT ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId1);
                ps.setInt(2, userId2);
                ps.setInt(3, userId2);
                ps.setInt(4, userId1);
                ps.setInt(5, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(mapResultSetToMessage(rs));
                    }
                }
            }
        }
        return out;
    }

    public List<Message> findUserPrivateMessages(int userId, int limit) throws SQLException {
        List<Message> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection()) {
            String sql = "SELECT m.id, m.sender_id, m.receiver_id, m.content, m.message_type, m.is_code, m.is_read, m.created_at, " +
                    "u1.username as sender_username, u2.username as receiver_username " +
                    "FROM messages m " +
                    "LEFT JOIN users u1 ON m.sender_id = u1.id " +
                    "LEFT JOIN users u2 ON m.receiver_id = u2.id " +
                    "WHERE m.message_type = 'private' AND (m.sender_id = ? OR m.receiver_id = ?) " +
                    "ORDER BY m.created_at DESC LIMIT ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
                ps.setInt(3, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(mapResultSetToMessage(rs));
                    }
                }
            }
        }
        return out;
    }

    public void markMessagesAsRead(int senderId, int receiverId) throws SQLException {
        try (Connection c = DbUtil.getConnection()) {
            String sql = "UPDATE messages SET is_read = TRUE WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.executeUpdate();
            }
        }
    }

    public Message findById(long id) throws SQLException {
        try (Connection c = DbUtil.getConnection()) {
            String sql = "SELECT m.id, m.sender_id, m.receiver_id, m.content, m.message_type, m.is_code, m.is_read, m.created_at, " +
                    "u1.username as sender_username, u2.username as receiver_username " +
                    "FROM messages m " +
                    "LEFT JOIN users u1 ON m.sender_id = u1.id " +
                    "LEFT JOIN users u2 ON m.receiver_id = u2.id " +
                    "WHERE m.id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToMessage(rs);
                    }
                }
            }
        }
        return null;
    }

    public int deleteById(long id) throws SQLException {
        try (Connection c = DbUtil.getConnection()) {
            String sql = "DELETE FROM messages WHERE id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate();
            }
        }
    }

    public List<Message> searchPrivateMessages(int userId1, int userId2, String content, Integer senderId, Timestamp startTime, Timestamp endTime, Boolean isCode, int offset, int limit) throws SQLException {
        List<Message> out = new ArrayList<>();
        try (Connection c = DbUtil.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT m.id, m.sender_id, m.receiver_id, m.content, m.message_type, m.is_code, m.is_read, m.created_at, " +
                    "u1.username as sender_username, u2.username as receiver_username " +
                    "FROM messages m " +
                    "LEFT JOIN users u1 ON m.sender_id = u1.id " +
                    "LEFT JOIN users u2 ON m.receiver_id = u2.id " +
                    "WHERE m.message_type = 'private' AND ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) ");

            List<Object> params = new ArrayList<>();
            params.add(userId1);
            params.add(userId2);
            params.add(userId2);
            params.add(userId1);

            if (content != null && !content.isEmpty()) {
                sql.append("AND m.content LIKE ? ");
                params.add("%" + content + "%");
            }
            if (senderId != null) {
                sql.append("AND m.sender_id = ? ");
                params.add(senderId);
            }
            if (startTime != null) {
                sql.append("AND m.created_at >= ? ");
                params.add(startTime);
            }
            if (endTime != null) {
                sql.append("AND m.created_at <= ? ");
                params.add(endTime);
            }
            if (isCode != null) {
                sql.append("AND m.is_code = ? ");
                params.add(isCode);
            }

            sql.append("ORDER BY m.created_at DESC LIMIT ? OFFSET ?");
            params.add(limit);
            params.add(offset);

            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(mapResultSetToMessage(rs));
                    }
                }
            }
        }
        return out;
    }

    public int countSearchPrivateMessages(int userId1, int userId2, String content, Integer senderId, Timestamp startTime, Timestamp endTime, Boolean isCode) throws SQLException {
        try (Connection c = DbUtil.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) " +
                    "FROM messages m " +
                    "WHERE m.message_type = 'private' AND ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?)) ");

            List<Object> params = new ArrayList<>();
            params.add(userId1);
            params.add(userId2);
            params.add(userId2);
            params.add(userId1);

            if (content != null && !content.isEmpty()) {
                sql.append("AND m.content LIKE ? ");
                params.add("%" + content + "%");
            }
            if (senderId != null) {
                sql.append("AND m.sender_id = ? ");
                params.add(senderId);
            }
            if (startTime != null) {
                sql.append("AND m.created_at >= ? ");
                params.add(startTime);
            }
            if (endTime != null) {
                sql.append("AND m.created_at <= ? ");
                params.add(endTime);
            }
            if (isCode != null) {
                sql.append("AND m.is_code = ? ");
                params.add(isCode);
            }

            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return 0;
    }

    public void deleteMessages(List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;
        try (Connection c = DbUtil.getConnection()) {
            StringBuilder sql = new StringBuilder("DELETE FROM messages WHERE id IN (");
            for (int i = 0; i < ids.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(")");
            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                for (int i = 0; i < ids.size(); i++) {
                    ps.setLong(i + 1, ids.get(i));
                }
                ps.executeUpdate();
            }
        }
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getLong("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null);
        m.setContent(rs.getString("content"));
        m.setMessageType(rs.getString("message_type"));
        // 从 is_code 列映射到模型字段
        try {
            m.setCode(rs.getBoolean("is_code"));
        } catch (SQLException ignored) {
            // 向后兼容：如果旧表中没有 is_code 列，则忽略
        }
        m.setRead(rs.getBoolean("is_read"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        m.setSenderUsername(rs.getString("sender_username"));
        m.setReceiverUsername(rs.getString("receiver_username"));
        return m;
    }
}