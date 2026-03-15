package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.request.AddGroupMemberRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateGroupRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.*;
import com.tranzo.tranzo_user_ms.splitwise.exception.*;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Splitwise groups and their members.
 */
@Slf4j
@Service
@Transactional
public class SplitwiseGroupService {

    private final SplitwiseGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    public SplitwiseGroupService(SplitwiseGroupRepository groupRepository,
                               UserRepository userRepository,
                               ActivityService activityService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    /**
     * Creates a new group with proper validation and member management.
     */
    public GroupResponse createGroup(CreateGroupRequest request, UUID createdByUserId) {
        log.info("Creating new group '{}' by user {}", request.getName(), createdByUserId);

        UsersEntity createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new SplitwiseException("Creator user not found: " + createdByUserId));

        // Create group entity
        SplitwiseGroup group = SplitwiseGroup.builder()
                .tripId(UUID.randomUUID()) // Generate trip ID or get from request
                .description(request.getDescription())
                .createdBy(createdByUserId)
                .build();

        // Add creator as admin member
        GroupMember creatorMember = GroupMember.builder()
                .group(group)
                .userId(createdByUserId)
                .role(GroupMember.MemberRole.ADMIN)
                .joinedAt(java.time.LocalDateTime.now())
                .build();

        group.addMember(creatorMember);

        // Add additional members if provided
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (UUID memberId : request.getMemberIds()) {
                if (!memberId.equals(createdByUserId)) {
                    UsersEntity member = userRepository.findById(memberId)
                            .orElseThrow(() -> new SplitwiseException("Member user not found: " + memberId));

                    // Check if user is already a member
                    if (group.isMember(memberId)) {
                        log.warn("User {} is already a member of group {}", memberId, group.getId());
                        continue;
                    }

                    GroupMember groupMember = GroupMember.builder()
                            .group(group)
                            .userId(memberId)
                            .role(GroupMember.MemberRole.MEMBER)
                            .joinedAt(java.time.LocalDateTime.now())
                            .build();

                    group.addMember(groupMember);
                }
            }
        }

        group = groupRepository.save(group);

        // Log activity
        activityService.logGroupCreated(createdBy, group);

