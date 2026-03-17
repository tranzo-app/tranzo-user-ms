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
        log.debug("Received request to get balances for group: {}", groupId);
        
        List<BalanceResponse> response = balanceService.getGroupBalances(groupId);
        
        log.debug("Retrieved balances for {} users in group {}", response.size(), groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets balance summary for a specific user in a group.
     */
    @GetMapping("/group/{groupId}/user/{userId}")
    public ResponseEntity<BalanceResponse> getUserBalanceInGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        
        log.debug("Received request to get balance for user {} in group: {}", userId, groupId);
        
        BalanceResponse response = balanceService.getUserBalanceInGroup(groupId, userId);
        
        log.debug("Retrieved balance for user {} in group {}: net={}", 
                 userId, groupId, response.getNetBalance());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets balance for the current user in a group.
     */
    @GetMapping("/group/{groupId}/my-balance")
    public ResponseEntity<BalanceResponse> getMyBalanceInGroup(
            @PathVariable UUID groupId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get balance for current user in group: {}", groupId);
        BalanceResponse response = balanceService.getUserBalanceInGroup(groupId, userId);
        
        log.debug("Retrieved balance for current user in group {}: net={}", 
                 groupId, response.getNetBalance());
        return ResponseEntity.ok(response);
    }
}
