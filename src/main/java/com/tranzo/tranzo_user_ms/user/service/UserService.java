package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
import com.tranzo.tranzo_user_ms.user.dto.SocialHandleDto;
import com.tranzo.tranzo_user_ms.user.dto.UrlDto;
import com.tranzo.tranzo_user_ms.user.dto.UserProfileDto;
import com.tranzo.tranzo_user_ms.user.dto.UserReportRequestDto;
import com.tranzo.tranzo_user_ms.user.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.user.enums.SocialHandle;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import com.tranzo.tranzo_user_ms.user.model.*;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileHistoryRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserProfileRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserReportRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserReportRepository userReportRepository;
    private final UserProfileHistoryRepository userProfileHistoryRepository;

    public void findUserByUserId(UUID userUuid) {
        userRepository.findUserByUserUuid(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UserProfileDto getUserProfile(UUID userId) {
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));
        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public void createUserProfile(UserProfileDto userProfileDto, UUID userId) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Prevent duplicate profile creation
        if (user.getUserProfileEntity() != null) {
            throw new RuntimeException("User profile already exists");
        }
        UserProfileEntity userProfileEntity = new UserProfileEntity();
        userProfileEntity.setFirstName(userProfileDto.getFirstName());
        userProfileEntity.setMiddleName(userProfileDto.getMiddleName());
        userProfileEntity.setLastName(userProfileDto.getLastName());
        userProfileEntity.setBio(userProfileDto.getBio());
        userProfileEntity.setGender(userProfileDto.getGender());
        userProfileEntity.setDob(userProfileDto.getDob());
        userProfileEntity.setLocation(userProfileDto.getLocation());
        userProfileEntity.setProfilePictureUrl(userProfileDto.getProfilePictureUrl());
        userProfileEntity.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
        if (userProfileDto.getSocialHandleDtoList() != null && !userProfileDto.getSocialHandleDtoList().isEmpty())
        {
            List<SocialHandleEntity> socialHandles = userProfileDto.getSocialHandleDtoList().stream()
                    .map(sh -> {
                        SocialHandleEntity socialHandleEntity = new SocialHandleEntity();
                        socialHandleEntity.setPlatform(sh.getPlatform());
                        socialHandleEntity.setPlatformUrl(sh.getUrl());
                        socialHandleEntity.setUser(user);
                        return socialHandleEntity;
                    }).toList();
            user.setSocialHandleEntity(socialHandles);
        }
        userProfileEntity.setUser(user);
        user.setUserProfileEntity(userProfileEntity);
        userRepository.save(user);
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
    private UserProfileHistoryEntity mapToUserProfileHistoryEntity(UserProfileEntity profileEntity) {
        UsersEntity user = profileEntity.getUser();
        return UserProfileHistoryEntity.builder()
                .userProfileUuid(profileEntity.getUserProfileUuid())
                .profileVersion(profileEntity.getVersion())
                .user(profileEntity.getUser())
                .firstName(profileEntity.getFirstName())
                .middleName(profileEntity.getMiddleName())
                .lastName(profileEntity.getLastName())
                .profilePictureUrl(profileEntity.getProfilePictureUrl())
                .bio(profileEntity.getBio())
                .gender(profileEntity.getGender())
                .dob(profileEntity.getDob())
                .location(profileEntity.getLocation())
                .verificationStatus(profileEntity.getVerificationStatus())
                .build();
    }

    @Transactional
    public UserProfileDto updateUserProfile(UUID userId, UserProfileDto modifiedUserProfileDto) {
        if(isEmptyUpdateRequest(modifiedUserProfileDto)){
            throw new InvalidPatchRequestException("No fields provided for update");
        }

        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        UsersEntity user = profileEntity.getUser();

        userProfileHistoryRepository.save(mapToUserProfileHistoryEntity(profileEntity));

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
    public void deleteUserProfile(UUID userId) {
       /* UsersEntity user = userRepository
                .findUserByUserUuid(userUUID)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));
*/
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        UsersEntity user = profileEntity.getUser();

        if(profileEntity.getUser().getAccountStatus()== AccountStatus.DELETED){
            throw new UserAlreadyDeletedException("User already deleted for id: " + userId);
        }

        userProfileHistoryRepository.save(mapToUserProfileHistoryEntity(profileEntity));

        user.setAccountStatus(AccountStatus.DELETED);
        user.setUserProfileEntity(null);
        user.getSocialHandleEntity().clear();

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
    public UserProfileDto updateProfilePicture(UUID userId, UrlDto profilePictureUrl) {
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));

        userProfileHistoryRepository.save(mapToUserProfileHistoryEntity(profileEntity));

        profileEntity.setProfilePictureUrl(profilePictureUrl.getUrl());

        log.info("Profile picture updated for userId: {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public UserProfileDto deleteProfilePicture(UUID userId){
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));

        userProfileHistoryRepository.save(mapToUserProfileHistoryEntity(profileEntity));

        profileEntity.setProfilePictureUrl(null);
        log.info("Profile picture deleted for userId: {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public UserProfileDto upsertSocialHandles(UUID userId, List<SocialHandleDto> socialHandles) {
        if (socialHandles == null || socialHandles.isEmpty()) {
            throw new InvalidPatchRequestException("At least one social handle must be provided");
        }

        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        UsersEntity user = profileEntity.getUser();

        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw new UserAlreadyDeletedException("User account is deleted for id: " + userId);
        }

        List<SocialHandleEntity> existingHandles = user.getSocialHandleEntity();

        Map<SocialHandle, SocialHandleEntity> existingByPlatform = existingHandles.stream()
                .collect(Collectors.toMap(
                        SocialHandleEntity::getPlatform,
                        e -> e
                ));

        Set<SocialHandle> seen = new HashSet<>();
        for (SocialHandleDto dto : socialHandles) {
            if (!seen.add(dto.getPlatform())) {
                throw new InvalidPatchRequestException(
                        "Duplicate platform in request: " + dto.getPlatform()
                );
            }
        }

        for (SocialHandleDto dto : socialHandles) {
            SocialHandle platform = dto.getPlatform();
            String url = dto.getUrl();
            SocialHandleEntity existing = existingByPlatform.get(platform);

            if (url == null || url.isBlank()) {
                if (existing != null) {
                    existingHandles.remove(existing);
                    existingByPlatform.remove(platform);
                }
                continue;
            }

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

    public void reportUser(String reportedUserId, UUID reporterUserId, UserReportRequestDto userReportRequestDto) {
            UUID reportedUuid;
            try {
                reportedUuid = UUID.fromString(reportedUserId);
            } catch (IllegalArgumentException ex) {
                throw new InvalidUserIdException("Invalid user id(s): " + reportedUserId + ", " + reporterUserId);
            }
            if(reportedUuid.equals(reporterUserId)){
                throw new InvalidReportRequestException("User cannot report themselves: " + reportedUserId);
            }

            if(userReportRepository.existsByReportedUserIdAndReportingUserId(reportedUuid,reporterUserId)){
                throw new DuplicateReportException("User has already reported this user: " + reportedUserId);
            }

            if(!userProfileExists(reportedUuid)){
                throw new UserProfileNotFoundException("Reported user does not exist: " + reportedUserId);
            }

            UserReportEntity userReport = UserReportEntity.builder()
                    .reportedUserId(reportedUuid)
                    .reportingUserId(reportedUuid)
                    .message(userReportRequestDto.getReportReasonMessage())
                    .build();

            userReportRepository.save(userReport);
            log.info("User {} reported user {} successfully", reporterUserId, reportedUserId);
    }

    boolean userProfileExists(UUID userProfileId) {
        UsersEntity user = userRepository
                .findUserByUserUuid(userProfileId)
                .orElse(null);
        if (user == null || user.getAccountStatus() == AccountStatus.DELETED) {
            return false;
        }
        return true;
    }
}