package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.BalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.BalanceService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing balances.
 */
@Slf4j
@RestController
@RequestMapping("/api/splitwise/balances")
@RequiredArgsConstructor
@Validated
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * Gets balance summary for all users in a group.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<BalanceResponse>> getGroupBalances(@PathVariable UUID groupId) {
        log.info("Incoming request | API=/api/splitwise/balances/group/{} | method=GET", groupId);
        
        List<BalanceResponse> response = balanceService.getGroupBalances(groupId);
        
        log.info("Group balances retrieved | groupId={} | usersCount={} | status=SUCCESS", groupId, response.size());
        return ResponseEntity.ok(response);
    }
    /**
     * Gets balance summary for a specific user in a group.
     */
    @GetMapping("/group/{groupId}/user")
    public ResponseEntity<BalanceResponse> getUserBalanceInGroup(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/balances/group/{}/user | method=GET | userId={}", groupId, userId);
        
        BalanceResponse response = balanceService.getUserBalanceInGroup(groupId, userId);
        
        log.info("User balance retrieved | userId={} | groupId={} | netBalance={} | status=SUCCESS", userId, groupId, response.getNetBalance());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets balance for the current user in a group.
     */
    @GetMapping("/group/{groupId}/my-balance")
    public ResponseEntity<BalanceResponse> getMyBalanceInGroup(@PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/balances/group/{}/my-balance | method=GET | userId={}", groupId, userId);
        
        BalanceResponse response = balanceService.getUserBalanceInGroup(groupId, userId);
        
        log.info("My balance retrieved | userId={} | groupId={} | netBalance={} | status=SUCCESS", userId, groupId, response.getNetBalance());
        return ResponseEntity.ok(response);
    }
}
