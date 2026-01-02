package com.example.chat.dao;

import com.example.chat.model.AiMessage;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AiMessageDao {

    public AiMessage insert(AiMessage m) throws SQLException {
        String sql = "INSERT INTO ai_messages (session_id, user_id, role, content, token_count, error_flag, raw_ai_metadata, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getSessionId());
            ps.setInt(2, m.getUserId());
            ps.setString(3, m.getRole());
            ps.setString(4, m.getContent());
            if (m.getTokenCount() != null) {
                ps.setInt(5, m.getTokenCount());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setBoolean(6, m.isErrorFlag());
            ps.setString(7, m.getRawAiMetadata());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    m.setId(id);
                    m.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                }
            }
        }
        return m;
    }

    public List<AiMessage> findMessagesBySession(long sessionId, int userId, int limit, int offset) throws SQLException {
        String sql = "SELECT m.* FROM ai_messages m " +
                "JOIN ai_chat_sessions s ON m.session_id = s.id " +
                "WHERE m.session_id = ? AND s.user_id = ? " +
                "ORDER BY m.created_at ASC LIMIT ? OFFSET ?";
        List<AiMessage> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.setInt(2, userId);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<AiMessage> findRecentMessagesForContext(long sessionId, int userId, int maxCount) throws SQLException {
        String sql = "SELECT m.* FROM ai_messages m " +
                "JOIN ai_chat_sessions s ON m.session_id = s.id " +
                "WHERE m.session_id = ? AND s.user_id = ? " +
                "ORDER BY m.created_at DESC LIMIT ?";
        List<AiMessage> reversed = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.setInt(2, userId);
            ps.setInt(3, maxCount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reversed.add(map(rs));
                }
            }
        }
        // reverse to chronological order
        List<AiMessage> chronological = new ArrayList<>();
        for (int i = reversed.size() - 1; i >= 0; i--) {
            chronological.add(reversed.get(i));
        }
        return chronological;
    }

    private AiMessage map(ResultSet rs) throws SQLException {
        AiMessage m = new AiMessage();
        m.setId(rs.getLong("id"));
        m.setSessionId(rs.getLong("session_id"));
        m.setUserId(rs.getInt("user_id"));
        m.setRole(rs.getString("role"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        int tokenCount = rs.getInt("token_count");
        m.setTokenCount(rs.wasNull() ? null : tokenCount);
        m.setErrorFlag(rs.getBoolean("error_flag"));
        m.setRawAiMetadata(rs.getString("raw_ai_metadata"));
        return m;
    }
}

