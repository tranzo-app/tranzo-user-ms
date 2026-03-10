package com.tranzo.tranzo_user_ms.splitwise.mapper;

import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    @Test
    @DisplayName("toResponse with null user returns null")
    void toResponse_nullUser_returnsNull() {
        assertNull(UserMapper.toResponse(null));
        assertNull(UserMapper.toResponse(null, new UserProfileEntity()));
    }

    @Test
    @DisplayName("toResponse without profile uses email as name")
    void toResponse_noProfile_usesEmailAsName() {
        UsersEntity user = new UsersEntity();
        user.setUserUuid(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setMobileNumber("9876543210");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserResponse response = UserMapper.toResponse(user);

        assertNotNull(response);
        assertEquals(user.getUserUuid(), response.getUserUuid());
        assertEquals("test@example.com", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("9876543210", response.getMobileNumber());
    }

    @Test
    @DisplayName("toResponse with profile uses full name")
    void toResponse_withProfile_usesFullName() {
        UsersEntity user = new UsersEntity();
        user.setUserUuid(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("John");
        profile.setLastName("Doe");

        UserResponse response = UserMapper.toResponse(user, profile);

        assertEquals("John Doe", response.getName());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("toResponse with profile and middle name includes middle name")
    void toResponse_withMiddleName_includesInFullName() {
        UsersEntity user = new UsersEntity();
        user.setUserUuid(UUID.randomUUID());
        user.setEmail("a@b.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("John");
        profile.setMiddleName("Q");
        profile.setLastName("Doe");

        UserResponse response = UserMapper.toResponse(user, profile);

        assertEquals("John Q Doe", response.getName());
    }

    @Test
    @DisplayName("toResponse with empty profile name falls back to email")
    void toResponse_emptyProfileName_fallbackToEmail() {
        UsersEntity user = new UsersEntity();
        user.setUserUuid(UUID.randomUUID());
        user.setEmail("fallback@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("");
        profile.setLastName("");

        UserResponse response = UserMapper.toResponse(user, profile);

        assertEquals("fallback@example.com", response.getName());
    }
}
