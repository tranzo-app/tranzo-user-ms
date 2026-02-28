package com.tranzo.tranzo_user_ms.commons.service;

import com.tranzo.tranzo_user_ms.commons.exception.UnauthorizedException;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

    @Value("${spring.jwt.registration-token-expiry-minutes}")
    private long registrationExpiryMinutes;

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
                .setId(UUID.randomUUID().toString())
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
    public String generateRegistrationToken(String identifier)
    {
        return Jwts.builder()
                .setSubject(identifier)
                .setIssuer(issuer)
                .claim("type", "REGISTRATION")
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(
                                Instant.now().plus(registrationExpiryMinutes, ChronoUnit.MINUTES)
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            return "ACCESS".equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            return "REFRESH".equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void validateTokenOrThrow(String token) {
        try {
            parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid or expired token");
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

    @Override
    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }
}