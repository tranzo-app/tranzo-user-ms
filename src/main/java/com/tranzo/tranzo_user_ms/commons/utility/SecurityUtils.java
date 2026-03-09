package com.tranzo.tranzo_user_ms.commons.utility;

import jakarta.security.auth.message.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getCurrentUserUuid() throws AuthException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AuthException("Unauthenticated request");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof String str) {
            return UUID.fromString(str);
        }
        if (principal instanceof UserDetails userDetails) {
            return UUID.fromString(userDetails.getUsername());
        }
        throw new AuthException("Invalid authentication principal");
    }
}

