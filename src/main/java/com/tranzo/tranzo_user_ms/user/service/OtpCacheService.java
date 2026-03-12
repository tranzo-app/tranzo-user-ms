package com.tranzo.tranzo_user_ms.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tranzo.tranzo_user_ms.user.dto.OtpData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpCacheService {

    private final Cache<String, OtpData> otpCache;

    public OtpData get(String phone) {
        return otpCache.getIfPresent(phone);
    }

    public void put(String phone, OtpData data) {
        otpCache.put(phone, data);
    }

    public void remove(String phone) {
        otpCache.invalidate(phone);
    }
}
