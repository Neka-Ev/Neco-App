package com.example.chat.model;

public class GroupMember {
    private int id;
    private int groupId;
    private int userId;
    private int role; // 1-群主, 2-管理员, 3-成员
    private int status; // 0-正常, 1-已退出/被踢

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}