        log.info("Successfully created group with ID '{}' and {} members", group.getId(), group.getMembers().size());
        return convertToGroupResponse(group);
    }

    /**
     * Creates a Splitwise group for a published trip. Called when a trip is published.
     * The host is added as the only member (ADMIN). Trip members are added when they join the trip.
     */
    public GroupResponse createGroupForTrip(UUID tripId, String tripTitle, UUID hostUserId) {
        log.info("Creating Splitwise group for trip {} (title: '{}'), host: {}", tripId, tripTitle, hostUserId);

        Optional<SplitwiseGroup> existing = groupRepository.findByTripId(tripId);
        if (existing.isPresent()) {
            log.warn("Splitwise group already exists for trip {}", tripId);
            return convertToGroupResponse(existing.get());
        }

        UsersEntity host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new SplitwiseException("Host user not found: " + hostUserId));

        SplitwiseGroup group = SplitwiseGroup.builder()
                .tripId(tripId)
                .description(tripTitle != null ? tripTitle : "Trip " + tripId)
                .createdBy(hostUserId)
                .build();

        GroupMember hostMember = GroupMember.builder()
                .group(group)
                .userId(hostUserId)
                .role(GroupMember.MemberRole.ADMIN)
                .joinedAt(java.time.LocalDateTime.now())
                .build();
        group.addMember(hostMember);

        group = groupRepository.save(group);
        activityService.logGroupCreated(host, group);

        log.info("Created Splitwise group {} for trip {}, host {} as admin", group.getId(), tripId, hostUserId);
        return convertToGroupResponse(group);
    }

    /**
     * Adds a user to the Splitwise group linked to the given trip. Called when a user joins the trip.
     * No admin check; used by system when trip membership changes.
     */
    public void addMemberToGroupByTripId(UUID tripId, UUID userId) {
        log.info("Adding user {} to Splitwise group for trip {}", userId, tripId);

        SplitwiseGroup group = groupRepository.findByTripId(tripId)
                .orElseThrow(() -> new SplitwiseException("No Splitwise group found for trip: " + tripId));

        if (group.isMember(userId)) {
            log.debug("User {} already in group for trip {}", userId, tripId);
            return;
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new SplitwiseException("User not found: " + userId));

        GroupMember member = GroupMember.builder()
                .group(group)
                .userId(userId)
                .role(GroupMember.MemberRole.MEMBER)
                .joinedAt(java.time.LocalDateTime.now())
                .build();
        group.addMember(member);
        groupRepository.save(group);

        UUID addedBy = group.getAdminMembers().stream()
                .findFirst()
                .map(GroupMember::getUserId)
                .orElse(null);
        activityService.logMemberAdded(userId, group, addedBy);

        log.info("Added user {} to Splitwise group {} for trip {}", userId, group.getId(), tripId);
    }

    /**
     * Gets a group by ID with authorization check.
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId, UUID currentUserId) {
        log.debug("Fetching group {} for user {}", groupId, currentUserId);

        SplitwiseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Verify user is member of the group
        if (!group.isMember(currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }

        log.debug("Successfully retrieved group with ID: {}", group.getId());
        return convertToGroupResponse(group);
    }

    /**
     * Adds members to an existing group.
     */
    public GroupResponse addMembers(Long groupId, AddGroupMemberRequest request, UUID currentUserId) {
        log.info("Adding {} members to group {} by user {}", 
                 request.getMemberIds().size(), groupId, currentUserId);

        SplitwiseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Verify current user is an admin
        validateUserIsGroupAdmin(groupId, currentUserId);

        // Add new members
        for (UUID memberId : request.getMemberIds()) {
            UsersEntity user = userRepository.findById(memberId)
                    .orElseThrow(() -> new SplitwiseException("User not found: " + memberId));

            // Check if user is already a member
            if (group.isMember(memberId)) {
                log.warn("User {} is already a member of group {}", memberId, groupId);
                continue;
            }

            GroupMember member = GroupMember.builder()
                    .group(group)
                    .userId(memberId)
                    .role(GroupMember.MemberRole.MEMBER)
                    .joinedAt(java.time.LocalDateTime.now())
                    .build();

            group.addMember(member);

            // Log activity
            activityService.logMemberAdded(memberId, group, currentUserId);
        }

        group = groupRepository.save(group);

        log.info("Successfully added members to group {}", groupId);
        return convertToGroupResponse(group);
    }

    /**
     * Removes a member from a group.
     */
    public GroupResponse removeMember(Long groupId, UUID memberId, UUID currentUserId) {
        log.info("Removing member {} from group {} by user {}", memberId, groupId, currentUserId);

        SplitwiseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Verify current user is an admin (or removing themselves)
        if (!currentUserId.equals(memberId)) {
            validateUserIsGroupAdmin(groupId, currentUserId);
        }

        UsersEntity memberToRemove = userRepository.findById(memberId)
                .orElseThrow(() -> new SplitwiseException("User not found: " + memberId));

        // Check if user is a member
        if (!group.isMember(memberId)) {
            throw new UserNotMemberException(memberId, groupId);
        }

        // Remove member
        GroupMember groupMember = group.getMembers().stream()
                .filter(member -> member.getUserId().equals(memberId))
                .findFirst()
                .orElse(null);
        if (groupMember != null) {
            group.removeMember(groupMember);
            groupRepository.save(group);

            // Log activity
            activityService.logMemberRemoved(memberId, group, currentUserId);
        }

        log.info("Successfully removed member {} from group {}", memberId, groupId);
        return convertToGroupResponse(group);
    }

    /**
     * Gets all groups for a user.
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(UUID userId) {
        log.debug("Fetching groups for user: {}", userId);

        List<SplitwiseGroup> groups = groupRepository.findByUserId(userId);

        log.debug("Found {} groups for user {}", groups.size(), userId);
        return groups.stream()
                .map(this::convertToGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates group details.
     */
    public GroupResponse updateGroup(Long groupId, CreateGroupRequest request, UUID currentUserId) {
        log.info("Updating group {} by user {}", groupId, currentUserId);

        SplitwiseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Verify current user is an admin
        validateUserIsGroupAdmin(groupId, currentUserId);

        // Update group details
        group.setDescription(request.getDescription());

        group = groupRepository.save(group);

        // Log activity
        activityService.logGroupUpdated(group, currentUserId);

        log.info("Successfully updated group {}", groupId);
        return convertToGroupResponse(group);
    }

    /**
     * Deletes a group.
     */
    public void deleteGroup(Long groupId, UUID currentUserId) {
        log.info("Deleting group {} by user {}", groupId, currentUserId);

        SplitwiseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // Verify current user is an admin
        validateUserIsGroupAdmin(groupId, currentUserId);

        groupRepository.delete(group);

        // Log activity
        activityService.logGroupDeleted(group, currentUserId);

        log.info("Successfully deleted group {}", groupId);
    }

    /**
     * Validates that the user is an admin of the group.
     */
    private void validateUserIsGroupAdmin(Long groupId, UUID userId) {
        boolean isAdmin = groupRepository.isUserAdminOfGroup(groupId, userId);
        if (!isAdmin) {
            throw new SplitwiseException("User " + userId + " is not an admin of group " + groupId);
        }
    }

    /**
     * Converts Group entity to GroupResponse DTO.
     */
    private GroupResponse convertToGroupResponse(SplitwiseGroup group) {
        // Fetch user details for all members
        List<UserResponse> members = group.getMembers().stream()
                .map(member -> {
                    UsersEntity user = userRepository.findById(member.getUserId())
                            .orElse(null);
                    if (user == null) return null;
                    
                    UserProfileEntity profile = user.getUserProfileEntity();
                    String name = (profile != null) ? 
                            (profile.getFirstName() + " " + (profile.getLastName() != null ? profile.getLastName() : "")).trim() 
                            : "Unknown";
                    
                    return UserResponse.builder()
                            .userUuid(member.getUserId())
                            .name(name)
                            .email(user.getEmail())
                            .mobileNumber(user.getCountryCode() + user.getMobileNumber())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        // Fetch creator details
        UsersEntity creator = userRepository.findById(group.getCreatedBy())
                .orElse(null);
        UserResponse creatorResponse = null;
        if (creator != null) {
            UserProfileEntity creatorProfile = creator.getUserProfileEntity();
            String creatorName = (creatorProfile != null) ? 
                    (creatorProfile.getFirstName() + " " + (creatorProfile.getLastName() != null ? creatorProfile.getLastName() : "")).trim() 
                    : "Unknown";
            
            creatorResponse = UserResponse.builder()
                    .userUuid(creator.getUserUuid())
                    .name(creatorName)
                    .email(creator.getEmail())
                    .mobileNumber(creator.getCountryCode() + creator.getMobileNumber())
                    .createdAt(creator.getCreatedAt())
                    .updatedAt(creator.getUpdatedAt())
                    .build();
        }

        return GroupResponse.builder()
                .id(group.getId())
                .name("Group " + group.getId()) // Use a default name since entity doesn't have name field
                .description(group.getDescription())
                .createdBy(creatorResponse)
                .members(members)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
