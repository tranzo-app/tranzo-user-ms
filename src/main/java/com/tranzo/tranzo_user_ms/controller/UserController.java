package com.tranzo.tranzo_user_ms.controller;

import com.tranzo.tranzo_user_ms.dto.*;
import com.tranzo.tranzo_user_ms.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class UserController {

    // After Implementation of JWT Authentication, userId can be fetched from the token itself.

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDto> getUser(@PathVariable String userId) {
        UserProfileDto userProfileDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile fetched successfully", userProfileDto));
    }

    @PatchMapping("/user/update/{userId}")
    public ResponseEntity<ResponseDto> updateUserProfile(@PathVariable String userId, @RequestBody @Valid UserProfileDto userProfileDto) {
        UserProfileDto updatedUserProfile = userService.updateUserProfile(userId, userProfileDto);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile updated successfully", userProfileDto));
    }

    @DeleteMapping("/user/delete-user/{userId}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable String userId) {
        userService.deleteUserProfile(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"User profile deleted successfully", null));
    }

    @PutMapping("/user/{userId}/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> updateProfilePicture(@PathVariable String userId, @RequestBody @Valid UrlDto profilePictureUrl) {
        UserProfileDto updatedProfile = userService.updateProfilePicture(userId, profilePictureUrl);
        return ResponseEntity.ok(ResponseDto.success(200,"Profile picture updated  successfully", updatedProfile));
    }

    @DeleteMapping("/user/{userId}/profile-picture")
    public ResponseEntity<ResponseDto<UserProfileDto>> deleteProfilePicture(@PathVariable String userId) {
        UserProfileDto updatedProfile = userService.deleteProfilePicture(userId);
        return ResponseEntity.ok(ResponseDto.success(200,"Profile picture deleted successfully", updatedProfile));
    }

    @PatchMapping("/user/{userId}/social-handles")
    public ResponseEntity<ResponseDto<UserProfileDto>> upsertSocialHandles(
            @PathVariable String userId,
            @RequestBody @Valid List<SocialHandleDto> socialHandles) {
        UserProfileDto updatedProfile = userService.upsertSocialHandles(userId, socialHandles);
        return ResponseEntity.ok(
                ResponseDto.success(200, "Social handles updated successfully", updatedProfile)
        );
    }

    @PostMapping("/User/{reportedUserId}/{reporterUserId}/report")
    public ResponseEntity<ResponseDto<Void>> reportUser(@PathVariable String reportedUserId,@PathVariable String reporterUserId, @RequestBody @Valid UserReportRequestDto userReportRequestDto) {
        userService.reportUser(reportedUserId,reporterUserId, userReportRequestDto);
        return ResponseEntity.ok(
                ResponseDto.success(200, "User reported successfully", null)
        );
    }
}
