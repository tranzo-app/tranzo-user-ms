package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.commons.exception.*;
import com.tranzo.tranzo_user_ms.trip.client.TripStatisticsClient;
import com.tranzo.tranzo_user_ms.user.dto.SocialHandleDto;
import com.tranzo.tranzo_user_ms.user.dto.UrlDto;
import com.tranzo.tranzo_user_ms.user.dto.PublicUserProfileDto;
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
import com.tranzo.tranzo_user_ms.media.service.S3MediaService;
import org.springframework.transaction.annotation.Transactional;
import com.tranzo.tranzo_user_ms.user.utility.UserUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final UserUtility userUtility;
    private final S3MediaService s3MediaService;
    private final TravelPalService travelPalService;
    private final RatingService ratingService;
    private final TripStatisticsClient tripStatisticsClient;

    public void findUserByUserId(UUID userUuid) {
        userRepository.findUserByUserUuid(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Returns the user profile. Profile picture URL is resolved to a presigned URL when stored in S3.
     */
    public UserProfileDto getUserProfile(UUID userId) {
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));
        return mapToUserProfileDto(profileEntity);
    }

    /**
     * Public profile for another user: no email or mobile. Picture URL presigned when stored in S3.
     */
    public PublicUserProfileDto getPublicUserProfile(UUID userId) {
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));
        return mapToPublicUserProfileDto(profileEntity);
    }

    private PublicUserProfileDto mapToPublicUserProfileDto(UserProfileEntity profileEntity) {
        UsersEntity user = profileEntity.getUser();
        List<SocialHandleDto> socialHandleDtos = user.getSocialHandleEntity().stream()
                .map(this::mapToSocialHandleDto)
                .collect(Collectors.toList());
        String profilePictureUrl = resolveProfilePictureUrl(profileEntity.getProfilePictureUrl());
        return PublicUserProfileDto.builder()
                .firstName(profileEntity.getFirstName())
                .middleName(profileEntity.getMiddleName())
                .lastName(profileEntity.getLastName())
                .bio(profileEntity.getBio())
                .gender(profileEntity.getGender())
                .dob(profileEntity.getDob())
                .location(profileEntity.getLocation())
                .profilePictureUrl(profilePictureUrl)
                .socialHandleDtoList(socialHandleDtos)
                .verificationStatus(profileEntity.getVerificationStatus())
                .travelPalsCount(travelPalService.getMyTravelPals(user.getUserUuid()).size())
                .completedTripsCount(tripStatisticsClient.getCompletedTripsCount(user.getUserUuid()))
                .userRating(ratingService.getUserAverageRating(user.getUserUuid()))
                .build();
    }

    /**
     * Creates user profile. Profile picture is set from uploaded file (S3 key) if present,
     * otherwise from DTO's profilePictureUrl (e.g. external URL) when non-blank.
     */
    @Transactional
    public UUID createUserProfile(UserProfileDto userProfileDto, String identifier, MultipartFile file) throws IOException {
        UsersEntity user = userUtility.findUserByIdentifier(identifier)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found for identifier: " + identifier
                ));
        // Prevent duplicate profile creation
        if (user.getUserProfileEntity() != null) {
            throw new UserProfileAlreadyExistsException(
                    "User profile already exists for user: " + user.getUserUuid()
            );
        }
        UserProfileEntity userProfileEntity = new UserProfileEntity();
        userProfileEntity.setFirstName(userProfileDto.getFirstName());
        userProfileEntity.setMiddleName(userProfileDto.getMiddleName());
        userProfileEntity.setLastName(userProfileDto.getLastName());
        userProfileEntity.setBio(userProfileDto.getBio());
        userProfileEntity.setGender(userProfileDto.getGender());
        userProfileEntity.setDob(userProfileDto.getDob());
        userProfileEntity.setLocation(userProfileDto.getLocation());
        userProfileEntity.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
        userProfileEntity.setUser(user);
        user.setUserProfileEntity(userProfileEntity);
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
            user.getSocialHandleEntity().addAll(socialHandles);
        }
        // Handle profile picture BEFORE saving the user profile
        String profilePictureUrl = null;
        if (file != null && !file.isEmpty()) {
            var uploadResult = s3MediaService.upload(file, user.getUserUuid().toString());
            profilePictureUrl = uploadResult.getKey();
        } else if (userProfileDto.getProfilePictureUrl() != null && !userProfileDto.getProfilePictureUrl().isBlank()) {
            profilePictureUrl = userProfileDto.getProfilePictureUrl();
        }
        userProfileEntity.setProfilePictureUrl(profilePictureUrl);
        userRepository.save(user);
        UUID savedUserId = user.getUserUuid();
        log.info("User profile created successfully for userId: {} with profilePictureUrl: {}", savedUserId, profilePictureUrl);
        return savedUserId;
    }

    private UserProfileDto mapToUserProfileDto(UserProfileEntity profileEntity) {

        UsersEntity user = profileEntity.getUser();

        List<SocialHandleDto> socialHandleDtos = user.getSocialHandleEntity().stream()
                .map(this::mapToSocialHandleDto)
                .collect(Collectors.toList());

        String profilePictureUrl = resolveProfilePictureUrl(profileEntity.getProfilePictureUrl());

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
                .profilePictureUrl(profilePictureUrl)
                .socialHandleDtoList(socialHandleDtos)
                .verificationStatus(profileEntity.getVerificationStatus())
                .travelPalsCount(travelPalService.getMyTravelPals(user.getUserUuid()).size())
                .completedTripsCount(tripStatisticsClient.getCompletedTripsCount(user.getUserUuid()))
                .userRating(ratingService.getUserAverageRating(user.getUserUuid()))
                .build();
    }

    /**
     * If the value is an S3 key (starts with "uploads/"), returns a presigned URL for display.
     * Otherwise returns the value as-is (e.g. external URL).
     */
    public String resolveProfilePictureUrl(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) {
            return urlOrKey;
        }
        if (!urlOrKey.startsWith("uploads/")) {
            return urlOrKey;
        }
        try {
            return s3MediaService.getPresignedUrl(urlOrKey, null).getUrl();
        } catch (Exception e) {
            log.debug("Could not resolve S3 key to presigned URL (S3 may not be configured): {}", e.getMessage());
            return urlOrKey;
        }
    }

    private SocialHandleDto mapToSocialHandleDto(SocialHandleEntity entity) {
        return SocialHandleDto.builder()
                .platform(entity.getPlatform())
                .url(entity.getPlatformUrl())
                .build();
    }
    private UserProfileHistoryEntity mapToUserProfileHistoryEntity(UserProfileEntity profileEntity, int historyVersion) {
        UsersEntity user = profileEntity.getUser();
        return UserProfileHistoryEntity.builder()
                .userProfileUuid(profileEntity.getUserProfileUuid())
                .profileVersion(historyVersion)
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

    private void saveProfileHistory(UserProfileEntity profileEntity) {
        int nextVersion = userProfileHistoryRepository.findMaxVersionByUserProfileUuid(profileEntity.getUserProfileUuid()) + 1;
        userProfileHistoryRepository.save(mapToUserProfileHistoryEntity(profileEntity, nextVersion));
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

        saveProfileHistory(profileEntity);

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

    /**
     * Updates user profile with optional new picture. If file is present, uploads to S3 and sets key;
     * if no file but profile has other fields, updates those. Returns the updated profile.
     */
    @Transactional
    public UserProfileDto updateUserProfile(UUID userId, UserProfileDto modifiedUserProfileDto, MultipartFile file) throws IOException {
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found for id: " + userId));

        // Save history before making changes
        saveProfileHistory(profileEntity);

        // Handle profile picture upload FIRST
        if (file != null && !file.isEmpty()) {
            var uploadResult = s3MediaService.upload(file, userId.toString());
            profileEntity.setProfilePictureUrl(uploadResult.getKey());
            log.info("Profile picture uploaded for userId: {} with key: {}", userId, uploadResult.getKey());
        }

        // Update other profile fields
        if (!isEmptyUpdateRequest(modifiedUserProfileDto)) {
            updateUserProfile(userId, modifiedUserProfileDto);
        }

        // Save the profile entity with the new profile picture URL
        userProfileRepository.save(profileEntity);

        log.info("User profile updated successfully for userId: {}", userId);
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

        saveProfileHistory(profileEntity);

        user.setAccountStatus(AccountStatus.DELETED);
        user.setUserProfileEntity(null);
        user.getSocialHandleEntity().clear();

        // Additional cleanup logic can be added here if needed and after discussion we will implement it.
    }

    public boolean isEmptyUpdateRequest(UserProfileDto dto){
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

        saveProfileHistory(profileEntity);

        profileEntity.setProfilePictureUrl(profilePictureUrl.getUrl());
        userProfileRepository.save(profileEntity);

        log.info("Profile picture updated for userId: {}", userId);
        return mapToUserProfileDto(profileEntity);
    }

    @Transactional
    public UserProfileDto deleteProfilePicture(UUID userId){
        UserProfileEntity profileEntity = userProfileRepository
                .findAllUserProfileDetailByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User not found for id: " + userId));

        saveProfileHistory(profileEntity);

        profileEntity.setProfilePictureUrl(null);
        userProfileRepository.save(profileEntity);
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