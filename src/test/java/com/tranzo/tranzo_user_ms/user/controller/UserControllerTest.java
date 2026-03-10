package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.dto.*;
import com.tranzo.tranzo_user_ms.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserController controller;

    private UUID userId;
    private UserProfileDto profileDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileDto = UserProfileDto.builder()
                .firstName("Test")
                .lastName("User")
                .emailId("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should get user profile")
    void getUser_Success() throws Exception {
        when(userService.getUserProfile(userId)).thenReturn(profileDto);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<UserProfileDto>> res = controller.getUser();

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody().getData());
            assertEquals("Test", res.getBody().getData().getFirstName());
        }
    }

    @Test
    @DisplayName("Should register user")
    void registerUser_Success() throws Exception {
        when(request.getAttribute("registrationIdentifier")).thenReturn("email:u@test.com");
        when(userService.createUserProfile(any(UserProfileDto.class), eq("email:u@test.com"), any())).thenReturn(userId);
        when(userService.getUserProfile(userId)).thenReturn(profileDto);

        ResponseEntity<ResponseDto<UserProfileDto>> res = controller.registerUser(request, profileDto, null);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertNotNull(res.getBody().getData());
        verify(userService).createUserProfile(any(UserProfileDto.class), eq("email:u@test.com"), any());
        verify(userService).getUserProfile(userId);
    }

    @Test
    @DisplayName("Should update user profile")
    void updateUserProfile_Success() throws Exception {
        when(userService.updateUserProfile(eq(userId), any(UserProfileDto.class), any())).thenReturn(profileDto);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<UserProfileDto>> res = controller.updateUserProfile(profileDto, null);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody().getData());
            verify(userService).updateUserProfile(eq(userId), any(UserProfileDto.class), any());
        }
    }

    @Test
    @DisplayName("Should update user profile with new picture")
    void updateUserProfile_WithFile_Success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(userService.updateUserProfile(eq(userId), any(UserProfileDto.class), eq(file))).thenReturn(profileDto);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<UserProfileDto>> res = controller.updateUserProfile(profileDto, file);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            verify(userService).updateUserProfile(eq(userId), any(UserProfileDto.class), eq(file));
        }
    }

    @Test
    @DisplayName("Should delete user")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUserProfile(userId);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> res = controller.deleteUser();

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should update profile picture")
    void updateProfilePicture_Success() throws Exception {
        UrlDto urlDto = new UrlDto("https://cdn.com/pic.png");
        when(userService.updateProfilePicture(userId, urlDto)).thenReturn(profileDto);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<UserProfileDto>> res = controller.updateProfilePicture(urlDto);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should delete profile picture")
    void deleteProfilePicture_Success() throws Exception {
        when(userService.deleteProfilePicture(userId)).thenReturn(profileDto);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<UserProfileDto>> res = controller.deleteProfilePicture();

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should upsert social handles")
    void upsertSocialHandles_Success() throws Exception {
        when(userService.upsertSocialHandles(userId, List.of())).thenReturn(profileDto);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<UserProfileDto>> res = controller.upsertSocialHandles(List.of());

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should report user")
    void reportUser_Success() throws Exception {
        UUID reportedId = UUID.randomUUID();
        UserReportRequestDto reportDto = new UserReportRequestDto();
        doNothing().when(userService).reportUser(eq(reportedId.toString()), eq(userId), any(UserReportRequestDto.class));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<ResponseDto<Void>> res = controller.reportUser(reportedId.toString(), reportDto);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }
}
