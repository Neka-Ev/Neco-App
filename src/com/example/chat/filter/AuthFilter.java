package com.example.chat.filter;

import com.example.chat.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 统一登录状态校验过滤器
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String contextPath = req.getContextPath();

        if (isPublicPath(req)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            // 未登录：统一重定向到登录页
            resp.sendRedirect(contextPath + "/index.jsp?page=login");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    private boolean isPublicPath(HttpServletRequest req) {
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String p = path.substring(contextPath.length());

        // 静态资源
        if (p.startsWith("/assets/")) return true;
        if (p.startsWith("/WEB-INF/")) return false;

        // 错误页
        if (p.startsWith("/error.jsp")) return true;

        // 认证接口
        if (p.startsWith("/api/auth")) return true;

        if (p.startsWith("/api/")) return false;

        // index.jsp：根据 page 参数决定是否放行
        if (p.equals("/") || p.equals("/index.jsp")) {
            String page = req.getParameter("page");
            if (page == null || page.isEmpty()
                    || "login".equals(page)
                    || "register".equals(page)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
