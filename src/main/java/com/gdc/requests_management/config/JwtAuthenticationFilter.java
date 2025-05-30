package com.gdc.requests_management.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());
    private final String secret = "iimxlU5IZxO+N4VaQVoVpKzTnE63eqCRyhm9UKcZirk=";

    // Define public endpoints that should skip JWT processing
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null) {
            header = request.getHeader("authorization"); // Case-insensitive
        }
        logger.info("Authorization header: " + (header != null ? header.substring(0, Math.min(header.length(), 20)) + "..." : "null"));
        logger.info("Request URL: " + request.getRequestURI());

        if (header == null || !header.startsWith("Bearer ")) {
            logger.info("No Bearer token found, proceeding without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");
        logger.info("Processing JWT token");

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userIdStr = claims.getSubject();
            String role = claims.get("role", String.class);

            // Convert userId to UUID if it's a valid UUID string
            UUID userId = null;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid UUID format for userId: " + userIdStr);
                // For backward compatibility, keep the original string
            }

            logger.info("UserID: " + userIdStr + ", Role: " + role);

            if (userIdStr != null && role != null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userIdStr, // Keep as string for AuthenticationPrincipal
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authentication set for user: " + userIdStr + " with role: ROLE_" + role);
            } else {
                logger.warning("UserID or role is null in JWT claims");
            }
        } catch (Exception e) {
            logger.severe("JWT validation failed: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}