package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.model.UsersEntity;
import jakarta.security.auth.message.AuthException;

import java.util.UUID;

public interface JwtService {
    String generateAccessToken(UsersEntity user);
    String generateRefreshToken(UsersEntity user);
    boolean validateToken(String token);
    void validateAccessToken(String token) throws AuthException;
    void validateRefreshToken(String token) throws AuthException;
    UUID extractUserUuid(String token);
    String extractTokenType(String token);
}
