package com.library.librarysystem.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtils(
            @Value("${jwt.secret:library-system-jwt-secret-key-2026-very-long-string}") String secret,
            @Value("${jwt.access-token-expiration:7200000}") long accessExp,     // 2h default
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshExp  // 7d default
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessExp;
        this.refreshTokenExpiration = refreshExp;
    }

    public String generateAccessToken(Long userId, String username, List<String> roles) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
}
