package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.model.UsersEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.access-token-expiry-minutes}")
    private long accessExpiryMinutes;

    @Value("${spring.jwt.refresh-token-expiry-days}")
    private long refreshExpiryDays;

    @Value("${spring.jwt.issuer}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(UsersEntity user) {
        return Jwts.builder()
                .setSubject(user.getUserUuid().toString())
                .setIssuer(issuer)
                .claim("role", user.getUserRole().name())
                .claim("type", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(
                                Instant.now().plus(accessExpiryMinutes, ChronoUnit.MINUTES)
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(UsersEntity user) {
        return Jwts.builder()
                .setSubject(user.getUserUuid().toString())
                .setIssuer(issuer)
                .claim("type", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(
                                Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS)
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    @Override
    public void validateAccessToken(String token) throws AuthException {
        if (validateToken(token)) {
            throw new AuthException("Invalid JWT token");
        }
        String tokenType = extractTokenType(token);
        if (!"ACCESS".equals(tokenType)) {
            throw new AuthException("Provided token is not an access token");
        }
        Claims claims = parseClaims(token);
        if (claims.getExpiration().before(new Date())) {
            throw new AuthException("Access token expired");
        }
    }

    @Override
    public void validateRefreshToken(String token) throws AuthException {
        if (validateToken(token)) {
            throw new AuthException("Invalid JWT token");
        }
        String tokenType = extractTokenType(token);
        if (!"REFRESH".equals(tokenType)) {
            throw new AuthException("Provided token is not a refresh token");
        }
        Claims claims = parseClaims(token);
        if (claims.getExpiration().before(new Date())) {
            throw new AuthException("Refresh token expired");
        }
    }


    @Override
    public UUID extractUserUuid(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    @Override
    public String extractTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}