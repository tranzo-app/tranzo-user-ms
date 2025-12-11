package com.tranzo.tranzo_user_ms.controller;

import com.tranzo.tranzo_user_ms.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.dto.SocialHandleDto;
import com.tranzo.tranzo_user_ms.dto.UrlDto;
import com.tranzo.tranzo_user_ms.dto.UserProfileDto;
import com.tranzo.tranzo_user_ms.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDto> getUser(@PathVariable String userId) {
        log.info("GET /user/{} called", userId);
        UserProfileDto userProfileDto = userService.getUserProfile(userId);
        log.debug("User profile for {} retrieved successfully", userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile retrieved successfully", userProfileDto));
    }

    @PatchMapping("/user/{userId}")
    public ResponseEntity<ResponseDto> updateUserProfile(@PathVariable String userId, @RequestBody @Valid UserProfileDto userProfileDto) {
        log.info("PATCH /user/{} called with payload: {}", userId, userProfileDto);
        UserProfileDto updatedUserProfile = userService.updateUserProfile(userId, userProfileDto);
        log.debug("User profile for {} updated successfully", userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile retrieved successfully", userProfileDto));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable String userId) {
        log.info("DELETE /user/{} called", userId);
        userService.deleteUserProfile(userId);
        log.debug("User {} deleted successfully", userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile retrieved successfully", null));
    }

    @PutMapping("/user/{userId}/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> updateProfilePicture(@PathVariable String userId, @RequestBody @Valid UrlDto profilePictureUrl) {
        log.info("PUT /user/{}/profile-picture called with URL: {}", userId, profilePictureUrl.getUrl());
        UserProfileDto updatedProfile = userService.updateProfilePicture(userId, profilePictureUrl);
        log.debug("Profile picture for user {} updated successfully", userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile retrieved successfully", updatedProfile));
    }

    @DeleteMapping("/user/{userId}/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> deleteProfilePicture(@PathVariable String userId) {
        log.info("DELETE /user/{}/profile-picture called", userId);
        UserProfileDto updatedProfile = userService.deleteProfilePicture(userId);
        log.debug("Profile picture for user {} deleted successfully", userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile retrieved successfully", updatedProfile));
    }

    @PatchMapping("/user/{userId}/social-handles")
    public ResponseEntity<ResponseDto<UserProfileDto>> upsertSocialHandles(
            @PathVariable String userId,
            @RequestBody @Valid List<SocialHandleDto> socialHandles) {

        log.info("PATCH /user/{}/social-handles called with payload: {}", userId, socialHandles);

        UserProfileDto updatedProfile = userService.upsertSocialHandles(userId, socialHandles);

        log.debug("Social handles for user {} updated successfully", userId);
        return ResponseEntity.ok(
                ResponseDto.success(200, "Social handles updated successfully", updatedProfile)
        );
    }

}
