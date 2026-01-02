package com.example.chat.dao;

import com.example.chat.model.PostLike;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostLikeDao {

    public boolean addLike(PostLike like) {
        String sql = "INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, like.getPostId());
            stmt.setInt(2, like.getUserId());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    like.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            // Unique constraint violation means user already liked
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error code
                return false;
            }
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeLike(int postId, int userId) {
        String sql = "DELETE FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasLiked(int postId, int userId) {
        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countLikesByPostId(int postId) {
        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private PostLike mapResultSetToLike(ResultSet rs) throws SQLException {
        PostLike like = new PostLike();
        like.setId(rs.getInt("id"));
        like.setPostId(rs.getInt("post_id"));
        like.setUserId(rs.getInt("user_id"));
        like.setCreatedAt(rs.getTimestamp("created_at"));
        return like;
    }
}