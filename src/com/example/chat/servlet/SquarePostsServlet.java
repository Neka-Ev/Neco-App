package com.example.chat.servlet;

import com.example.chat.dao.PostDao;
import com.example.chat.dao.PostCommentDao;
import com.example.chat.dao.PostLikeDao;
import com.example.chat.model.Post;
import com.example.chat.model.PostComment;
import com.example.chat.model.User;
import com.example.chat.service.PostService;
import com.example.chat.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/square/posts", "/square/posts/*"})
@javax.servlet.annotation.MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)
public class SquarePostsServlet extends HttpServlet {

    private PostService postService;

    @Override
    public void init() throws ServletException {
        super.init();
        PostDao postDao = new PostDao();
        PostCommentDao commentDao = new PostCommentDao();
        PostLikeDao likeDao = new PostLikeDao();
        postService = new PostService(postDao, commentDao, likeDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonError(response, "用户未登录");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /square/posts - 获取广场帖子列表
            handleGetPostsList(request, response);
        } else if (pathInfo.matches("/\\d+/comments")) {
            // GET /square/posts/{id}/comments - 获取帖子评论列表
            handleGetCommentsList(request, response, pathInfo);
        } else {
            // GET /square/posts/{id} - 获取帖子详情
            handleGetPostDetails(request, response, pathInfo);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonError(response, "用户未登录");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/\\d+/comments/\\d+")) {
            // DELETE /square/posts/{postId}/comments/{commentId}
            handleDeleteComment(request, response, pathInfo);
            return;
        }

        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            // DELETE /square/posts/{id} - 删除帖子
            try {
                int postId = Integer.parseInt(pathInfo.substring(1));
                User currentUser = (User) session.getAttribute("user");
                boolean success = postService.deletePost(postId, currentUser.getId());
                
                if (success) {
                    JsonUtil.writeJsonSuccess(response, "删除帖子成功");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JsonUtil.writeJsonError(response, "删除帖子失败");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "无效的帖子ID");
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "删除帖子失败");
            } catch (SecurityException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.writeJsonError(response, e.getMessage());
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJsonError(response, e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的请求路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonError(response, "用户未登录");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // POST /square/posts - 发帖 (支持图片上传)
            handleCreatePost(request, response);
        } else if (pathInfo.matches("/\\d+/comment")) {
            // POST /square/posts/{id}/comment - 评论
            handleCreateComment(request, response, pathInfo);
        } else if (pathInfo.matches("/\\d+/like")) {
            // POST /square/posts/{id}/like - 点赞
            handleLikePost(request, response, pathInfo);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的请求路径");
        }
    }

    private void handleGetPostsList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 解析分页参数
            int page = 1;
            int pageSize = 5; // default 5

            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try { page = Integer.parseInt(pageParam); }
                catch (NumberFormatException ignored) {}
            }

            String pageSizeParam = request.getParameter("pageSize");
            if (pageSizeParam != null && !pageSizeParam.isEmpty()) {
                try { pageSize = Integer.parseInt(pageSizeParam); }
                catch (NumberFormatException ignored) {}
            }

            // 获取当前登录用户
            HttpSession session = request.getSession(false);
            User currentUser = (User) session.getAttribute("user");
            int currentUserId = currentUser.getId();

            // 获取帖子列表
            List<Post> posts = postService.getPostList(page, pageSize);
            int totalCount = postService.countActivePosts();

            // 为每个帖子添加作者是否为好友的状态
            List<Map<String, Object>> postsWithFriendInfo = new ArrayList<>();
            for (Post post : posts) {
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("id", post.getId());
                postMap.put("authorId", post.getAuthorId());
                postMap.put("authorUsername", post.getAuthorName());
                postMap.put("authorAvatarUrl", post.getAuthorAvatarUrl());
                postMap.put("content", post.getContent());
                postMap.put("contentType", post.getContentType());
                postMap.put("likesCount", post.getLikesCount());
                postMap.put("commentsCount", post.getCommentsCount());
                postMap.put("createdAt", post.getCreatedAt());
                postMap.put("status", post.getStatus());
                postMap.put("imageUrl", post.getImageUrl());

                boolean isAuthorFriend = false;
                if (currentUserId != post.getAuthorId()) {
                    try {
                        isAuthorFriend = com.example.chat.servlet.FriendServlet.isFriend(currentUserId, post.getAuthorId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                postMap.put("isAuthorFriend", isAuthorFriend);

                postsWithFriendInfo.add(postMap);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("posts", postsWithFriendInfo);
            responseData.put("page", page);
            responseData.put("pageSize", pageSize);
            responseData.put("total", totalCount);

            JsonUtil.writeJsonResponse(response, responseData);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "获取帖子列表失败");
        }
    }

    private void handleGetPostDetails(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            // 解析帖子ID
            int postId = Integer.parseInt(pathInfo.substring(1));
            
            // 获取帖子详情
            Post post = postService.getPost(postId);
            
            // 获取帖子评论
            List<PostComment> comments = postService.getComments(postId);
            
            // 获取当前用户
            User currentUser = (User) request.getSession(false).getAttribute("user");
            
            // 检查当前用户是否已点赞
            boolean isLiked = postService.hasLiked(postId, currentUser.getId());
            
            // 检查作者是否为好友
            boolean isAuthorFriend = false;
            if (currentUser.getId() != post.getAuthorId()) { // 排除自己
                try {
                    isAuthorFriend = com.example.chat.servlet.FriendServlet.isFriend(currentUser.getId(), post.getAuthorId());
                } catch (Exception e) {
                    e.printStackTrace();
                    // 出错时默认不是好友
                }
            }
            
            // 构造响应数据
            Map<String, Object> responseData = new HashMap<>();
            
            // 为帖子添加好友关系状态
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("authorId", post.getAuthorId());
            postMap.put("authorUsername", post.getAuthorName());
            postMap.put("content", post.getContent());
            postMap.put("contentType", post.getContentType());
            postMap.put("likesCount", post.getLikesCount());
            postMap.put("commentsCount", post.getCommentsCount());
            postMap.put("createdAt", post.getCreatedAt());
            postMap.put("status", post.getStatus());
            postMap.put("isAuthorFriend", isAuthorFriend);
            postMap.put("imageUrl", post.getImageUrl());

            responseData.put("post", postMap);
            responseData.put("comments", comments);
            responseData.put("isLiked", isLiked);
            
            JsonUtil.writeJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的帖子ID");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "获取帖子详情失败");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonUtil.writeJsonError(response, e.getMessage());
        }
    }
    
    private void handleGetCommentsList(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            // 解析帖子ID
            int postId = Integer.parseInt(pathInfo.split("/")[1]);
            
            // 获取帖子评论列表
            List<PostComment> comments = postService.getComments(postId);
            
            // 返回评论列表
            JsonUtil.writeJsonResponse(response, comments);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的帖子ID");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "获取评论列表失败");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonUtil.writeJsonError(response, e.getMessage());
        }
    }

    private void handleCreatePost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            User currentUser = (User) request.getSession(false).getAttribute("user");

            boolean isMultipart = request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/");
            String content = null;
            String contentType = "text";
            String imageUrl = null;

            if (isMultipart) {
                request.setCharacterEncoding("UTF-8");
                Collection<Part> parts = request.getParts();
                for (Part part : parts) {
                    if ("content".equals(part.getName())) {
                        content = new String(part.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                    } else if ("contentType".equals(part.getName())) {
                        contentType = new String(part.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                    } else if ("image".equals(part.getName()) && part.getSize() > 0) {
                        String fileName = System.currentTimeMillis() + "_" + part.getSubmittedFileName();
                        File uploadDir = new File(request.getServletContext().getRealPath("/assets/images/postImages"));
                        if (!uploadDir.exists()) uploadDir.mkdirs();
                        File dest = new File(uploadDir, fileName);
                        part.write(dest.getAbsolutePath());
                        imageUrl = "assets/images/postImages/" + fileName;
                    }
                }
            } else {
                String requestBody = JsonUtil.readRequestBody(request);
                Map<String, Object> requestData = JsonUtil.fromJson(requestBody, Map.class);
                content = (String) requestData.get("content");
                if (requestData.containsKey("contentType")) {
                    contentType = (String) requestData.get("contentType");
                }
                if (requestData.containsKey("imageUrl")) {
                    imageUrl = (String) requestData.get("imageUrl");
                }
            }

            if (content == null || content.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "帖子内容不能为空");
                return;
            }

            Post post = postService.createPost(currentUser.getId(), content, contentType, imageUrl);

            response.setStatus(HttpServletResponse.SC_CREATED);
            JsonUtil.writeJsonResponse(response, post);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "创建帖子失败");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, e.getMessage());
        }
    }

    private void handleCreateComment(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            User currentUser = (User) request.getSession(false).getAttribute("user");
            
            // 解析帖子ID
            int postId = Integer.parseInt(pathInfo.split("/")[1]);
            
            // 解析请求体
            String requestBody = JsonUtil.readRequestBody(request);
            Map<String, Object> requestData = JsonUtil.fromJson(requestBody, Map.class);
            
            String content = (String) requestData.get("content");
            Integer parentId = null;
            if (requestData.containsKey("parentId")) {
                Object parentIdObj = requestData.get("parentId");
                if (parentIdObj != null) {
                    parentId = Integer.parseInt(parentIdObj.toString());
                }
            }
            
            // 创建评论
            PostComment comment = postService.addComment(postId, currentUser.getId(), currentUser.getUsername(), content, currentUser.getAvatarUrl(), parentId);

            // 获取更新后的帖子信息（包含更新后的评论数）
            Post updatedPost = postService.getPost(postId);
            
            // 构造响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("comment", comment);
            responseData.put("postCommentsCount", updatedPost.getCommentsCount());
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            JsonUtil.writeJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的ID参数");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "创建评论失败");
        } catch (IllegalArgumentException | SecurityException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, e.getMessage());
        }
    }

    private void handleLikePost(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            User currentUser = (User) request.getSession(false).getAttribute("user");
            
            // 解析帖子ID
            int postId = Integer.parseInt(pathInfo.split("/")[1]);
            
            // 点赞/取消点赞
            boolean isLiked = postService.hasLiked(postId, currentUser.getId());
            boolean success;
            
            if (isLiked) {
                // 取消点赞
                success = postService.unlikePost(postId, currentUser.getId());
            } else {
                // 点赞
                success = postService.likePost(postId, currentUser.getId());
            }
            
            if (success) {
                // 更新后的点赞状态
                boolean newLikedStatus = !isLiked;
                
                // 获取更新后的点赞数
                Post post = postService.getPost(postId);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("isLiked", newLikedStatus);
                responseData.put("likesCount", post.getLikesCount());
                
                JsonUtil.writeJsonResponse(response, responseData);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "操作失败");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的帖子ID");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "点赞操作失败");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonUtil.writeJsonError(response, e.getMessage());
        }
    }

    private void handleDeleteComment(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        try {
            User currentUser = (User) request.getSession(false).getAttribute("user");
            String[] parts = pathInfo.split("/");
            int postId = Integer.parseInt(parts[1]);
            int commentId = Integer.parseInt(parts[3]);

            boolean success = postService.deleteComment(postId, commentId, currentUser.getId());
            if (success) {
                Post post = postService.getPost(postId);
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("postCommentsCount", post.getCommentsCount());
                JsonUtil.writeJsonResponse(response, resp);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "删除评论失败");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的ID参数");
        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJsonError(response, e.getMessage());
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonUtil.writeJsonError(response, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "删除评论失败");
        }
    }
}

