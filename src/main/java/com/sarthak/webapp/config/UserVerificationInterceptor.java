package com.sarthak.webapp.config;

import com.sarthak.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserVerificationInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public UserVerificationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Skip verification for these endpoints
        if (request.getRequestURI().contains("/v1/verifyEmail") ||
                request.getRequestURI().contains("/v1/user") && request.getMethod().equals("POST") ||
                request.getRequestURI().contains("/healthz")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            if (!userService.isUserVerified(auth.getName())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        return true;
    }
}