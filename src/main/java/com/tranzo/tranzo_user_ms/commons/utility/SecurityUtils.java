package com.tranzo.tranzo_user_ms.commons.utility;

import jakarta.security.auth.message.AuthException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UUID> getCurrentUserUuidOptional() {
        try {
            return Optional.of(getCurrentUserUuid());
        } catch (AuthException ex) {
            return Optional.empty();
        }
    }

    public static UUID getCurrentUserUuid() throws AuthException {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthException("Unauthenticated request");
        }

        Object principal = authentication.getPrincipal();

        if (principal == null
                || "anonymousUser".equals(principal)) {
            throw new AuthException("Unauthenticated request");
        }

        if (principal instanceof UUID uuid) {
            return uuid;
        }

        if (principal instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException ex) {
                throw new AuthException("Invalid authentication principal", ex);
            }
        }

        if (principal instanceof UserDetails userDetails) {
            try {
                return UUID.fromString(userDetails.getUsername());
            } catch (IllegalArgumentException ex) {
                throw new AuthException("Invalid authentication principal", ex);
            }
        }

        throw new AuthException("Invalid authentication principal");
    }
}

