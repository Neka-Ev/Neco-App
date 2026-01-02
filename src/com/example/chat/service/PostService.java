package com.example.chat.service;

import com.example.chat.dao.PostDao;
import com.example.chat.dao.PostCommentDao;
import com.example.chat.dao.PostLikeDao;
import com.example.chat.model.Post;
import com.example.chat.model.PostComment;
import com.example.chat.model.PostLike;

import java.sql.SQLException;
import java.util.List;

public class PostService {

    private final PostDao postDao;
    private final PostCommentDao commentDao;
    private final PostLikeDao likeDao;

    public PostService() {
        this.postDao = new PostDao();
        this.commentDao = new PostCommentDao();
        this.likeDao = new PostLikeDao();
    }

    public PostService(PostDao postDao, PostCommentDao commentDao, PostLikeDao likeDao) {
        this.postDao = postDao;
        this.commentDao = commentDao;
        this.likeDao = likeDao;
    }

    // 发帖功能
    public Post createPost(int userId, String content, String contentType, String imageUrl) throws SQLException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }
        Post post = new Post();
        post.setAuthorId(userId);
        post.setContent(content.trim());
        post.setContentType(contentType != null ? contentType.trim() : "text");
        post.setStatus("active");
        post.setImageUrl(imageUrl);
        boolean success = postDao.createPost(post);
        if (!success) {
            throw new SQLException("创建帖子失败");
        }
        return post;
    }

    // 兼容旧接口
    public Post createPost(int userId, String content, String contentType) throws SQLException {
        return createPost(userId, content, contentType, null);
    }

    // 获取帖子详情
    public Post getPost(int postId) throws SQLException {
        Post post = postDao.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        return post;
    }

    // 获取广场帖子列表（分页）
    public List<Post> getPostList(int page, int pageSize) throws SQLException {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 10;
        
        int offset = (page - 1) * pageSize;
        return postDao.findAllActivePosts(offset, pageSize);
    }
    
    // 兼容JSP中调用的getPosts方法
    public List<Post> getPosts(int offset, int limit) throws SQLException {
        return postDao.findAllActivePosts(offset, limit);
    }

    // 删除帖子（逻辑删除）
    public boolean deletePost(int postId, int userId) throws SQLException {
        Post post = postDao.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        
        // 检查权限：只有作者可以删除自己的帖子
        if (post.getAuthorId() != userId) {
            throw new SecurityException("无权限删除此帖子");
        }
        
        return postDao.updatePostStatus(postId, "deleted");
    }

    // 评论帖子
    public PostComment addComment(int postId, int userId, String userName, String content, String avatarUrl, Integer parentId) throws SQLException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        
        // 检查帖子是否存在
        Post post = postDao.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        
        // 如果是回复评论，检查父评论是否存在
        if (parentId != null) {
            PostComment parentComment = commentDao.findCommentById(parentId);
            if (parentComment == null) {
                throw new IllegalArgumentException("回复的评论不存在");
            }
        }
        
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setAuthorId(userId);
        comment.setAuthorName(userName);
        comment.setContent(content.trim());
        comment.setAuthorAvatarUrl(avatarUrl);
        comment.setParentId(parentId);

        boolean success = commentDao.createComment(comment);
        if (!success) {
            throw new SQLException("创建评论失败");
        }
        
        // 更新帖子的评论数
        postDao.incrementCommentsCount(postId);
        
        return comment;
    }

    // 获取帖子的所有评论
    public List<PostComment> getComments(int postId) throws SQLException {
        // 检查帖子是否存在
        Post post = postDao.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        
        return commentDao.findCommentsByPostId(postId);
    }

    // 点赞帖子
    public boolean likePost(int postId, int userId) throws SQLException {
        // 检查帖子是否存在
        Post post = postDao.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        
        // 检查是否已点赞
        if (likeDao.hasLiked(postId, userId)) {
            return false; // 已点赞，无需重复操作
        }
        
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        
        boolean success = likeDao.addLike(like);
        if (success) {
            // 更新帖子的点赞数
            postDao.incrementLikesCount(postId);
        }
        
        return success;
    }

    // 取消点赞
    public boolean unlikePost(int postId, int userId) throws SQLException {
        // 检查帖子是否存在
        Post post = postDao.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在或已被删除");
        }
        
        // 检查是否已点赞
        if (!likeDao.hasLiked(postId, userId)) {
            return false; // 未点赞，无需操作
        }
        
        boolean success = likeDao.removeLike(postId, userId);
        if (success) {
            // 更新帖子的点赞数
            postDao.decrementLikesCount(postId);
        }
        
        return success;
    }

    // 检查用户是否已点赞
    public boolean hasLiked(int postId, int userId) throws SQLException {
        return likeDao.hasLiked(postId, userId);
    }

    // 统计所有活动帖子的数量
    public int countActivePosts() throws SQLException {
        return postDao.countActivePosts();
    }

    // 删除评论
    public boolean deleteComment(int postId, int commentId, int userId) throws SQLException {
        PostComment comment = commentDao.findCommentById(commentId);
        if (comment == null || comment.getPostId() != postId) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (comment.getAuthorId() != userId) {
            throw new SecurityException("无权限删除此评论");
        }
        boolean deleted = commentDao.deleteComment(commentId);
        if (deleted) {
            postDao.decrementCommentsCount(postId);
        }
        return deleted;
    }
}
