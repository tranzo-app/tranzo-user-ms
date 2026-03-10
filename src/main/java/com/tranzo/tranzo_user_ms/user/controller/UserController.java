package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.media.dto.UploadResponseDto;
import com.tranzo.tranzo_user_ms.media.service.S3MediaService;
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
    private final S3MediaService s3MediaService;

    public UserController(UserService userService, JwtService jwtService, S3MediaService s3MediaService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.s3MediaService = s3MediaService;
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseDto<UserProfileDto>> getUser() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Fetching user profile for userId: {}", userId);
        UserProfileDto userProfileDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile fetched successfully", userProfileDto));
    }

    /**
     * Register user. Multipart: part "profile" (JSON), optional part "file" (profile picture).
     * If file is present, it is uploaded to S3 and the key is stored as profile picture.
     */
    @PostMapping(value = "/user/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> registerUser(
            HttpServletRequest request,
            @RequestPart("profile") @Valid UserProfileDto userProfileDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws AuthException, IOException {
        log.info("Register with file present: {}", file != null && !file.isEmpty());
        String identifier = (String) request.getAttribute("registrationIdentifier");
        UUID userId = userService.createUserProfile(userProfileDto, identifier);
        if (file != null && !file.isEmpty()) {
            UploadResponseDto uploadResult = s3MediaService.upload(file, userId.toString());
            userService.updateProfilePicture(userId, new UrlDto(uploadResult.getKey()));
        }
        UserProfileDto createdProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200, "User profile created successfully", createdProfile));
    }

    /**
     * Update user profile. Multipart: part "profile" (JSON), optional part "file" (new profile picture).
     * If file is present, it is uploaded to S3 and the key is stored as profile picture.
     * When only file is sent (no profile fields), only the picture is updated.
     */
    @PatchMapping(value = "/user/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> updateUserProfile(
            @RequestPart("profile") @Valid UserProfileDto userProfileDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws AuthException, IOException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        if (file != null && !file.isEmpty()) {
            UploadResponseDto uploadResult = s3MediaService.upload(file, userId.toString());
            userService.updateProfilePicture(userId, new UrlDto(uploadResult.getKey()));
        }
        if (!userService.isEmptyUpdateRequest(userProfileDto)) {
            userService.updateUserProfile(userId, userProfileDto);
        }
        UserProfileDto updatedProfile = userService.getUserProfile(userId);
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