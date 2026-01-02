package com.example.chat.dao;

import com.example.chat.model.PostComment;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostCommentDao {

    public boolean createComment(PostComment comment) {
        String sql = "INSERT INTO post_comments (post_id, author_id, content, parent_id, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, comment.getPostId());
            stmt.setInt(2, comment.getAuthorId());
            stmt.setString(3, comment.getContent());
            if (comment.getParentId() != null) {
                stmt.setInt(4, comment.getParentId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PostComment> findCommentsByPostId(int postId) {
        List<PostComment> comments = new ArrayList<>();
        String sql = "SELECT pc.*, u.username, u.avatar_url FROM post_comments pc JOIN users u ON pc.author_id = u.id WHERE pc.post_id = ? ORDER BY pc.created_at";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public PostComment findCommentById(int commentId) {
        String sql = "SELECT pc.*, u.username, u.avatar_url FROM post_comments pc JOIN users u ON pc.author_id = u.id WHERE pc.id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToComment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteComment(int commentId) {
        String sql = "DELETE FROM post_comments WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PostComment mapResultSetToComment(ResultSet rs) throws SQLException {
        PostComment comment = new PostComment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setAuthorId(rs.getInt("author_id"));
        comment.setAuthorName(rs.getString("username"));
        comment.setContent(rs.getString("content"));
        
        int parentId = rs.getInt("parent_id");
        if (!rs.wasNull()) {
            comment.setParentId(parentId);
        } else {
            comment.setParentId(null);
        }
        
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            comment.setAuthorAvatarUrl(rs.getString("avatar_url"));
        } catch (SQLException e) {
            // ignore if column missing
        }
        return comment;
    }
}