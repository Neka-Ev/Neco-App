package com.example.chat.dao;

import com.example.chat.model.ChatSession;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatSessionDao {
    
    public ChatSession findOrCreateSession(int user1Id, int user2Id, String user1Username, String user2Username) 
            throws SQLException {
        int minUserId = user1Id;
        int maxUserId = user2Id;
        // 确保user1Id < user2Id以保持一致性
        if(user1Id > user2Id) {
            int tempId = user1Id;
            minUserId = user2Id;
            maxUserId = tempId;

            String tempUsername = user1Username;
            user1Username = user2Username;
            user2Username = tempUsername;
        }
        
        String sql = "SELECT * FROM chat_sessions WHERE user1_id = ? AND user2_id = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, minUserId);
            stmt.setInt(2, maxUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChatSession(rs);
                }
            }
        }
        
        // 如果不存在，创建新会话
        return createSession(minUserId, maxUserId, user1Username, user2Username);
    }
    
    public ChatSession createSession(int user1Id, int user2Id, String user1Username, String user2Username) 
            throws SQLException {
        
        String sql = "INSERT INTO chat_sessions (user1_id, user2_id, user1_username, user2_username, " +
                     "unread_count_user1, unread_count_user2, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, 0, 0, NOW(), NOW())";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setString(3, user1Username);
            stmt.setString(4, user2Username);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("创建会话失败");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long sessionId = generatedKeys.getLong(1);
                    return findSessionById(sessionId);
                } else {
                    throw new SQLException("创建会话失败，无法获取ID");
                }
            }
        }
    }
    
    public ChatSession findSessionById(long sessionId) throws SQLException {
        String sql = "SELECT * FROM chat_sessions WHERE id = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChatSession(rs);
                }
            }
        }
        return null;
    }
    
    public List<ChatSession> findSessionsByUserId(int userId) throws SQLException {
        List<ChatSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM chat_sessions WHERE user1_id = ? OR user2_id = ? ORDER BY updated_at DESC";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToChatSession(rs));
                }
            }
        }
        return sessions;
    }
    
    public void updateLastMessage(long sessionId, long messageId) throws SQLException {
        String sql = "UPDATE chat_sessions SET last_message_id = ?, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, messageId);
            stmt.setLong(2, sessionId);
            
            stmt.executeUpdate();
        }
    }
    
    public void incrementUnreadCount(long sessionId, int userId) throws SQLException {
        String fieldName = (userId == getSessionUser1Id(sessionId)) ? "unread_count_user1" : "unread_count_user2";
        String sql = "UPDATE chat_sessions SET " + fieldName + " = " + fieldName + " + 1, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sessionId);
            stmt.executeUpdate();
        }
    }
    
    public void resetUnreadCount(long sessionId, int userId) throws SQLException {
        String fieldName = (userId == getSessionUser1Id(sessionId)) ? "unread_count_user1" : "unread_count_user2";
        String sql = "UPDATE chat_sessions SET " + fieldName + " = 0, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sessionId);
            stmt.executeUpdate();
        }
    }
    
    public void deleteSession(long sessionId) throws SQLException {
        String sql = "DELETE FROM chat_sessions WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sessionId);
            stmt.executeUpdate();
        }
    }

    private int getSessionUser1Id(long sessionId) throws SQLException {
        String sql = "SELECT user1_id FROM chat_sessions WHERE id = ?";
        
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user1_id");
                }
            }
        }
        return 0;
    }
    
    private ChatSession mapResultSetToChatSession(ResultSet rs) throws SQLException {
        return new ChatSession(
            rs.getLong("id"),
            rs.getInt("user1_id"),
            rs.getInt("user2_id"),
            rs.getString("user1_username"),
            rs.getString("user2_username"),
            rs.getLong("last_message_id"),
            rs.getInt("unread_count_user1"),
            rs.getInt("unread_count_user2"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }
}