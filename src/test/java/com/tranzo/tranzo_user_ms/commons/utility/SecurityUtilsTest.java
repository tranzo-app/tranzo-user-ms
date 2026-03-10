package com.tranzo.tranzo_user_ms.commons.utility;

import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityUtils Unit Tests")
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return UUID when principal is UUID")
    void getCurrentUserUuid_principalIsUuid_returnsUuid() throws AuthException {
        UUID expected = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(expected, null));

        UUID result = SecurityUtils.getCurrentUserUuid();

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should return UUID when principal is String")
    void getCurrentUserUuid_principalIsString_returnsParsedUuid() throws AuthException {
        String uuidStr = "11111111-1111-4111-8111-111111111111";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(uuidStr, null));

        UUID result = SecurityUtils.getCurrentUserUuid();

        assertEquals(UUID.fromString(uuidStr), result);
    }

    @Test
    @DisplayName("Should return UUID when principal is UserDetails")
    void getCurrentUserUuid_principalIsUserDetails_returnsUuidFromUsername() throws AuthException {
        String uuidStr = "22222222-2222-4222-8222-222222222222";
        UserDetails user = User.builder().username(uuidStr).password("").authorities("USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));

        UUID result = SecurityUtils.getCurrentUserUuid();

        assertEquals(UUID.fromString(uuidStr), result);
    }

    @Test
    @DisplayName("Should throw AuthException when authentication is null")
    void getCurrentUserUuid_nullAuthentication_throwsAuthException() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthException.class, SecurityUtils::getCurrentUserUuid);
    }

    @Test
    @DisplayName("Should throw AuthException when principal is null")
    void getCurrentUserUuid_nullPrincipal_throwsAuthException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(null, null));

        assertThrows(AuthException.class, SecurityUtils::getCurrentUserUuid);
    }

    @Test
    @DisplayName("Should throw AuthException when principal type is invalid")
    void getCurrentUserUuid_invalidPrincipalType_throwsAuthException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(12345, null));

        AuthException ex = assertThrows(AuthException.class, SecurityUtils::getCurrentUserUuid);
        assertTrue(ex.getMessage().contains("Invalid authentication principal"));
    }
}
