package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.exception.BadRequestException;
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
@CrossOrigin
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
        log.info("API call started | endpoint=GET:/user");

        try {
            UUID userId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=getUser | userId={}", userId);

            UserProfileDto userProfileDto = userService.getUserProfile(userId);

            log.info("API call completed | endpoint=GET:/user | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200,"User profile fetched successfully", userProfileDto));
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=GET:/user | reason={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=GET:/user | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Public profile for another user (no email / phone). Authenticated callers only.
     */
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<ResponseDto<PublicUserProfileDto>> getPublicUserProfile(@PathVariable String userId) {
        log.info("API call started | endpoint=GET:/user/{}/public | targetUserId={}", userId, userId);

        try {
            UUID id;
            try {
                id = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid request | endpoint=GET:/user/{}/public | reason=INVALID_UUID", userId);
                throw new BadRequestException("Invalid user id");
            }

            log.info("Processing request | operation=getPublicUserProfile | targetUserId={}", id);
            PublicUserProfileDto dto = userService.getPublicUserProfile(id);

            log.info("API call completed | endpoint=GET:/user/{}/public | targetUserId={} | status=SUCCESS", userId, id);
            return ResponseEntity.ok(ResponseDto.success(200, "Public profile fetched successfully", dto));
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=GET:/user/{}/public | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Register user (JSON only, no file). Use when profile picture is omitted or provided via profilePictureUrl in the body.
     * Auth: Bearer registration token from OTP verify.
     */
    @PostMapping(value = "/user/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> registerUserWithoutFile(
            HttpServletRequest request,
            @RequestBody @Valid UserProfileDto userProfileDto) throws IOException {
        log.info("API call started | endpoint=POST:/user/register | type=JSON");

        try {
            String identifier = (String) request.getAttribute("registrationIdentifier");
            log.info("Processing request | operation=registerUserWithoutFile | identifier={}", identifier);

            UUID userId = userService.createUserProfile(userProfileDto, identifier, null);
            UserProfileDto createdProfile = userService.getUserProfile(userId);

            log.info("API call completed | endpoint=POST:/user/register | type=JSON | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200, "User profile created successfully", createdProfile));
        } catch (Exception e) {
            log.error("API call failed | endpoint=POST:/user/register | type=JSON | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Register user. Multipart: part "profile" (JSON), optional part "file" (profile picture).
     * Profile picture is set from file (S3) or from DTO URL in the service.
     */
    @PostMapping(value = "/user/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> registerUser(
            HttpServletRequest request,
            @RequestPart("profile") @Valid UserProfileDto userProfileDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        log.info("API call started | endpoint=POST:/user/register | type=MULTIPART | hasFile={}", file != null && !file.isEmpty());

        try {
            String identifier = (String) request.getAttribute("registrationIdentifier");
            log.info("Processing request | operation=registerUser | identifier={} | hasFile={}", identifier, file != null && !file.isEmpty());

            UUID userId = userService.createUserProfile(userProfileDto, identifier, file);
            UserProfileDto createdProfile = userService.getUserProfile(userId);

            log.info("API call completed | endpoint=POST:/user/register | type=MULTIPART | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200, "User profile created successfully", createdProfile));
        } catch (Exception e) {
            log.error("API call failed | endpoint=POST:/user/register | type=MULTIPART | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update user profile. Multipart: part "profile" (JSON), optional part "file" (new profile picture).
     * Picture and/or profile fields are updated in the service.
     */
    @PatchMapping(value = "/user/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<UserProfileDto>> updateUserProfile(
            @RequestPart("profile") @Valid UserProfileDto userProfileDto,
            @RequestPart(value = "file", required = false) MultipartFile file) throws AuthException, IOException {
        log.info("API call started | endpoint=PATCH:/user/update | hasFile={}", file != null && !file.isEmpty());

        try {
            UUID userId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=updateUserProfile | userId={} | hasFile={}", userId, file != null && !file.isEmpty());

            UserProfileDto updatedProfile = userService.updateUserProfile(userId, userProfileDto, file);

            log.info("API call completed | endpoint=PATCH:/user/update | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200, "User profile updated successfully", updatedProfile));
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=PATCH:/user/update | reason={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=PATCH:/user/update | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/user/delete-user")
    public ResponseEntity<ResponseDto<Void>> deleteUser() throws AuthException {
        log.info("API call started | endpoint=DELETE:/user/delete-user");

        try {
            UUID userId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=deleteUser | userId={}", userId);

            userService.deleteUserProfile(userId);

            log.info("API call completed | endpoint=DELETE:/user/delete-user | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200,"User profile deleted successfully", null));
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=DELETE:/user/delete-user | reason={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=DELETE:/user/delete-user | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/user/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> updateProfilePicture(@RequestBody @Valid UrlDto profilePictureUrl) throws AuthException {
        log.info("API call started | endpoint=PUT:/user/profile-picture");

        try {
            UUID userId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=updateProfilePicture | userId={}", userId);

            UserProfileDto updatedProfile = userService.updateProfilePicture(userId, profilePictureUrl);

            log.info("API call completed | endpoint=PUT:/user/profile-picture | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200,"Profile picture updated  successfully", updatedProfile));
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=PUT:/user/profile-picture | reason={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=PUT:/user/profile-picture | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/user/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> deleteProfilePicture() throws AuthException {
        log.info("API call started | endpoint=DELETE:/user/profile-picture");

        try {
            UUID userId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=deleteProfilePicture | userId={}", userId);

            UserProfileDto updatedProfile = userService.deleteProfilePicture(userId);

            log.info("API call completed | endpoint=DELETE:/user/profile-picture | userId={} | status=SUCCESS", userId);
            return ResponseEntity.ok(ResponseDto.success(200,"Profile picture deleted successfully", updatedProfile));
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=DELETE:/user/profile-picture | reason={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=DELETE:/user/profile-picture | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/user/social-handles")
    public ResponseEntity<ResponseDto<UserProfileDto>> upsertSocialHandles(@RequestBody @Valid List<SocialHandleDto> socialHandles) throws AuthException {
        log.info("API call started | endpoint=PATCH:/user/social-handles | handlesCount={}", socialHandles.size());

        try {
            UUID userId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=upsertSocialHandles | userId={} | handlesCount={}", userId, socialHandles.size());

            UserProfileDto updatedProfile = userService.upsertSocialHandles(userId, socialHandles);

            log.info("API call completed | endpoint=PATCH:/user/social-handles | userId={} | handlesCount={} | status=SUCCESS", userId, socialHandles.size());
            return ResponseEntity.ok(
                    ResponseDto.success(200, "Social handles updated successfully", updatedProfile)
            );
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=PATCH:/user/social-handles | reason={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=PATCH:/user/social-handles | reason={}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/user/{reportedUserId}/report")
    public ResponseEntity<ResponseDto<Void>> reportUser(@PathVariable String reportedUserId, @RequestBody @Valid UserReportRequestDto userReportRequestDto) throws AuthException {
        log.info("API call started | endpoint=POST:/user/{}/report | reportedUserId={}", reportedUserId, reportedUserId);

        try {
            UUID reporterUserId = SecurityUtils.getCurrentUserUuid();
            log.info("Processing request | operation=reportUser | reporterUserId={} | reportedUserId={} | reason={}", reporterUserId, reportedUserId, userReportRequestDto.getReportReasonMessage());

            userService.reportUser(reportedUserId,reporterUserId, userReportRequestDto);

            log.info("API call completed | endpoint=POST:/user/{}/report | reporterUserId={} | reportedUserId={} | status=SUCCESS", reportedUserId, reporterUserId, reportedUserId);
            return ResponseEntity.ok(
                    ResponseDto.success(200, "User reported successfully", null)
            );
        } catch (AuthException e) {
            log.error("Authentication failed | endpoint=POST:/user/{}/report | reason={}", reportedUserId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("API call failed | endpoint=POST:/user/{}/report | reason={}", reportedUserId, e.getMessage(), e);
            throw e;
        }
    }
}

// history table and version in userProfileTable
// populate history table in case of modification and deletion
// test
// Documentation
//
