package com.example.chat.dao;

import com.example.chat.model.Post;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDao {

    public boolean createPost(Post post) {
        String sql = "INSERT INTO posts (author_id, content, content_type, status, likes_count, comments_count, created_at, updated_at, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, post.getAuthorId());
            stmt.setString(2, post.getContent());
            stmt.setString(3, post.getContentType());
            stmt.setString(4, post.getStatus());
            stmt.setInt(5, 0);
            stmt.setInt(6, 0);
            stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            stmt.setString(9, post.getImageUrl());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    post.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Post findById(int postId) {
        String sql = "SELECT p.*, u.username, u.avatar_url FROM posts p JOIN users u ON p.author_id = u.id WHERE p.id = ? AND p.status = 'active'";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPost(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Post> findAllActivePosts(int offset, int limit) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username, u.avatar_url FROM posts p JOIN users u ON p.author_id = u.id WHERE p.status = 'active' ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                posts.add(mapResultSetToPost(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean updatePostStatus(int postId, String status) {
        String sql = "UPDATE posts SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean incrementLikesCount(int postId) {
        String sql = "UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean decrementLikesCount(int postId) {
        String sql = "UPDATE posts SET likes_count = GREATEST(0, likes_count - 1) WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean incrementCommentsCount(int postId) {
        String sql = "UPDATE posts SET comments_count = comments_count + 1 WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean decrementCommentsCount(int postId) {
        String sql = "UPDATE posts SET comments_count = GREATEST(0, comments_count - 1) WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countActivePosts() {
        String sql = "SELECT COUNT(*) FROM posts WHERE status = 'active'";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setAuthorId(rs.getInt("author_id"));
        
        // 设置用户名（如果查询中包含users表）
        try {
            post.setAuthorName(rs.getString("username"));
        } catch (SQLException e) {
            // 如果没有JOIN users表，忽略此异常
        }
        
        // 设置作者头像URL
        try {
            post.setAuthorAvatarUrl(rs.getString("avatar_url"));
        } catch (SQLException e) {
            // ignore if column missing
        }

        post.setContent(rs.getString("content"));
        post.setContentType(rs.getString("content_type"));
        post.setStatus(rs.getString("status"));
        post.setLikesCount(rs.getInt("likes_count"));
        post.setCommentsCount(rs.getInt("comments_count"));
        post.setCreatedAt(rs.getTimestamp("created_at"));
        post.setUpdatedAt(rs.getTimestamp("updated_at"));
        post.setImageUrl(rs.getString("image_url"));
        return post;
    }
}