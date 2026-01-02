package com.example.chat.servlet;

import com.example.chat.dao.GroupDao;
import com.example.chat.dao.GroupMemberDao;
import com.example.chat.dao.GroupMessageDao;
import com.example.chat.model.Group;
import com.example.chat.model.GroupMessage;
import com.example.chat.model.User;
import com.example.chat.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/groups/*"})
public class GroupServlet extends HttpServlet {

    private GroupDao groupDao = new GroupDao();
    private GroupMemberDao groupMemberDao = new GroupMemberDao();
    private GroupMessageDao groupMessageDao = new GroupMessageDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonError(response, "用户未登录");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/my")) {
            // GET /api/groups/my - 获取用户加入的所有群组列表
            handleGetMyGroups(request, response);
        } else if (pathInfo != null && pathInfo.equals("/messages")) {
            // GET /api/groups/messages - 获取群历史消息
            handleGetGroupMessages(request, response);
        } else if (pathInfo != null && pathInfo.equals("/messages/search")) {
            // GET /api/groups/messages/search - 搜索群消息
            handleSearchMessages(request, response);
        } else if (pathInfo != null && pathInfo.equals("/members")) {
            // GET /api/groups/members - 获取/搜索群成员
            handleGetMembers(request, response);
        } else if (pathInfo != null && pathInfo.equals("/role")) {
            // GET /api/groups/role - 获取当前用户在群的角色
            handleGetMyRole(request, response);
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
        if (pathInfo != null && pathInfo.equals("/create")) {
            // POST /api/groups/create - 创建群聊
            handleCreateGroup(request, response);
        } else if (pathInfo != null && pathInfo.equals("/member")) {
            // POST /api/groups/member - 邀请/踢人
            handleGroupMemberAction(request, response);
        } else if (pathInfo != null && pathInfo.equals("/messages/delete")) {
            // POST /api/groups/messages/delete - 删除消息
            handleDeleteMessage(request, response);
        } else if (pathInfo != null && pathInfo.equals("/members/kick")) {
            // POST /api/groups/members/kick - 踢出成员
            handleKickMember(request, response);
        } else if (pathInfo != null && pathInfo.equals("/delete")) {
            // POST /api/groups/delete - 删除群聊
            handleDeleteGroup(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的请求路径");
        }
    }

    /**
     * 创建群聊
     */
    private void handleCreateGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");

        try {
            // 读取请求体
            String json = JsonUtil.readRequestBody(request);
            System.out.println("群聊创建请求体: " + json);
            Map<String, Object> requestData = JsonUtil.fromJson(json, Map.class);

            // 验证参数
            if (requestData.get("groupName") == null || requestData.get("memberIds") == null) {
                System.out.println("缺少必要参数: groupName或memberIds");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "缺少必要参数");
                return;
            }

            String groupName = (String) requestData.get("groupName");
            // 检查群聊名称是否为空字符串
            if (groupName == null || groupName.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "群聊名称不能为空");
                return;
            }
            groupName = groupName.trim();

            // 处理 memberIds 的类型转换
            List<Integer> memberIds = new ArrayList<>();
            Object memberIdsObj = requestData.get("memberIds");
            if (!(memberIdsObj instanceof List)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "memberIds 参数类型错误");
                return;
            }
            List<?> tempList = (List<?>) memberIdsObj;
            if (tempList.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "请至少选择一位成员");
                return;
            }
            for (Object obj : tempList) {
                Integer userId = null;
                if (obj instanceof Number) {
                    userId = ((Number) obj).intValue();
                } else if (obj instanceof String) {
                    try {
                        userId = Integer.parseInt((String) obj);
                    } catch (NumberFormatException e) {
                        // 字符串不能转换为整数，跳过该元素
                    }
                }
                if (userId != null) {
                    memberIds.add(userId);
                }
            }
            if (memberIds.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "memberIds 中没有有效的用户ID");
                return;
            }

            // 创建群组对象
            Group group = new Group();
            group.setGroupName(groupName);
            group.setOwnerId(currentUser.getId());
            group.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // 创建群组
            int groupId = groupDao.createGroup(group, memberIds);

            if (groupId > 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("groupId", groupId);
                result.put("groupName", groupName);
                result.put("ownerId", currentUser.getId());
                JsonUtil.writeJsonResponse(response, result);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "创建群组失败");
            }
        } catch (Exception e) {
            // 捕获所有异常，包括JSON解析异常和类型转换异常
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "创建群组失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户加入的所有群组列表
     */
    private void handleGetMyGroups(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");

        List<Group> groups = groupMemberDao.getUserGroups(currentUser.getId());
        JsonUtil.writeJsonResponse(response, groups);
    }

    /**
     * 邀请/踢人群成员
     */
    private void handleGroupMemberAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");

        // 读取请求体
        String json = JsonUtil.readRequestBody(request);
        Map<String, Object> requestData = JsonUtil.fromJson(json, Map.class);

        // 验证参数
        if (requestData.get("groupId") == null || requestData.get("userId") == null || requestData.get("action") == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "缺少必要参数");
            return;
        }

        int groupId = ((Number) requestData.get("groupId")).intValue();
        int userId = ((Number) requestData.get("userId")).intValue();
        String action = (String) requestData.get("action");

        // 检查当前用户是否为群成员
        if (!groupMemberDao.isMember(groupId, currentUser.getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJsonError(response, "您不是该群成员");
            return;
        }

        boolean success = false;
        if ("invite".equals(action)) {
            // 邀请成员
            success = groupMemberDao.addMember(groupId, userId);
        } else if ("kick".equals(action)) {
            // 踢除成员
            success = groupMemberDao.removeMember(groupId, userId);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "无效的操作类型");
            return;
        }

        if (success) {
            JsonUtil.writeJsonSuccess(response, "操作成功");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "操作失败");
        }
    }

    /**
     * 获取群历史消息
     */
    private void handleGetGroupMessages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");

        // 获取参数
        String groupIdParam = request.getParameter("groupId");
        String offsetParam = request.getParameter("offset");
        String limitParam = request.getParameter("limit");

        if (groupIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.writeJsonError(response, "缺少群ID参数");
            return;
        }

        int groupId = Integer.parseInt(groupIdParam);
        int offset = offsetParam != null ? Integer.parseInt(offsetParam) : 0;
        int limit = limitParam != null ? Integer.parseInt(limitParam) : 20;

        // 检查当前用户是否为群成员
        if (!groupMemberDao.isMember(groupId, currentUser.getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.writeJsonError(response, "您不是该群成员");
            return;
        }

        List<GroupMessage> messages = groupMessageDao.getHistory(groupId, offset, limit);
        JsonUtil.writeJsonResponse(response, messages);
    }

    private void handleSearchMessages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int groupId = Integer.parseInt(request.getParameter("groupId"));
            String content = request.getParameter("content");
            String senderIdStr = request.getParameter("senderId");
            Integer senderId = (senderIdStr != null && !senderIdStr.isEmpty()) ? Integer.parseInt(senderIdStr) : null;
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String isCodeStr = request.getParameter("isCode");
            Boolean isCode = (isCodeStr != null && !isCodeStr.isEmpty()) ? Boolean.parseBoolean(isCodeStr) : null;

            int page = 1;
            int pageSize = 20;
            if (request.getParameter("page") != null) page = Integer.parseInt(request.getParameter("page"));
            if (request.getParameter("pageSize") != null) pageSize = Integer.parseInt(request.getParameter("pageSize"));
            int offset = (page - 1) * pageSize;

            List<GroupMessage> messages = groupMessageDao.searchMessages(groupId, content, senderId, startDate, endDate, isCode, offset, pageSize);
            int total = groupMessageDao.countMessages(groupId, content, senderId, startDate, endDate, isCode);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", messages);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);

            JsonUtil.writeJsonResponse(response, result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "搜索消息失败: " + e.getMessage());
        }
    }

    private void handleDeleteMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        try {
            String json = JsonUtil.readRequestBody(request);
            Map<String, Object> data = JsonUtil.fromJson(json, Map.class);

            Object msgIdsObj = data.get("messageIds");
            Object groupIdObj = data.get("groupId");
            if (!(msgIdsObj instanceof List) || groupIdObj == null) {
                JsonUtil.writeJsonError(response, "参数缺失");
                return;
            }

            List<?> rawIds = (List<?>) msgIdsObj;
            List<Long> messageIds = new ArrayList<>();
            for (Object o : rawIds) {
                if (o instanceof Number) {
                    messageIds.add(((Number) o).longValue());
                } else if (o instanceof String) {
                    try {
                        messageIds.add(Long.parseLong((String) o));
                    } catch (NumberFormatException ignore) {
                        // skip invalid id
                    }
                }
            }

            int groupId;
            if (groupIdObj instanceof Number) {
                groupId = ((Number) groupIdObj).intValue();
            } else if (groupIdObj instanceof String) {
                try {
                    groupId = Integer.parseInt((String) groupIdObj);
                } catch (NumberFormatException e) {
                    JsonUtil.writeJsonError(response, "群ID格式错误");
                    return;
                }
            } else {
                JsonUtil.writeJsonError(response, "群ID格式错误");
                return;
            }

            if (messageIds.isEmpty()) {
                JsonUtil.writeJsonError(response, "未提供有效的消息ID");
                return;
            }

            int role = groupMemberDao.getMemberRole(groupId, currentUser.getId());

            // Role 1: Owner, 2: Admin
            if (role != 1 && role != 2) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.writeJsonError(response, "无权删除消息");
                return;
            }

            int deletedCount = 0;
            for (Long msgId : messageIds) {
                if (groupMessageDao.deleteMessage(msgId)) {
                    deletedCount++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("deletedCount", deletedCount);
            JsonUtil.writeJsonResponse(response, result);

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeJsonError(response, "删除消息失败");
        }
    }

    private void handleGetMembers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int groupId = Integer.parseInt(request.getParameter("groupId"));
            String query = request.getParameter("query");
            if (query == null) query = "";

            List<Map<String, Object>> members = groupMemberDao.searchMembersWithUserInfo(groupId, query);
            JsonUtil.writeJsonResponse(response, members);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeJsonError(response, "获取成员列表失败");
        }
    }

    private void handleKickMember(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        try {
            String json = JsonUtil.readRequestBody(request);
            Map<String, Object> data = JsonUtil.fromJson(json, Map.class);

            Double groupIdDouble = (Double) data.get("groupId");
            Double targetUserIdDouble = (Double) data.get("userId");

            if (groupIdDouble == null || targetUserIdDouble == null) {
                JsonUtil.writeJsonError(response, "参数缺失");
                return;
            }

            int groupId = groupIdDouble.intValue();
            int targetUserId = targetUserIdDouble.intValue();

            // 简化权限检查：直接查询群组所有者
            Group group = groupDao.getGroupById(groupId);
            if (group == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJsonError(response, "群组不存在");
                return;
            }

            // 只有群主可以踢人
            if (group.getOwnerId() != currentUser.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.writeJsonError(response, "权限不足");
                return;
            }

            // 不能踢自己
            if (currentUser.getId() == targetUserId) {
                JsonUtil.writeJsonError(response, "不能踢出自己");
                return;
            }

            if (groupMemberDao.removeMember(groupId, targetUserId)) {
                JsonUtil.writeJsonSuccess(response, "成员已踢出");
            } else {
                JsonUtil.writeJsonError(response, "操作失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeJsonError(response, "踢出成员失败");
        }
    }

    private void handleGetMyRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        try {
            int groupId = Integer.parseInt(request.getParameter("groupId"));
            // 简化角色获取：如果是群主返回1，否则返回3
            Group group = groupDao.getGroupById(groupId);
            int role = 3;
            if (group != null && group.getOwnerId() == currentUser.getId()) {
                role = 1;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("role", role);
            JsonUtil.writeJsonResponse(response, result);
        } catch (Exception e) {
            JsonUtil.writeJsonError(response, "获取角色失败");
        }
    }

    private void handleDeleteGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = (User) request.getSession(false).getAttribute("user");
        try {
            String json = JsonUtil.readRequestBody(request);
            Map<String, Object> data = JsonUtil.fromJson(json, Map.class);
            Double groupIdDouble = (Double) data.get("groupId");
            if (groupIdDouble == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.writeJsonError(response, "缺少群ID");
                return;
            }
            int groupId = groupIdDouble.intValue();
            Group group = groupDao.getGroupById(groupId);
            if (group == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.writeJsonError(response, "群聊不存在");
                return;
            }
            if (group.getOwnerId() != currentUser.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.writeJsonError(response, "只有群主可以删除群聊");
                return;
            }

            boolean deleted = groupDao.deleteGroup(groupId);
            if (deleted) {
                JsonUtil.writeJsonSuccess(response, "群聊已删除");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonUtil.writeJsonError(response, "删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.writeJsonError(response, "删除失败: " + e.getMessage());
        }
    }
}
