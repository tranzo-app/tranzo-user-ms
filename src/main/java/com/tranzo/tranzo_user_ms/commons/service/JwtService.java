package com.tranzo.tranzo_user_ms.commons.service;

import com.tranzo.tranzo_user_ms.user.model.UsersEntity;

import java.util.UUID;

public interface JwtService {
    String generateAccessToken(UsersEntity user);
    String generateRefreshToken(UsersEntity user);
    String generateRegistrationToken(String identifier);
    boolean validateToken(String token);
    void validateTokenOrThrow(String token);
    String extractSubject(String token);
    String extractTokenType(String token);
}
