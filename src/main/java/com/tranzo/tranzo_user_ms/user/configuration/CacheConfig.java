package com.tranzo.tranzo_user_ms.user.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tranzo.tranzo_user_ms.user.dto.OtpData;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {
    // TODO : What should be maximum entry size?

    @Bean
    public Cache<String, OtpData> otpCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // OTP expiry
                .maximumSize(100000) // max entries
                .build();
    }

    @Bean
    public Cache<String, Integer> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(100000)
                .build();
    }

    @Bean
    public Cache<String, String> aadhaarTokenCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(23, TimeUnit.HOURS)
                .maximumSize(1)
                .build();
    }
}
