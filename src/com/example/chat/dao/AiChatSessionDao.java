package com.example.chat.dao;

import com.example.chat.model.AiChatSession;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AiChatSessionDao {

    public AiChatSession createSession(int userId, String title, String model, String systemPrompt) throws SQLException {
        String sql = "INSERT INTO ai_chat_sessions (user_id, title, model, status, system_prompt, created_at, updated_at) " +
                "VALUES (?, ?, ?, 'active', ?, NOW(), NOW())";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, model);
            ps.setString(4, systemPrompt);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return findByIdAndUserId(id, userId);
                }
            }
        }
        return null;
    }

    public AiChatSession findByIdAndUserId(long id, int userId) throws SQLException {
        String sql = "SELECT * FROM ai_chat_sessions WHERE id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<AiChatSession> findSessionsByUserId(int userId, int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM ai_chat_sessions WHERE user_id = ? ORDER BY updated_at DESC LIMIT ? OFFSET ?";
        List<AiChatSession> list = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void updateSessionTitle(long id, int userId, String title) throws SQLException {
        String sql = "UPDATE ai_chat_sessions SET title = ?, updated_at = NOW() WHERE id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setLong(2, id);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void deleteSession(long id, int userId) throws SQLException {
        String sql = "DELETE FROM ai_chat_sessions WHERE id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateLastUpdated(long id) throws SQLException {
        String sql = "UPDATE ai_chat_sessions SET updated_at = NOW() WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private AiChatSession map(ResultSet rs) throws SQLException {
        AiChatSession s = new AiChatSession();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setTitle(rs.getString("title"));
        s.setModel(rs.getString("model"));
        s.setStatus(rs.getString("status"));
        s.setSystemPrompt(rs.getString("system_prompt"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}
