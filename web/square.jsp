<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.chat.model.User" %>
<%@ page import="com.example.chat.model.Post" %>
<%@ page import="com.example.chat.service.PostService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.SQLException" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    List<Post> posts = null;
    try {
        PostService postService = new PostService();
        posts = postService.getPosts(0, 20); // 每页20条，第一页
    } catch (SQLException e) {
        out.print("<div class='error-message'>加载帖子失败：" + e.getMessage() + "</div>");
        e.printStackTrace();
    }
%>
<link rel="stylesheet" href="assets/css/common.css" />
<link rel="stylesheet" href="assets/css/chat.css" />
<link rel="stylesheet" href="assets/css/square.css" />
<div class="square-layout">
    <div class="square-container">
        <div class="square-header">
            <h3 class="content-title">技术分享广场</h3>
            <span class="square-description">分享技术见解，交流学习经验</span>
        </div>
        <form id="postForm" class="message-form" enctype="multipart/form-data">
            <div class="message-input-wrapper">
                <textarea id="postInput" name="content" class="message-input message-input-multiline" rows="2" placeholder="分享你的技术见解..." required></textarea>
            </div>
            <div class="post-form-actions">
                <label class="upload-btn">
                    <input type="file" id="postImage" name="image" accept="image/*" style="display:none;" />
                    上传图片
                </label>
                <div id="image-preview" class="image-preview" style="display:none;"></div>
                <button type="submit" class="send-button">发布</button>
            </div>
        </form>
        <div class="posts-scroll">
            <div id="posts-container" class="posts-container">
                <% if (posts != null && !posts.isEmpty()) { %>
                    <% for (Post post : posts) { %>
                    <div class="post-item" data-post-id="<%= post.getId() %>">
                        <div class="post-header">
                            <div class="post-author">
                                <span class="author-name" data-author-id="<%= post.getAuthorId() %>">用户<%= post.getAuthorId() %></span>
                                <span class="post-time"><%= post.getCreatedAt().toString().substring(0, 19).replace('T', ' ') %></span>
                                <span class="friend-status" data-author-id="<%= post.getAuthorId() %>"></span>
                            </div>
                            <% if (currentUser.getId() != post.getAuthorId()) { %>
                            <div class="friend-actions">
                                <button class="add-friend-btn" data-author-id="<%= post.getAuthorId() %>" onclick="toggleFriendship(<%= post.getAuthorId() %>, this)">添加好友</button>
                                <button class="remove-friend-btn" data-author-id="<%= post.getAuthorId() %>" onclick="toggleFriendship(<%= post.getAuthorId() %>, this)" style="display: none;">解除好友</button>
                            </div>
                            <% } else { %>
                            <div class="post-actions">
                                <button class="delete-post-btn" onclick="deletePost(<%= post.getId() %>)">删除</button>
                            </div>
                            <% } %>
                        </div>
                        <div class="post-content">
                            <%= post.getContent() %>
                        </div>
                        <div class="post-footer">
                            <div class="post-stats">
                                <span class="like-count">👍 <%= post.getLikesCount() %></span>
                                <span class="comment-count">💬 <%= post.getCommentsCount() %></span>
                            </div>
                            <div class="post-interactions">
                                <button class="like-btn" onclick="toggleLike(<%= post.getId() %>)">
                                    点赞
                            </button>
                            <button class="comment-btn" onclick="toggleComments(<%= post.getId() %>)">
                                评论
                            </button>
                            </div>
                        </div>
                        <div class="comments-section" id="comments-<%= post.getId() %>">
                            <div class="comments-list"></div>
                            <form class="comment-form" onsubmit="addComment(<%= post.getId() %>, this); return false;">
                                <textarea class="comment-input" placeholder="写下你的评论..." required></textarea>
                                <button type="submit" class="send-comment-btn">发送</button>
                            </form>
                        </div>
                    </div>
                    <% } %>
                <% } else { %>
                    <div class="empty-posts">
                        <p>暂无帖子，快来发布第一条帖子吧！</p>
                    </div>
                <% } %>
            </div>
        </div>
        <div id="square-pagination" class="square-pagination"></div>
    </div>
</div>
<script src="assets/js/extra/jquery-3.7.1.min.js"></script>
<script src="assets/js/local/square.js"></script>
<script>
    $(function() {
        let currentUserId = <%= currentUser.getId() %>;
        let currentUsername = '<%= currentUser.getUsername() %>';
        let currentDisplayName = '<%= currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getUsername() %>';
        let currentAvatarUrl = '<%= currentUser.getAvatarUrl() != null ? currentUser.getAvatarUrl() : "assets/images/avatars/default.jpg" %>';
        window.squareManager = new SquareManager(currentUserId, currentUsername, currentDisplayName, currentAvatarUrl);
        window.squareManager.init();

        $('#postImage').on('change', function() {
            const file = this.files && this.files[0];
            if (file) {
                const url = URL.createObjectURL(file);
                $('#image-preview').html('<img src="' + url + '" alt="预览" />').show();
            } else {
                $('#image-preview').hide().empty();
            }
        });
    });
</script>