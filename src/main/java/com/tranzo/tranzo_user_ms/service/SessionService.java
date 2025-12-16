package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.dto.SessionRequestDto;
import com.tranzo.tranzo_user_ms.dto.SessionResponseDto;
import com.tranzo.tranzo_user_ms.model.RefreshTokenEntity;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import com.tranzo.tranzo_user_ms.repository.RefreshTokenRepository;
import com.tranzo.tranzo_user_ms.repository.UserRepository;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.jwt.access-token-expiry-minutes}")
    private long accessExpiryMinutes;

    @Value("${spring.jwt.refresh-token-expiry-days}")
    private long refreshExpiryDays;

    @Transactional
    public SessionResponseDto createSession(
            SessionRequestDto request,
            HttpServletResponse response
    ) {
        UsersEntity user = findUser(request);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveNewRefreshToken(refreshToken, user);
        setCookie(response, "ACCESS_TOKEN", accessToken, (int) (accessExpiryMinutes * 60));
        setCookie(response, "REFRESH_TOKEN", refreshToken, (int) (refreshExpiryDays * 24 * 60 * 60));

        return SessionResponseDto.builder()
                .authenticated(true)
                .build();
    }

    @Transactional
    public SessionResponseDto refreshSession(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthException {

        String refreshToken = extractRefreshToken(request);
        UUID userUuid = jwtService.extractUserUuid(refreshToken);
        RefreshTokenEntity storedToken =
                refreshTokenRepository
                        .findByUser_UserUuidAndRevokedFalse(userUuid)
                        .orElseThrow(() -> new AuthException("Session expired"));
        if (!passwordEncoder.matches(refreshToken, storedToken.getTokenHash())) {
            throw new AuthException("Invalid refresh token");
        }
        jwtService.validateRefreshToken(refreshToken);
        UsersEntity user = storedToken.getUser();
        String newRefreshToken = jwtService.generateRefreshToken(user);
        storedToken.setTokenHash(passwordEncoder.encode(newRefreshToken));
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(refreshExpiryDays));
//        storedToken.setUpdatedAt(LocalDateTime.now());
        refreshTokenRepository.save(storedToken);
        String newAccessToken = jwtService.generateAccessToken(user);
        setCookie(response, "ACCESS_TOKEN", newAccessToken, (int) (accessExpiryMinutes * 60));
        setCookie(response, "REFRESH_TOKEN", newRefreshToken, (int) (refreshExpiryDays * 24 * 60 * 60));
        return SessionResponseDto.builder()
                .authenticated(true)
                .build();
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken != null) {
            try {
                UUID userUuid = jwtService.extractUserUuid(refreshToken);
                refreshTokenRepository
                        .findByUser_UserUuidAndRevokedFalse(userUuid)
                        .ifPresent(token -> {
                            token.setRevoked(true);
//                            token.setUpdatedAt(LocalDateTime.now());
                            refreshTokenRepository.save(token);
                        });

            } catch (Exception e) {
                // DO NOT fail logout if token is bad/expired
                // Logout must always succeed
            }
        }
        clearCookie(response, "ACCESS_TOKEN");
        clearCookie(response, "REFRESH_TOKEN");
    }


    private UsersEntity findUser(SessionRequestDto dto) {

        return dto.getEmailId() != null
                ? userRepository.findByEmail(dto.getEmailId().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"))
                : userRepository.findByMobileNumber(dto.getMobileNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void setCookie(HttpServletResponse response,
                           String name,
                           String value,
                           int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);   // true in prod
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void saveNewRefreshToken(String token, UsersEntity user) {
        refreshTokenRepository.save(
                RefreshTokenEntity.builder()
                        .tokenHash(passwordEncoder.encode(token))
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .revoked(false)
                        .build()
        );
    }

    private String extractRefreshToken(HttpServletRequest request) {
        return Arrays.stream(Optional.ofNullable(request.getCookies())
                        .orElseThrow(() -> new RuntimeException("No cookies")))
                .filter(c -> "REFRESH_TOKEN".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Refresh token missing"));
    }
}

