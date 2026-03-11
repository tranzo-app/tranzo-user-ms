package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.exception.UserProfileNotFoundException;
import com.tranzo.tranzo_user_ms.user.dto.UserProfileDto;
import com.tranzo.tranzo_user_ms.user.dto.UrlDto;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileHistoryRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserReportRepository;
import com.tranzo.tranzo_user_ms.media.service.S3MediaService;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.utility.UserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserReportRepository userReportRepository;

    @Mock
    private UserProfileHistoryRepository userProfileHistoryRepository;

    @Mock
    private UserUtility userUtility;

    @Mock
    private S3MediaService s3MediaService;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UsersEntity userEntity;
    private UserProfileEntity profileEntity;
    private UserProfileDto profileDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = new UsersEntity();
        userEntity.setUserUuid(userId);
        userEntity.setEmail("test@example.com");
        userEntity.setMobileNumber("9876543210");

        profileEntity = new UserProfileEntity();
        profileEntity.setUserProfileUuid(UUID.randomUUID());
        profileEntity.setUser(userEntity);
        profileEntity.setFirstName("Test");
        profileEntity.setLastName("User");
        profileEntity.setBio("Bio");
        userEntity.setUserProfileEntity(profileEntity);
        userEntity.setSocialHandleEntity(new java.util.ArrayList<>());

        profileDto = UserProfileDto.builder()
                .firstName("Test")
                .lastName("User")
                .emailId("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void getUserProfile_Success() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.of(profileEntity));

        UserProfileDto result = userService.getUserProfile(userId);

        assertNotNull(result);
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userProfileRepository).findAllUserProfileDetailByUserId(userId);
    }

    @Test
    @DisplayName("Should throw UserProfileNotFoundException when profile not found")
    void getUserProfile_NotFound() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> userService.getUserProfile(userId));
    }

    @Test
    @DisplayName("Should find user by user id")
    void findUserByUserId_Success() {
        when(userRepository.findUserByUserUuid(userId)).thenReturn(Optional.of(userEntity));

        assertDoesNotThrow(() -> userService.findUserByUserId(userId));
        verify(userRepository).findUserByUserUuid(userId);
    }

    @Test
    @DisplayName("Should throw when user not found in findUserByUserId")
    void findUserByUserId_NotFound() {
        when(userRepository.findUserByUserUuid(userId)).thenReturn(Optional.empty());

        assertThrows(com.tranzo.tranzo_user_ms.commons.exception.EntityNotFoundException.class,
                () -> userService.findUserByUserId(userId));
    }
}
