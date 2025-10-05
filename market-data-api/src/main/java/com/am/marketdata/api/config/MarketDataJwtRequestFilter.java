package com.am.marketdata.api.config;

import com.modernportfolio.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Custom JWT filter for market-data application that validates JWT tokens
 * without requiring user management (UserDao/JwtService).
 * This filter only validates tokens issued by the login-management-system.
 */
@Component
public class MarketDataJwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataJwtRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.info("Illegal Argument while fetching the username: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.info("Given jwt token is expired: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.info("Some changes have been made to token - Invalid Token: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Error validating JWT token", e);
            }
        } else {
            logger.debug("JWT token does not start with Bearer");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Create a simple UserDetails object without querying the database
                // The token validation is sufficient for authentication
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("") // No password needed for token validation
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                        .build();

                // Validate the token
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.debug("Successfully authenticated user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Error during token validation for user: {}", username, e);
            }
        }

        filterChain.doFilter(request, response);
    }
}

