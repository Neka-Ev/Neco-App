package com.example.chat.servlet;

import com.example.chat.dao.UserDao;
import com.example.chat.model.User;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/users/online")
public class UsersOnlineServlet extends HttpServlet {
    
    private UserDao userDao;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDao();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置响应类型为JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 获取所有在线用户
            List<User> onlineUsers = userDao.findAllOnlineUsers();
            
            // 转换为前端需要的格式
            List<UserStatus> userStatusList = onlineUsers.stream()
                    .map(user -> new UserStatus(user.getId(), user.getUsername(), true))
                    .collect(Collectors.toList());
            
            // 返回JSON响应
            String json = gson.toJson(userStatusList);
            response.getWriter().write(json);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"服务器错误\"}");
            e.printStackTrace();
        }
    }
    
    // 内部类用于JSON序列化
    private static class UserStatus {
        private int id;
        private String username;
        private boolean online;
        
        public UserStatus(int id, String username, boolean online) {
            this.id = id;
            this.username = username;
            this.online = online;
        }
        
        // Getter方法用于JSON序列化
        public int getId() { return id; }
        public String getUsername() { return username; }
        public boolean isOnline() { return online; }
    }
}