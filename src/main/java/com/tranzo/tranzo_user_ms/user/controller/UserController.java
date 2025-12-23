package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.service.JwtService;
import com.tranzo.tranzo_user_ms.user.dto.*;
import com.tranzo.tranzo_user_ms.user.service.UserService;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseDto<UserProfileDto>> getUser() throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto userProfileDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile fetched successfully", userProfileDto));
    }

    @PostMapping("/user/register")
    public ResponseEntity<ResponseDto<Void>> registerUser(HttpServletRequest request, @Valid @RequestBody UserProfileDto userProfileDto) throws  AuthException {
        String identifier = (String) request.getAttribute("registrationIdentifier");
        userService.createUserProfile(userProfileDto, identifier);
        return ResponseEntity.ok(ResponseDto.success(200, "User profile created successfully", null));
    }

    @PatchMapping("/user/update")
    public ResponseEntity<ResponseDto<UserProfileDto>> updateUserProfile(@RequestBody @Valid UserProfileDto userProfileDto) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedUserProfile = userService.updateUserProfile(userId, userProfileDto);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile updated successfully", updatedUserProfile));
    }

    @DeleteMapping("/user/delete-user")
    public ResponseEntity<ResponseDto<Void>> deleteUser() throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        userService.deleteUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile deleted successfully", null));
    }

    @PutMapping("/user/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> updateProfilePicture(@RequestBody @Valid UrlDto profilePictureUrl) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.updateProfilePicture(userId, profilePictureUrl);
        return ResponseEntity.ok(ResponseDto.success(200,"Profile picture updated  successfully", updatedProfile));
    }

    @DeleteMapping("/user/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> deleteProfilePicture() throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.deleteProfilePicture(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"Profile picture deleted successfully", updatedProfile));
    }

    @PatchMapping("/user/social-handles")
    public ResponseEntity<ResponseDto<UserProfileDto>> upsertSocialHandles(@RequestBody @Valid List<SocialHandleDto> socialHandles) throws AuthException {
        String userId = SecurityUtils.getCurrentUserUuid();
        UserProfileDto updatedProfile = userService.upsertSocialHandles(userId, socialHandles);
        return ResponseEntity.ok(
                ResponseDto.success(200, "Social handles updated successfully", updatedProfile)
        );
    }

    @PostMapping("/User/{reportedUserId}/report")
    public ResponseEntity<ResponseDto<Void>> reportUser(@PathVariable String reportedUserId, @RequestBody @Valid UserReportRequestDto userReportRequestDto) throws AuthException {
        String reporterUserId = SecurityUtils.getCurrentUserUuid();
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