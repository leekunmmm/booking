package com.luxehaven.booking.config;

import com.luxehaven.booking.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute(SESSION_USER);

        boolean requiresLogin = path.startsWith("/admin")
                || path.startsWith("/bookings")
                || path.equals("/my-bookings");

        boolean requiresAdmin = path.startsWith("/admin");

        if (requiresLogin && user == null) {
            response.sendRedirect("/login?redirect=" + path);
            return false;
        }
        if (requiresAdmin && (user == null || !user.isAdmin())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập");
            return false;
        }
        return true;
    }
}
