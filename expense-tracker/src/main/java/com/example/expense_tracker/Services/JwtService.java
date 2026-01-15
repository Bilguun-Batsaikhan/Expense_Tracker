package com.example.expense_tracker.Services;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
// import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.expense_tracker.entities.Role;
import com.example.expense_tracker.entities.User;
// import com.example.expense_tracker.enums.ErrorCode;
// import com.example.expense_tracker.exceptions.ApiException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private static final String SECRET_KEY = "my-super-secret-key-my-super-secret-key"; // min 256-bit for HS256

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {

        List<String> roles = user.getRoles().stream()
                .map(Role::getName) // ROLE_USER, ROLE_ADMIN
                .toList();

        return Jwts.builder()
                .setSubject(user.getEmail()) // unique identifier
                .claim("userId", user.getId().toString()).claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // public UUID extractUserId(String token) {
    // Claims claims = extractAllClaims(token);
    // String userId = claims.get("userId", String.class);

    // if (userId == null) {
    // throw new ApiException(ErrorCode.INVALID_ACCESS_TOKEN);
    // }

    // return UUID.fromString(userId);
    // }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        return claims.get("roles", List.class);
    }

}
