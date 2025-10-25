package com.tranzo.tranzo_user_ms.service;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

public class TokenService {
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    public TokenService(StringRedisTemplate redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    public IssuedTokens issuedTokens(long userId){
        String access = jwtService.generateAccessToken(userId, ACCESS_TTL.toMillis());
        String refreshId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(refreshKey(refreshId), String.valueOf(userId),REFRESH_TTL);
        return new IssuedTokens(access, refreshId);
    }

    public IssuedTokens rotate(String oldRefreshId){
        String key = refreshKey(oldRefreshId);
        String userId = redisTemplate.opsForValue().get(key);
        if(userId == null) throw new IllegalStateException("refresh token Expired or invalid");
        redisTemplate.delete(key);

        String newAcess = jwtService.generateAccessToken(Long.valueOf(userId), ACCESS_TTL.toMillis());
        String newRefresh = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(refreshKey(newRefresh),userId,REFRESH_TTL);

        return new IssuedTokens(newAcess, newRefresh);
    }

    public void revoke(String refreshId){
        if(refreshId!=null) redisTemplate.delete(refreshKey(refreshId));
    }

    private String refreshKey(String refreshId) {return "refresh:" + refreshId;}
    public record IssuedTokens(String accessToken, String refreshToken) {}
}
