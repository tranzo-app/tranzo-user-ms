package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.AddGroupMemberRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateGroupRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.SplitwiseGroupService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Splitwise groups.
 */
@Slf4j
@RestController
@RequestMapping("/api/splitwise/groups")
@RequiredArgsConstructor
@Validated
public class SplitwiseGroupController {

    private final SplitwiseGroupService groupService;

    /**
     * Creates a new group.
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to create group: {}", request.getName());
        GroupResponse response = groupService.createGroup(request, userId);
        
        log.info("Successfully created group with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a group by ID.
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get group: {}", groupId);
        
        GroupResponse response = groupService.getGroup(groupId, userId);
        
        log.debug("Successfully retrieved group: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a group.
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateGroupRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to update group: {}", groupId);
        GroupResponse response = groupService.updateGroup(groupId, request, userId);
        
        log.info("Successfully updated group: {}", groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a group.
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to delete group: {}", groupId);
        groupService.deleteGroup(groupId, userId);
        
        log.info("Successfully deleted group: {}", groupId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets all groups for the current user.
     */
    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getUserGroups() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get groups for current user");
        List<GroupResponse> response = groupService.getUserGroups(userId);
        
        log.debug("Retrieved {} groups for user: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds members to a group.
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMembers(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to add {} members to group: {}", 
                 request.getMemberIds().size(), groupId);
        GroupResponse response = groupService.addMembers(groupId, request, userId);
        
        log.info("Successfully added members to group: {}", groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a member from a group.
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<GroupResponse> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to remove member {} from group: {}", memberId, groupId);
        GroupResponse response = groupService.removeMember(groupId, memberId, userId);
        
        log.info("Successfully removed member {} from group: {}", memberId, groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all members of a group.
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> getGroupMembers(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get members for group: {}", groupId);
        
        GroupResponse group = groupService.getGroup(groupId, userId);
        
        log.debug("Retrieved {} members for group: {}", group.getMembers() != null ? group.getMembers().size() : 0, groupId);
        return ResponseEntity.ok(group);
    }
}
