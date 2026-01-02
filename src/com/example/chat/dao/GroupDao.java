package com.example.chat.dao;

import com.example.chat.model.Group;
import com.example.chat.util.DbUtil;

import java.sql.*;
import java.util.List;

public class GroupDao {

    public int createGroup(Group group, List<Integer> initialMembers) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int groupId = -1;

        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 创建群组
            String sql = "INSERT INTO groups (group_name, owner_id, created_at) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, group.getGroupName());
            stmt.setInt(2, group.getOwnerId());
            stmt.setTimestamp(3, group.getCreatedAt());
            stmt.executeUpdate();

            // 获取生成的 groupId
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                groupId = rs.getInt(1);
            }
            rs.close(); // 及时关闭ResultSet

            // 添加初始成员（包括群主）
            
            // 添加群主（角色为1）
            sql = "INSERT INTO group_members (group_id, user_id, role, status) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, group.getOwnerId());
            stmt.setInt(3, 1); // 群主角色
            stmt.setInt(4, 0); // 正常状态
            stmt.executeUpdate();
            stmt.close(); // 关闭群主插入的stmt

            // 添加其他成员（角色为3）
            for (int userId : initialMembers) {
                if (userId == group.getOwnerId()) continue; // 跳过群主
                // 为每个成员创建新的PreparedStatement
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, groupId);
                stmt.setInt(2, userId);
                stmt.setInt(3, 3); // 成员角色
                stmt.setInt(4, 0); // 正常状态
                stmt.executeUpdate();
                stmt.close(); // 关闭当前成员的stmt
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }

        return groupId;
    }

    public Group getGroupById(int groupId) {
        String sql = "SELECT * FROM groups WHERE group_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Group group = new Group();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setOwnerId(rs.getInt("owner_id"));
                group.setCreatedAt(rs.getTimestamp("created_at"));
                return group;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteGroup(int groupId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            // 删除群消息
            stmt = conn.prepareStatement("DELETE FROM group_messages WHERE group_id = ?");
            stmt.setInt(1, groupId);
            stmt.executeUpdate();
            stmt.close();

            // 删除群成员
            stmt = conn.prepareStatement("DELETE FROM group_members WHERE group_id = ?");
            stmt.setInt(1, groupId);
            stmt.executeUpdate();
            stmt.close();

            // 删除群本身
            stmt = conn.prepareStatement("DELETE FROM groups WHERE group_id = ?");
            stmt.setInt(1, groupId);
            int affected = stmt.executeUpdate();

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return false;
    }
}