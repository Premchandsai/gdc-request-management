package com.gdc.requests_management.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenGenerator {
    public static void main(String[] args) {
        String secret = "iimxlU5IZxO+N4VaQVoVpKzTnE63eqCRyhm9UKcZirk=";
        String userId = "456e7890-e89b-12d3-a456-426614174000";
        String role = "DRIVER"; // Change to DRIVER or ADMIN as needed
        long expirationMs = 86400000; // 24 hours

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                .compact();

        System.out.println("JWT Token: " + token);
    }
}