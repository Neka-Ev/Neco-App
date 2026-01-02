package com.example.chat.dao;

import com.example.chat.model.Group;
import com.example.chat.model.GroupMember;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GroupMemberDao {

    public List<GroupMember> getMembers(int groupId) {
        List<GroupMember> members = new ArrayList<>();
        String sql = "SELECT * FROM group_members WHERE group_id = ? AND status = 0";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapResultSetToGroupMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public List<Integer> getMemberIds(int groupId) {
        List<Integer> memberIds = new ArrayList<>();
        String sql = "SELECT user_id FROM group_members WHERE group_id = ? AND status = 0";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                memberIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberIds;
    }

    public boolean isMember(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ? AND user_id = ? AND status = 0";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
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

    public boolean addMember(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id, role, status) VALUES (?, ?, 3, 0)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeMember(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Group> getUserGroups(int userId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g JOIN group_members gm ON g.group_id = gm.group_id WHERE gm.user_id = ? AND gm.status = 0";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Group group = new Group();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setOwnerId(rs.getInt("owner_id"));
                group.setCreatedAt(rs.getTimestamp("created_at"));
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public int getMemberRole(int groupId, int userId) {
        String sql = "SELECT role FROM group_members WHERE group_id = ? AND user_id = ? AND status = 0";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 0 means not a member or error
    }

    public List<GroupMember> searchMembers(int groupId, String query) {
        List<GroupMember> members = new ArrayList<>();
        String sql = "SELECT gm.*, u.username, u.display_name FROM group_members gm " +
                     "JOIN users u ON gm.user_id = u.id " +
                     "WHERE gm.group_id = ? AND gm.status = 0 " +
                     "AND (u.username LIKE ? OR u.display_name LIKE ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapResultSetToGroupMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // Helper to get member details with user info
    public List<Map<String, Object>> searchMembersWithUserInfo(int groupId, String query) {
        List<Map<String, Object>> members = new ArrayList<>();
        String sql = "SELECT gm.*, u.username, u.display_name, u.avatar_url FROM group_members gm " +
                     "JOIN users u ON gm.user_id = u.id " +
                     "WHERE gm.group_id = ? AND gm.status = 0 " +
                     "AND (u.username LIKE ? OR u.display_name LIKE ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> member = new HashMap<>();
                member.put("id", rs.getInt("id"));
                member.put("userId", rs.getInt("user_id"));
                member.put("role", rs.getInt("role"));
                member.put("username", rs.getString("username"));
                member.put("displayName", rs.getString("display_name"));
                member.put("avatarUrl", rs.getString("avatar_url"));
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    private GroupMember mapResultSetToGroupMember(ResultSet rs) throws SQLException {
        GroupMember member = new GroupMember();
        member.setId(rs.getInt("id"));
        member.setGroupId(rs.getInt("group_id"));
        member.setUserId(rs.getInt("user_id"));
        member.setRole(rs.getInt("role"));
        member.setStatus(rs.getInt("status"));
        return member;
    }
}