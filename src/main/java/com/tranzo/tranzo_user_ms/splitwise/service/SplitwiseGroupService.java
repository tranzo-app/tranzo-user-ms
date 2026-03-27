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
        log.info("Processing started | operation=createGroup | userId={} | groupName={}", currentUserId, request.getName());

        try {
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
            
            log.info("Calling external service | service=ActivityService | operation=logGroupCreated | groupId={}", group.getId());
            activityService.logGroupCreated(currentUser != null ? currentUser : null, group);
            
            log.info("Processing completed | operation=createGroup | userId={} | groupId={} | status=SUCCESS", currentUserId, group.getId());
            return toGroupResponse(group);
        } catch (Exception e) {
            log.error("Operation failed | operation=createGroup | userId={} | reason={}", currentUserId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a Splitwise group for a trip (event-driven). Host is added as ADMIN.
     */
    public GroupResponse createGroupForTrip(UUID tripId, String name, UUID hostUserId) {
        log.info("Processing started | operation=createGroupForTrip | tripId={} | hostUserId={}", tripId, hostUserId);

        try {
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
            
            log.info("Calling external service | service=ActivityService | operation=logGroupCreated | groupId={}", group.getId());
            activityService.logGroupCreated(host != null ? host : null, group);
            
            log.info("Processing completed | operation=createGroupForTrip | tripId={} | groupId={} | status=SUCCESS", tripId, group.getId());
            return toGroupResponse(group);
        } catch (Exception e) {
            log.error("Operation failed | operation=createGroupForTrip | tripId={} | reason={}", tripId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Adds a user to the group associated with the given trip (e.g. on participant join).
     */
    public void addMemberToGroupByTripId(UUID tripId, UUID userId) {
        log.info("Processing started | operation=addMemberToGroupByTripId | tripId={} | userId={}", tripId, userId);

        try {
            SplitwiseGroup group = splitwiseGroupRepository.findByTripId(tripId)
                    .orElseThrow(() -> new GroupNotFoundException("No group found for trip " + tripId));
            if (group.isMember(userId)) {
                log.info("User already member | operation=addMemberToGroupByTripId | tripId={} | userId={} | status=NOOP", tripId, userId);
                return;
            }
            GroupMember member = GroupMember.builder()
                    .group(group)
                    .userId(userId)
                    .role(GroupMember.MemberRole.MEMBER)
                    .build();
            group.addMember(member);
            splitwiseGroupRepository.save(group);
            
            log.info("Calling external service | service=ActivityService | operation=logMemberAdded | groupId={}", group.getId());
            activityService.logMemberAdded(userId, group, group.getCreatedBy());
            
            log.info("Processing completed | operation=addMemberToGroupByTripId | tripId={} | userId={} | groupId={} | status=SUCCESS", tripId, userId, group.getId());
        } catch (GroupNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=addMemberToGroupByTripId | tripId={} | userId={} | reason={}", tripId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a group by ID. Caller must be a member.
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroup(UUID groupId, UUID currentUserId) {
        log.info("Processing started | operation=getGroup | groupId={} | userId={}", groupId, currentUserId);

        try {
            SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId));
            if (!splitwiseGroupRepository.isUserMemberOfGroup(groupId, currentUserId)) {
                log.warn("Access denied | operation=getGroup | groupId={} | userId={} | reason=NOT_MEMBER", groupId, currentUserId);
                throw new UserNotMemberException(currentUserId, groupId);
            }
            
            log.info("Processing completed | operation=getGroup | groupId={} | userId={} | status=SUCCESS", groupId, currentUserId);
            return toGroupResponse(group);
        } catch (GroupNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=getGroup | groupId={} | userId={} | reason={}", groupId, currentUserId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates group (admin only). Only description is updated from request.
     */
    public GroupResponse updateGroup(UUID groupId, CreateGroupRequest request, UUID currentUserId) {
        log.info("Processing started | operation=updateGroup | groupId={} | userId={}", groupId, currentUserId);

        try {
            SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId));
            if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
                log.warn("Access denied | operation=updateGroup | groupId={} | userId={} | reason=NOT_ADMIN", groupId, currentUserId);
                throw new UserNotMemberException(currentUserId, groupId);
            }
            if (request.getDescription() != null) {
                group.setDescription(request.getDescription());
            }
            splitwiseGroupRepository.save(group);
            
            log.info("Calling external service | service=ActivityService | operation=logGroupUpdated | groupId={}", groupId);
            activityService.logGroupUpdated(group, currentUserId);
            
            log.info("Processing completed | operation=updateGroup | groupId={} | userId={} | status=SUCCESS", groupId, currentUserId);
            return toGroupResponse(group);
        } catch (GroupNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=updateGroup | groupId={} | userId={} | reason={}", groupId, currentUserId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a group (admin only).
     */
    public void deleteGroup(UUID groupId, UUID currentUserId) {
        log.info("Processing started | operation=deleteGroup | groupId={} | userId={}", groupId, currentUserId);

        try {
            SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId));
            if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
                log.warn("Access denied | operation=deleteGroup | groupId={} | userId={} | reason=NOT_ADMIN", groupId, currentUserId);
                throw new UserNotMemberException(currentUserId, groupId);
            }
            
            log.info("Calling external service | service=ActivityService | operation=logGroupDeleted | groupId={}", groupId);
            activityService.logGroupDeleted(group, currentUserId);
            splitwiseGroupRepository.delete(group);
            
            log.info("Processing completed | operation=deleteGroup | groupId={} | userId={} | status=SUCCESS", groupId, currentUserId);
        } catch (GroupNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=deleteGroup | groupId={} | userId={} | reason={}", groupId, currentUserId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Adds members to the group (admin only).
     */
    public GroupResponse addMembers(UUID groupId, AddGroupMemberRequest request, UUID currentUserId) {
        log.info("Processing started | operation=addMembers | groupId={} | userId={} | membersCount={}", groupId, currentUserId, request.getMemberIds().size());

        try {
            SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId));
            if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
                log.warn("Access denied | operation=addMembers | groupId={} | userId={} | reason=NOT_ADMIN", groupId, currentUserId);
                throw new UserNotMemberException(currentUserId, groupId);
            }
            GroupMember.MemberRole role = request.getRole() != null ? request.getRole() : GroupMember.MemberRole.MEMBER;
            int addedCount = 0;
            for (UUID memberId : request.getMemberIds()) {
                if (group.isMember(memberId)) continue;
                GroupMember member = GroupMember.builder()
                        .group(group)
                        .userId(memberId)
                        .role(role)
                        .build();
                group.addMember(member);
                
                log.info("Calling external service | service=ActivityService | operation=logMemberAdded | groupId={} | memberId={}", groupId, memberId);
                activityService.logMemberAdded(memberId, group, currentUserId);
                addedCount++;
            }
            splitwiseGroupRepository.save(group);
            
            log.info("Processing completed | operation=addMembers | groupId={} | userId={} | addedCount={} | status=SUCCESS", groupId, currentUserId, addedCount);
            return toGroupResponse(group);
        } catch (GroupNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=addMembers | groupId={} | userId={} | reason={}", groupId, currentUserId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Removes a member from the group (admin only).
     */
    public GroupResponse removeMember(UUID groupId, UUID memberId, UUID currentUserId) {
        log.info("Processing started | operation=removeMember | groupId={} | userId={} | memberId={}", groupId, currentUserId, memberId);

        try {
            SplitwiseGroup group = splitwiseGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId));
            if (!splitwiseGroupRepository.isUserAdminOfGroup(groupId, currentUserId)) {
                log.warn("Access denied | operation=removeMember | groupId={} | userId={} | reason=NOT_ADMIN", groupId, currentUserId);
                throw new UserNotMemberException(currentUserId, groupId);
            }
            Optional<GroupMember> toRemove = group.getMembers().stream()
                    .filter(m -> m.getUserId().equals(memberId))
                    .findFirst();
            if (toRemove.isPresent()) {
                group.removeMember(toRemove.get());
                splitwiseGroupRepository.save(group);
                
                log.info("Calling external service | service=ActivityService | operation=logMemberRemoved | groupId={} | memberId={}", groupId, memberId);
                activityService.logMemberRemoved(memberId, group, currentUserId);
                
                log.info("Processing completed | operation=removeMember | groupId={} | userId={} | memberId={} | status=SUCCESS", groupId, currentUserId, memberId);
            } else {
                log.info("Member not found | operation=removeMember | groupId={} | userId={} | memberId={} | status=NOOP", groupId, currentUserId, memberId);
            }
            return toGroupResponse(group);
        } catch (GroupNotFoundException | UserNotMemberException e) {
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=removeMember | groupId={} | userId={} | memberId={} | reason={}", groupId, currentUserId, memberId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lists groups where the current user is a member.
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(UUID currentUserId) {
        log.info("Processing started | operation=getUserGroups | userId={}", currentUserId);

        try {
            List<SplitwiseGroup> groups = splitwiseGroupRepository.findByUserId(currentUserId);
            List<GroupResponse> response = groups.stream().map(this::toGroupResponse).collect(Collectors.toList());
            
            log.info("Processing completed | operation=getUserGroups | userId={} | groupsCount={} | status=SUCCESS", currentUserId, response.size());
            return response;
        } catch (Exception e) {
            log.error("Operation failed | operation=getUserGroups | userId={} | reason={}", currentUserId, e.getMessage(), e);
            throw e;
        }
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
