package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.*;
import com.tranzo.tranzo_user_ms.user.service.UserService;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Get current user's profile. Profile picture URL in the response is resolved to a presigned URL
     * when stored in S3 (keys starting with "uploads/"); otherwise the stored URL is returned as-is.
     */
    @GetMapping("/user")
    public ResponseEntity<ResponseDto<UserProfileDto>> getUser() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Fetching user profile for userId: {}", userId);
        UserProfileDto userProfileDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile fetched successfully", userProfileDto));
    }

    /**
     * Register user (JSON only, no file). Use when profile picture is omitted or provided via profilePictureUrl in the body.
     * Auth: Bearer registration token from OTP verify.
     */
    @PostMapping(value = "/user/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> registerUserWithoutFile(
            HttpServletRequest request,
            @RequestBody @Valid UserProfileDto userProfileDto) throws AuthException, IOException {
        String identifier = (String) request.getAttribute("registrationIdentifier");
        UUID userId = userService.createUserProfile(userProfileDto, identifier, null);
        UserProfileDto createdProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200, "User profile created successfully", createdProfile));
    }

    /**
     * Register user. Multipart: part "profile" (JSON), optional part "file" (profile picture).
     * Profile picture is set from file (S3) or from DTO URL in the service.
     */
    @PostMapping(value = "/user/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> registerUser(
            HttpServletRequest request,
            @RequestPart("profile") @Valid UserProfileDto userProfileDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws AuthException, IOException {
        String identifier = (String) request.getAttribute("registrationIdentifier");
        UUID userId = userService.createUserProfile(userProfileDto, identifier, file);
        UserProfileDto createdProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200, "User profile created successfully", createdProfile));
    }

    /**
     * Update user profile. Multipart: part "profile" (JSON), optional part "file" (new profile picture).
     * Picture and/or profile fields are updated in the service.
     */
    @PatchMapping(value = "/user/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> updateUserProfile(
            @RequestPart("profile") @Valid UserProfileDto userProfileDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws AuthException, IOException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.updateUserProfile(userId, userProfileDto, file);
        return ResponseEntity.ok(ResponseDto.success(200, "User profile updated successfully", updatedProfile));
    }

    @DeleteMapping("/user/delete-user")
    public ResponseEntity<ResponseDto<Void>> deleteUser() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        userService.deleteUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile deleted successfully", null));
    }

    @PutMapping("/user/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> updateProfilePicture(@RequestBody @Valid UrlDto profilePictureUrl) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.updateProfilePicture(userId, profilePictureUrl);
        return ResponseEntity.ok(ResponseDto.success(200,"Profile picture updated  successfully", updatedProfile));
    }

    @DeleteMapping("/user/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> deleteProfilePicture() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.deleteProfilePicture(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"Profile picture deleted successfully", updatedProfile));
    }

    @PatchMapping("/user/social-handles")
    public ResponseEntity<ResponseDto<UserProfileDto>> upsertSocialHandles(@RequestBody @Valid List<SocialHandleDto> socialHandles) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.upsertSocialHandles(userId, socialHandles);
        return ResponseEntity.ok(
                ResponseDto.success(200, "Social handles updated successfully", updatedProfile)
        );
    }

    @PostMapping("/user/{reportedUserId}/report")
    public ResponseEntity<ResponseDto<Void>> reportUser(@PathVariable String reportedUserId, @RequestBody @Valid UserReportRequestDto userReportRequestDto) throws AuthException {
        UUID reporterUserId = SecurityUtils.getCurrentUserUuid();
        userService.reportUser(reportedUserId,reporterUserId, userReportRequestDto);
        return ResponseEntity.ok(
                ResponseDto.success(200, "User reported successfully", null)
        );
    }
}

// history table and version in userProfileTable
// populate history table in case of modification and deletion
// test
// Documentation
//