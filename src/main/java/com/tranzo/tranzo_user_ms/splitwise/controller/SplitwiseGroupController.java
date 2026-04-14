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
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups | method=POST | userId={} | groupName={}", userId, request.getName());
        
        try {
            GroupResponse response = groupService.createGroup(request, userId);
            
            log.info("Group created | userId={} | groupId={} | status=SUCCESS", userId, response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups | method=POST | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a group by ID.
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/{} | method=GET | userId={}", groupId, userId);
        
        try {
            GroupResponse response = groupService.getGroup(groupId, userId);
            
            log.info("Group retrieved | userId={} | groupId={} | status=SUCCESS", userId, groupId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/{} | method=GET | userId={} | reason={}", groupId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates a group.
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable UUID groupId, @Valid @RequestBody CreateGroupRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/{} | method=PUT | userId={} | groupName={}", groupId, userId, request.getName());
        
        try {
            GroupResponse response = groupService.updateGroup(groupId, request, userId);
            
            log.info("Group updated | userId={} | groupId={} | status=SUCCESS", userId, groupId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/{} | method=PUT | userId={} | reason={}", groupId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a group.
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/{} | method=DELETE | userId={}", groupId, userId);
        
        try {
            groupService.deleteGroup(groupId, userId);
            
            log.info("Group deleted | userId={} | groupId={} | status=SUCCESS", userId, groupId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/{} | method=DELETE | userId={} | reason={}", groupId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all groups for the current user.
     */
    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getUserGroups() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/my-groups | method=GET | userId={}", userId);
        
        try {
            List<GroupResponse> response = groupService.getUserGroups(userId);
            
            log.info("User groups retrieved | userId={} | groupsCount={} | status=SUCCESS", userId, response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/my-groups | method=GET | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Adds members to a group.
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMembers(@PathVariable UUID groupId, @Valid @RequestBody AddGroupMemberRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/{}/members | method=POST | userId={} | membersCount={}", 
                 groupId, userId, request.getMemberIds().size());
        
        try {
            GroupResponse response = groupService.addMembers(groupId, request, userId);
            
            log.info("Members added | userId={} | groupId={} | membersCount={} | status=SUCCESS", userId, groupId, request.getMemberIds().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/{}/members | method=POST | userId={} | reason={}", groupId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Removes a member from a group.
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<GroupResponse> removeMember(@PathVariable UUID groupId, @PathVariable UUID memberId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/{}/members/{} | method=DELETE | userId={}", groupId, memberId, userId);
        
        try {
            GroupResponse response = groupService.removeMember(groupId, memberId, userId);
            
            log.info("Member removed | userId={} | groupId={} | memberId={} | status=SUCCESS", userId, groupId, memberId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/{}/members/{} | method=DELETE | userId={} | reason={}", groupId, memberId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all members of a group.
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> getGroupMembers(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/groups/{}/members | method=GET | userId={}", groupId, userId);
        
        try {
            GroupResponse group = groupService.getGroup(groupId, userId);
            
            int membersCount = group.getMembers() != null ? group.getMembers().size() : 0;
            log.info("Group members retrieved | userId={} | groupId={} | membersCount={} | status=SUCCESS", userId, groupId, membersCount);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/groups/{}/members | method=GET | userId={} | reason={}", groupId, userId, e.getMessage(), e);
            throw e;
        }
    }
}
