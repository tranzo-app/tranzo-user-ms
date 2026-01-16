package com.tranzo.tranzo_user_ms.commons.utility;

import jakarta.security.auth.message.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getCurrentUserUuid() throws AuthException {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("Unauthenticated request");
        }
        return (UUID) authentication.getPrincipal();
    }
}

