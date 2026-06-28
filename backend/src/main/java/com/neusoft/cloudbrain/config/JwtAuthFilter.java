package com.neusoft.cloudbrain.config;

import com.neusoft.cloudbrain.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATHS = List.of(
            "/doc.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",
            "/",
            "/api/auth/**",
            "/api/patient/register",
            "/api/patient/login",
            "/api/patient/login/phone",
            "/api/doctor/list",
            "/api/doctor/detail",
            "/api/doctor/login",
            "/api/doctor/login/phone",
            "/api/doctor/register",
            "/api/doctor/status",
            "/api/doctor/pending",
            "/api/doctor/all-with-status",
            "/api/doctor/approve",
            "/api/doctor/reject",
            "/api/triage/consult",
            "/api/registration/create",
            "/api/registration/list",
            "/api/registration/cancel",
            "/api/medical-record/generate",
            "/api/medical-record/save",
            "/api/medical-record/list",
            "/api/medical-record/detail",
            "/api/prescription/create",
            "/api/prescription/check/**",
            "/api/prescription/list",
            "/api/prescription/detail",
            "/api/prescription/detail/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        for (String pattern : EXCLUDE_PATHS) {
            if (PATH_MATCHER.match(pattern, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            setUnauthorizedResponse(response, "未登录，请先登录");
            return;
        }

        try {
            if (jwtUtils.isTokenExpired(token)) {
                setUnauthorizedResponse(response, "登录已过期，请重新登录");
                return;
            }

            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            setUnauthorizedResponse(response, "无效的令牌");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
