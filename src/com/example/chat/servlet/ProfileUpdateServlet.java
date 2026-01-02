package com.example.chat.servlet;

import com.example.chat.dao.UserDao;
import com.example.chat.model.User;
import com.example.chat.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@WebServlet("/api/profile/update")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class ProfileUpdateServlet extends HttpServlet {

    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        super.init();
        userDao = new UserDao();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.writeJsonError(response, "未登录");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        // 获取表单数据
        String displayName = request.getParameter("displayName");
        String bio = request.getParameter("bio");

        // 更新基本信息
        if (displayName != null && !displayName.trim().isEmpty()) {
            currentUser.setDisplayName(displayName.trim());
        }
        if (bio != null) {
            currentUser.setBio(bio.trim());
        }

        // 处理头像上传
        Part filePart = request.getPart("avatar");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String fileExt = fileName.substring(fileName.lastIndexOf("."));

            // 简单的文件类型检查
            if (!fileExt.matches("(?i)\\.(jpg|jpeg|png|gif|webp)")) {
                JsonUtil.writeJsonError(response, "不支持的文件类型");
                return;
            }

            // 使用用户名作为文件名，或者加时间戳防止缓存
            String newFileName = currentUser.getUsername() + "_" + System.currentTimeMillis() + fileExt;

            // 获取上传目录的绝对路径
            String uploadPath = getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "avatars";

            // 打印路径以便调试
            System.out.println("头像上传保存路径: " + uploadPath);


            // 保存文件
            filePart.write(uploadPath + File.separator + newFileName);

            // 更新用户头像URL
            currentUser.setAvatarUrl("assets/images/avatars/" + newFileName);
        }

        // 修改密码
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        // 如果提交了修改密码请求，先校验旧密码
        if ((oldPassword != null && !oldPassword.isEmpty()) || (newPassword != null && !newPassword.isEmpty())) {
            if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
                JsonUtil.writeJsonError(response, "请输入原密码和新密码");
                return;
            }
            try {
                User dbUser = userDao.findById(currentUser.getId());
                if (dbUser == null || !com.example.chat.util.PasswordUtil.verify(oldPassword, dbUser.getPasswordHash())) {
                    JsonUtil.writeJsonError(response, "原密码不正确");
                    return;
                }
                String newHash = com.example.chat.util.PasswordUtil.hash(newPassword);
                dbUser.setPasswordHash(newHash);
                currentUser.setPasswordHash(newHash);
            } catch (Exception ex) {
                JsonUtil.writeJsonError(response, "修改密码失败");
                return;
            }
        }

        // 保存到数据库
        if (userDao.update(currentUser)) {
            // 更新Session中的用户信息
            session.setAttribute("user", currentUser);
            JsonUtil.writeJsonSuccess(response, "个人资料更新成功");
        } else {
            JsonUtil.writeJsonError(response, "更新失败，请重试");
        }
    }
}
