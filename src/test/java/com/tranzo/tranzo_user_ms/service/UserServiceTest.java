package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.dto.UrlDto;
import com.tranzo.tranzo_user_ms.dto.UserProfileDto;
import com.tranzo.tranzo_user_ms.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.enums.Gender;
import com.tranzo.tranzo_user_ms.exception.*;
import com.tranzo.tranzo_user_ms.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import com.tranzo.tranzo_user_ms.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.repository.UserRepository;
import com.tranzo.tranzo_user_ms.exception.UserAlreadyDeletedExeption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    private UUID userUuid;
    private UsersEntity userEntity;
    private UserProfileEntity profileEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userUuid = UUID.randomUUID();

        userEntity = new UsersEntity();
        userEntity.setUserUuid(userUuid);
        userEntity.setEmail("test@gmail.com");
        userEntity.setMobileNumber("9876543210");
        userEntity.setAccountStatus(AccountStatus.ACTIVE);

        profileEntity = new UserProfileEntity();
        profileEntity.setUserProfileUuid(UUID.randomUUID());
        profileEntity.setUser(userEntity);
        profileEntity.setFirstName("Komal");
        profileEntity.setLastName("Sharma");
        profileEntity.setGender(Gender.FEMALE);
        profileEntity.setDob(LocalDate.of(1997, 1, 1));
        profileEntity.setLocation("Mumbai");
    }

    // -------------------------------------------------------------------------
    // 1) GET USER PROFILE
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET — should return profile successfully")
    void getUserProfile_success() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.of(profileEntity));

        UserProfileDto result = userService.getUserProfile(userUuid.toString());

        assertEquals("Komal", result.getFirstName());
        verify(userProfileRepository).findAllUserProfileDetailByUserId(userUuid);
    }

    @Test
    @DisplayName("GET — should fail when UUID format is invalid")
    void getUserProfile_invalidUuid() {
        assertThrows(InvalidUserIdException.class,
                () -> userService.getUserProfile("bad-uuid"));
    }

    @Test
    @DisplayName("GET — should fail when user profile not found")
    void getUserProfile_notFound() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> userService.getUserProfile(userUuid.toString()));
    }

    // -------------------------------------------------------------------------
    // 2) UPDATE USER PROFILE
    // -------------------------------------------------------------------------

    private UserProfileDto validUpdateDto() {
        return UserProfileDto.builder()
                .firstName("Komal Updated")
                .mobileNumber("9876543210")
                .location("Delhi")
                .build();
    }

    @Test
    @DisplayName("PATCH — should update user profile successfully")
    void updateUserProfile_success() {
        UserProfileDto updateDto = validUpdateDto();

        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.of(profileEntity));

        UserProfileDto result = userService.updateUserProfile(userUuid.toString(), updateDto);

        assertEquals("Komal Updated", result.getFirstName());
        assertEquals("Delhi", result.getLocation());
    }

    @Test
    @DisplayName("PATCH — should fail when invalid UUID")
    void updateUserProfile_invalidUuid() {
        UserProfileDto dto = validUpdateDto();

        assertThrows(InvalidUserIdException.class,
                () -> userService.updateUserProfile("bad-uuid", dto));
    }

    @Test
    @DisplayName("PATCH — should fail when no fields provided")
    void updateUserProfile_emptyRequest() {
        UserProfileDto emptyDto = new UserProfileDto(); // all null

        assertThrows(InvalidPatchRequestException.class,
                () -> userService.updateUserProfile(userUuid.toString(), emptyDto));
    }

    @Test
    @DisplayName("PATCH — should fail when profile not found")
    void updateUserProfile_notFound() {
        UserProfileDto updateDto = validUpdateDto();

        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> userService.updateUserProfile(userUuid.toString(), updateDto));
    }

    // -------------------------------------------------------------------------
    // 3) DELETE USER (SOFT DELETE)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE — should soft delete successfully")
    void deleteUserProfile_success() {
        when(userRepository.findUserByUserUuid(userUuid))
                .thenReturn(Optional.of(userEntity));

        userService.deleteUserProfile(userUuid.toString());

        assertEquals(AccountStatus.DELETED, userEntity.getAccountStatus());
    }

    @Test
    @DisplayName("DELETE — should fail when uuid invalid")
    void deleteUserProfile_invalidUuid() {
        assertThrows(InvalidUserIdException.class,
                () -> userService.deleteUserProfile("bad-uuid"));
    }

    @Test
    @DisplayName("DELETE — should fail when user not found")
    void deleteUserProfile_notFound() {
        when(userRepository.findUserByUserUuid(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> userService.deleteUserProfile(userUuid.toString()));
    }

    @Test
    @DisplayName("DELETE — should fail when already deleted")
    void deleteUserProfile_alreadyDeleted() {
        userEntity.setAccountStatus(AccountStatus.DELETED);

        when(userRepository.findUserByUserUuid(userUuid))
                .thenReturn(Optional.of(userEntity));

        assertThrows(UserAlreadyDeletedExeption.class,
                () -> userService.deleteUserProfile(userUuid.toString()));
    }

    // -------------------------------------------------------------------------
    // 4) UPDATE PROFILE PICTURE
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT profile-picture — success")
    void updateProfilePicture_success() {
        UrlDto dto = UrlDto.builder().url("https://cdn.com/img123.png").build();
        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.of(profileEntity));

        UserProfileDto result = userService.updateProfilePicture(userUuid.toString(), dto);

        assertEquals("https://cdn.com/img123.png", profileEntity.getProfilePictureUrl());
    }

    @Test
    @DisplayName("PUT profile-picture — invalid uuid")
    void updateProfilePicture_invalidUuid() {
        UrlDto dto = UrlDto.builder().url("https://img.png").build();
        assertThrows(InvalidUserIdException.class,
                () -> userService.updateProfilePicture("bad-uuid", dto));
    }

    @Test
    @DisplayName("PUT profile-picture — profile not found")
    void updateProfilePicture_notFound() {
        UrlDto dto = UrlDto.builder().url("https://img.png").build();

        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> userService.updateProfilePicture(userUuid.toString(), dto));
    }

    // -------------------------------------------------------------------------
    // 5) DELETE PROFILE PICTURE
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE profile-picture — success")
    void deleteProfilePicture_success() {
        profileEntity.setProfilePictureUrl("https://some-url.com/pic.png");

        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.of(profileEntity));

        UserProfileDto result = userService.deleteProfilePicture(userUuid.toString());

        assertNull(profileEntity.getProfilePictureUrl());
    }

    @Test
    @DisplayName("DELETE profile-picture — invalid UUID")
    void deleteProfilePicture_invalidUuid() {
        assertThrows(InvalidUserIdException.class,
                () -> userService.deleteProfilePicture("bad-uuid"));
    }

    @Test
    @DisplayName("DELETE profile-picture — profile not found")
    void deleteProfilePicture_notFound() {
        when(userProfileRepository.findAllUserProfileDetailByUserId(userUuid))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class,
                () -> userService.deleteProfilePicture(userUuid.toString()));
    }
}