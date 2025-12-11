package com.tranzo.tranzo_user_ms.service;

import com.tranzo.tranzo_user_ms.dto.SocialHandleDto;
import com.tranzo.tranzo_user_ms.dto.UrlDto;
import com.tranzo.tranzo_user_ms.dto.UserProfileDto;
import com.tranzo.tranzo_user_ms.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.enums.SocialHandle;
import com.tranzo.tranzo_user_ms.exception.InvalidPatchRequestException;
import com.tranzo.tranzo_user_ms.exception.InvalidUserIdException;
import com.tranzo.tranzo_user_ms.exception.UserAlreadyDeletedExeption;
import com.tranzo.tranzo_user_ms.exception.UserProfileNotFoundException;
import com.tranzo.tranzo_user_ms.model.SocialHandleEntity;
import com.tranzo.tranzo_user_ms.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.model.UsersEntity;
import com.tranzo.tranzo_user_ms.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileDto getUserProfile(String userId) {
        log.info("Fetching user profile for userId: {}", userId);
        UUID userUuid ;
        try{
            userUuid = UUID.fromString(userId);
        }catch(IllegalArgumentException e){
            throw new InvalidUserIdException("Invalid user id: " + userId);
        }

        UserProfileEntity profileEntity = userProfileRepository
                .findWithSocialHandlesByUserUuid(userUuid)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));
        log.info("User profile found for userId: {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

    private UserProfileDto mapToUserProfileDto(UserProfileEntity profileEntity) {

        UsersEntity user = profileEntity.getUser();

        List<SocialHandleDto> socialHandleDtos = user.getSocialHandleEntity().stream()
                .map(this::mapToSocialHandleDto)
                .collect(Collectors.toList());

        return UserProfileDto.builder()
                .firstName(profileEntity.getFirstName())
                .middleName(profileEntity.getMiddleName())
                .lastName(profileEntity.getLastName())
                .bio(profileEntity.getBio())
                .gender(profileEntity.getGender())
                .mobileNumber(user.getMobileNumber())
                .emailId(user.getEmail())
                .dob(profileEntity.getDob())
                .location(profileEntity.getLocation())
                .profilePictureUrl(profileEntity.getProfilePictureUrl())
                .socialHandleDtoList(socialHandleDtos)
                .build();
    }

    private SocialHandleDto mapToSocialHandleDto(SocialHandleEntity entity) {
        return SocialHandleDto.builder()
                .platform(entity.getPlatform())
                .url(entity.getPlatformUrl())
                .build();
    }

    @Transactional
    public UserProfileDto updateUserProfile(String userId, UserProfileDto modifiedUserProfileDto) {

        UUID userUUID ;
        try{
            userUUID = UUID.fromString(userId);
        }catch(IllegalArgumentException e){
            throw new InvalidUserIdException("Invalid user id: " + userId);
        }

        if(isEmptyUpdateRequest(modifiedUserProfileDto)){
            throw new InvalidPatchRequestException("No fields provided for update");
        }

        UserProfileEntity profileEntity = userProfileRepository
                .findWithSocialHandlesByUserUuid(userUUID)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        UsersEntity user = profileEntity.getUser();

        if(modifiedUserProfileDto.getFirstName() != null) {
            profileEntity.setFirstName(modifiedUserProfileDto.getFirstName());
        }
        if(modifiedUserProfileDto.getMiddleName() != null) {
            profileEntity.setMiddleName(modifiedUserProfileDto.getMiddleName());
        }
        if(modifiedUserProfileDto.getLastName() != null) {
            profileEntity.setLastName(modifiedUserProfileDto.getLastName());
        }
        if(modifiedUserProfileDto.getBio() != null) {
            profileEntity.setBio(modifiedUserProfileDto.getBio());
        }
        if(modifiedUserProfileDto.getGender() != null) {
            profileEntity.setGender(modifiedUserProfileDto.getGender());
        }
        if(modifiedUserProfileDto.getMobileNumber() != null) {
            user.setMobileNumber(modifiedUserProfileDto.getMobileNumber());
        }
        if(modifiedUserProfileDto.getEmailId() != null) {
            user.setEmail(modifiedUserProfileDto.getEmailId());
        }
        if(modifiedUserProfileDto.getDob() != null) {
            profileEntity.setDob(modifiedUserProfileDto.getDob());
        }
        if(modifiedUserProfileDto.getLocation() != null) {
            profileEntity.setLocation(modifiedUserProfileDto.getLocation());
        }
        if(modifiedUserProfileDto.getSocialHandleDtoList() != null) {
            user.getSocialHandleEntity().clear();
            List<SocialHandleEntity> updatedSocialHandles = modifiedUserProfileDto.getSocialHandleDtoList().stream()
                    .map(dto -> {
                        SocialHandleEntity entity = new SocialHandleEntity();
                        entity.setPlatform(dto.getPlatform());
                        entity.setPlatformUrl(dto.getUrl());
                        entity.setUser(user);
                        return entity;
                    })
                    .toList();
            user.getSocialHandleEntity().addAll(updatedSocialHandles);
        }

        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public void deleteUserProfile(String userId) {
        UUID userUUID ;
        try{
            userUUID = UUID.fromString(userId);
        }catch(IllegalArgumentException e){
            throw new InvalidUserIdException("Invalid user id: " + userId);
        }

        UsersEntity user = userRepository
                .findUserByUserUuid(userUUID)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        if(user.getAccountStatus()== AccountStatus.DELETED){
            throw new UserAlreadyDeletedExeption("User already deleted for id: " + userId);
        }
        user.setAccountStatus(AccountStatus.DELETED);

        // Additional cleanup logic can be added here if needed and after discussion we will implement it.
    }

    boolean isEmptyUpdateRequest(UserProfileDto dto){
        return dto.getFirstName() == null &&
                dto.getMiddleName() == null &&
                dto.getLastName() == null &&
                dto.getBio() == null &&
                dto.getGender() == null &&
                dto.getMobileNumber() == null &&
                dto.getEmailId() == null &&
                dto.getDob() == null &&
                dto.getLocation() == null &&
                dto.getSocialHandleDtoList() == null;
    }

    @Transactional
    public UserProfileDto updateProfilePicture(String userId, UrlDto profilePictureUrl) {
        UUID userUuid ;
        try{
            userUuid = UUID.fromString(userId);
        }catch(IllegalArgumentException e){
            throw new InvalidUserIdException("Invalid user id: " + userId);
        }
        UserProfileEntity profileEntity = userProfileRepository
                .findWithSocialHandlesByUserUuid(userUuid)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));
        profileEntity.setProfilePictureUrl(profilePictureUrl.getUrl());
        log.info("Profile picture updated for userId: {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public UserProfileDto deleteProfilePicture(String userId){
        UUID userUuid ;
        try{
            userUuid = UUID.fromString(userId);
        }catch(IllegalArgumentException e){
            throw new InvalidUserIdException("Invalid user id: " + userId);
        }
        UserProfileEntity profileEntity = userProfileRepository
                .findWithSocialHandlesByUserUuid(userUuid)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));
        profileEntity.setProfilePictureUrl(null);
        log.info("Profile picture deleted for userId: {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public UserProfileDto upsertSocialHandles(String userId, List<SocialHandleDto> socialHandles) {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserIdException("Invalid user id: " + userId);
        }

        if (socialHandles == null || socialHandles.isEmpty()) {
            throw new InvalidPatchRequestException("At least one social handle must be provided");
        }

        // Fetch profile + user + social handles (your existing query with join fetch)
        UserProfileEntity profileEntity = userProfileRepository
                .findWithSocialHandlesByUserUuid(userUuid)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        UsersEntity user = profileEntity.getUser();

        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new UserAlreadyDeletedExeption("User account is deleted for id: " + userId);
        }

        List<SocialHandleEntity> existingHandles = user.getSocialHandleEntity();

        // Map existing handles by platform for easy lookup
        Map<SocialHandle, SocialHandleEntity> existingByPlatform = existingHandles.stream()
                .collect(Collectors.toMap(
                        SocialHandleEntity::getPlatform,
                        e -> e
                ));

        // Optional: avoid duplicates in request
        Set<SocialHandle> seen = new HashSet<>();
        for (SocialHandleDto dto : socialHandles) {
            if (!seen.add(dto.getPlatform())) {
                throw new InvalidPatchRequestException(
                        "Duplicate platform in request: " + dto.getPlatform()
                );
            }
        }

        // Apply operations:
        //  - url null/blank -> delete
        //  - url present     -> add/update
        for (SocialHandleDto dto : socialHandles) {
            SocialHandle platform = dto.getPlatform();
            String url = dto.getUrl();
            SocialHandleEntity existing = existingByPlatform.get(platform);

            // DELETE case
            if (url == null || url.isBlank()) {
                if (existing != null) {
                    existingHandles.remove(existing);
                    existingByPlatform.remove(platform);
                }
                continue;
            }

            // ADD / UPDATE case
            if (existing == null) {
                SocialHandleEntity entity = new SocialHandleEntity();
                entity.setUser(user);
                entity.setPlatform(platform);
                entity.setPlatformUrl(url);

                existingHandles.add(entity);
                existingByPlatform.put(platform, entity);
            } else {
                existing.setPlatformUrl(url);
            }
        }

        log.info("Social handles updated for user {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

}