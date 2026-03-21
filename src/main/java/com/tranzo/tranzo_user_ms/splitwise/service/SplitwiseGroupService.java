package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.request.AddGroupMemberRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateGroupRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.GroupMember;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.GroupNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.UserNotMemberException;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.trip.model.TripEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing Splitwise groups: CRUD, membership, and trip-linked group creation.
 */
@Slf4j
@Service
@Transactional
public class SplitwiseGroupService {

    private final SplitwiseGroupRepository splitwiseGroupRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    public SplitwiseGroupService(SplitwiseGroupRepository splitwiseGroupRepository,
                                TripRepository tripRepository,
                                UserRepository userRepository,
                                ActivityService activityService) {
        this.splitwiseGroupRepository = splitwiseGroupRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    /**
     * Creates a new group (manual creation). Current user becomes ADMIN; initial members from request.
     */
    public GroupResponse createGroup(CreateGroupRequest request, UUID currentUserId) {
        UUID syntheticTripId = UUID.randomUUID();
        String description = request.getDescription() != null ? request.getDescription() : request.getName();
        SplitwiseGroup group = SplitwiseGroup.builder()
                .tripId(syntheticTripId)
                .description(description)
                .createdBy(currentUserId)
                .build();
        group = splitwiseGroupRepository.save(group);

        GroupMember adminMember = GroupMember.builder()
                .group(group)
                .userId(currentUserId)
                .role(GroupMember.MemberRole.ADMIN)
                .build();
        group.addMember(adminMember);

        for (UUID memberId : request.getMemberIds()) {
            if (memberId.equals(currentUserId)) continue;
            GroupMember member = GroupMember.builder()
                    .group(group)
                    .userId(memberId)
                    .role(GroupMember.MemberRole.MEMBER)
                    .build();
            group.addMember(member);
        }
        splitwiseGroupRepository.save(group);

        UsersEntity currentUser = userRepository.findUserByUserUuid(currentUserId).orElse(null);
        activityService.logGroupCreated(currentUser != null ? currentUser : null, group);
        log.info("Created group {} by user {}", group.getId(), currentUserId);
        return toGroupResponse(group);
    }

    /**
     * Creates a Splitwise group for a trip (event-driven). Host is added as ADMIN.
     */
    public GroupResponse createGroupForTrip(UUID tripId, String name, UUID hostUserId) {
        SplitwiseGroup group = SplitwiseGroup.builder()
                .tripId(tripId)
                .description(name)
                .createdBy(hostUserId)
                .build();
        group = splitwiseGroupRepository.save(group);

        GroupMember adminMember = GroupMember.builder()
                .group(group)
                .userId(hostUserId)
                .role(GroupMember.MemberRole.ADMIN)
                .build();
        group.addMember(adminMember);
        splitwiseGroupRepository.save(group);

        UsersEntity host = userRepository.findUserByUserUuid(hostUserId).orElse(null);
        activityService.logGroupCreated(host != null ? host : null, group);
        log.info("Created Splitwise group {} for trip {}", group.getId(), tripId);
        return toGroupResponse(group);
    }

    /**
     * Adds a user to the group associated with the given trip (e.g. on participant join).
     */
    public void addMemberToGroupByTripId(UUID tripId, UUID userId) {
        SplitwiseGroup group = splitwiseGroupRepository.findByTripId(tripId)
                .orElseThrow(() -> new GroupNotFoundException("No group found for trip " + tripId));
        if (group.isMember(userId)) {
            log.debug("User {} already in group {}", userId, group.getId());
            return;
        }
        GroupMember member = GroupMember.builder()
                .group(group)
                .userId(userId)
                .role(GroupMember.MemberRole.MEMBER)
                .build();
        group.addMember(member);
        splitwiseGroupRepository.save(group);
        activityService.logMemberAdded(userId, group, group.getCreatedBy());
        log.info("Added user {} to group {} (trip {})", userId, group.getId(), tripId);
    }

    /**
     * Gets a group by ID. Caller must be a member.
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroup(UUID groupId, UUID currentUserId) {
        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!splitwiseGroupRepository.isUserMemberOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        return toGroupResponse(group);
    }

    /**
     * Updates group (admin only). Only description is updated from request.
     */
    public GroupResponse updateGroup(UUID groupId, CreateGroupRequest request, UUID currentUserId) {
        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        splitwiseGroupRepository.save(group);
        activityService.logGroupUpdated(group, currentUserId);
        log.info("Updated group {}", groupId);
        return toGroupResponse(group);
    }

    /**
     * Deletes a group (admin only).
     */
    public void deleteGroup(UUID groupId, UUID currentUserId) {
        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        activityService.logGroupDeleted(group, currentUserId);
        splitwiseGroupRepository.delete(group);
        log.info("Deleted group {}", groupId);
    }

    /**
     * Adds members to the group (admin only).
     */
    public GroupResponse addMembers(UUID groupId, AddGroupMemberRequest request, UUID currentUserId) {
        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        GroupMember.MemberRole role = request.getRole() != null ? request.getRole() : GroupMember.MemberRole.MEMBER;
        for (UUID memberId : request.getMemberIds()) {
            if (group.isMember(memberId)) continue;
            GroupMember member = GroupMember.builder()
                    .group(group)
                    .userId(memberId)
                    .role(role)
                    .build();
            group.addMember(member);
            activityService.logMemberAdded(memberId, group, currentUserId);
        }
        splitwiseGroupRepository.save(group);
        log.info("Added members to group {}", groupId);
        return toGroupResponse(group);
    }

    /**
     * Removes a member from the group (admin only).
     */
    public GroupResponse removeMember(UUID groupId, UUID memberId, UUID currentUserId) {
        SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
            throw new UserNotMemberException(currentUserId, groupId);
        }
        Optional<GroupMember> toRemove = group.getMembers().stream()
                .filter(m -> m.getUserId().equals(memberId))
                .findFirst();
        if (toRemove.isPresent()) {
            group.removeMember(toRemove.get());
            splitwiseGroupRepository.save(group);
            activityService.logMemberRemoved(memberId, group, currentUserId);
            log.info("Removed member {} from group {}", memberId, groupId);
        }
        return toGroupResponse(group);
    }

