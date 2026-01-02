package com.example.chat.servlet;

import com.example.chat.dao.UserDao;
import com.example.chat.model.User;
import com.example.chat.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/api/auth/login", "/api/auth/register", "/api/auth/logout"})
public class AuthServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        
        try {
            if (path.endsWith("/login")) {
                handleLogin(req, resp);
            } else if (path.endsWith("/register")) {
                handleRegister(req, resp);
            } else if (path.endsWith("/logout")) {
                handleLogout(req, resp);
            } else {
                resp.setStatus(404);
                resp.getWriter().print("Unknown action");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/index.jsp?page=error&error=" +
                java.net.URLEncoder.encode("服务器错误，请重试", "UTF-8"));
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        if (username == null || password == null) {
            // Redirect back to login page with error
            resp.sendRedirect(req.getContextPath() + "/index.jsp?page=login&error=" +
                java.net.URLEncoder.encode("请填写用户名和密码", "UTF-8"));
            return;
        }

        User u = userDao.findByUsername(username);
        if (u != null && PasswordUtil.verify(password, u.getPasswordHash())) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", u);
            session.setAttribute("userId", u.getId());
            session.setAttribute("username", u.getUsername());
            session.setMaxInactiveInterval(30*60);

            // 更新用户在线状态
            userDao.setUserOnlineStatus(u.getId(), true);

            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        } else {
            resp.sendRedirect(req.getContextPath() + "/index.jsp?page=login&error=" +
                java.net.URLEncoder.encode("用户名或密码错误", "UTF-8"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if (path.endsWith("/logout")) {
            handleLogout(req, resp);
        } else {
            resp.setStatus(404);
            resp.getWriter().print("Unknown action");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                userDao.setUserOnlineStatus(user.getId(), false);
            }
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/index.jsp?page=login");
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String displayName = req.getParameter("displayName");
        String email = req.getParameter("email");
        
        if (username == null || password == null || email == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp?page=register&error=" +
                java.net.URLEncoder.encode("请填写必填字段", "UTF-8"));
            return;
        }
        
        try {
            User existing = userDao.findByUsername(username);
            if (existing != null) {
                resp.sendRedirect(req.getContextPath() + "/index.jsp?page=register&error=" +
                    java.net.URLEncoder.encode("用户名已存在", "UTF-8"));
                return;
            }

            User existingEmail = userDao.findByEmail(email);
            if (existingEmail != null) {
                resp.sendRedirect(req.getContextPath() + "/index.jsp?page=register&error=" +
                    java.net.URLEncoder.encode("邮箱已被注册", "UTF-8"));
                return;
            }
            
            String hash = PasswordUtil.hash(password);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPasswordHash(hash);
            newUser.setDisplayName(displayName != null ? displayName : username);
            newUser.setEmail(email);
            
            boolean success = userDao.insert(newUser);

            if (success) {
                User createdUser = userDao.findByUsername(username);
                if (createdUser != null) {
                    System.out.println("User registered: " + createdUser.getId() + ", " + username + ", " + displayName + ", " + email);
                    HttpSession session = req.getSession(true);
                    session.setAttribute("user", createdUser);
                    session.setAttribute("userId", createdUser.getId());
                    session.setAttribute("username", username);
                    session.setMaxInactiveInterval(30*60);
                    userDao.setUserOnlineStatus(createdUser.getId(), true);

                    resp.sendRedirect(req.getContextPath() + "/index.jsp");
                } else {
                    throw new SQLException("Failed to retrieve created user");
                }
            } else {
                resp.sendRedirect(req.getContextPath() + "/index.jsp?page=register&error=" +
                    java.net.URLEncoder.encode("注册失败，请重试", "UTF-8"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/index.jsp?page=register&error=" +
                java.net.URLEncoder.encode("服务器错误，请重试", "UTF-8"));
        }
    }
}