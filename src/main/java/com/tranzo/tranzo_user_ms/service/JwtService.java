package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.model.UsersEntity;

import java.util.UUID;

public interface JwtService {
    String generateAccessToken(UsersEntity user);
    String generateRefreshToken(UsersEntity user);
    boolean validateAccessToken(String token);
    boolean validateRefreshToken(String token);
    UUID extractUserUuid(String token);
    String extractTokenType(String token);
}