    /**
     * Lists groups where the current user is a member.
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(UUID currentUserId) {
        List<SplitwiseGroup> groups = splitwiseGroupRepository.findByUserId(currentUserId);
        return groups.stream().map(this::toGroupResponse).collect(Collectors.toList());
    }

    private GroupResponse toGroupResponse(SplitwiseGroup group) {
        String name = tripRepository.findById(group.getTripId())
                .map(TripEntity::getTripTitle)
                .orElse(group.getDescription());
        UserResponse createdByResponse = toUserResponse(group.getCreatedBy());
        List<UserResponse> memberResponses = group.getMembers() == null
                ? new ArrayList<>()
                : group.getMembers().stream()
                .map(m -> toUserResponse(m.getUserId()))
                .collect(Collectors.toList());
        return GroupResponse.builder()
                .id(group.getId())
                .tripId(group.getTripId())
                .name(name)
                .description(group.getDescription())
                .createdBy(createdByResponse)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .members(memberResponses)
                .build();
    }

    private UserResponse toUserResponse(UUID userId) {
        if (userId == null) return null;
        UsersEntity user = userRepository.findUserByUserUuid(userId).orElse(null);
        if (user == null) return UserResponse.builder().userUuid(userId).build();
        String name = "";
        if (user.getUserProfileEntity() != null) {
            UserProfileEntity p = user.getUserProfileEntity();
            name = (p.getFirstName() != null ? p.getFirstName() : "") + " " + (p.getLastName() != null ? p.getLastName() : "");
        }
        return UserResponse.builder()
                .userUuid(user.getUserUuid())
                .name(name.trim().isEmpty() ? null : name.trim())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .build();
    }
}
