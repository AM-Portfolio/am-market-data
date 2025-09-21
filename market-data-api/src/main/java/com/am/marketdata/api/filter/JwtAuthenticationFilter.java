package com.am.marketdata.api.filter;

import com.myportfolio.jwtlogin.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        if (uri.contains("/health") || uri.contains("/actuator") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(httpResponse, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtil.verifyToken(token);
            httpRequest.setAttribute("userId", claims.get("userId"));
            httpRequest.setAttribute("username", claims.getSubject());
            httpRequest.setAttribute("roles", claims.get("roles"));
            httpRequest.setAttribute("claims", claims);
            chain.doFilter(request, response);
        } catch (Exception ex) {
            logger.warn("JWT validation failed: {}", ex.getMessage());
            unauthorized(httpResponse, "Invalid or expired token");
        }
    }

    private void unauthorized(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}
