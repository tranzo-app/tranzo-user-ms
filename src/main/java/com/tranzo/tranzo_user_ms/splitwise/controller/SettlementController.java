package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateSettlementRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.SettlementResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.SettlementService;
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
 * REST controller for managing settlements.
 */
@Slf4j
@RestController
@RequestMapping("/api/splitwise/settlements")
@RequiredArgsConstructor
@Validated
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * Creates a new settlement.
     */
    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(@Valid @RequestBody CreateSettlementRequest request) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Received request to create settlement: {} -> {} amount {}", 
                 request.getPaidById(), request.getPaidToId(), request.getAmount());
        SettlementResponse response = settlementService.createSettlement(request, userId);
        log.info("Successfully created settlement with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a settlement by ID.
     */
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable UUID settlementId) {
        log.debug("Received request to get settlement: {}", settlementId);
        SettlementResponse response = settlementService.getSettlement(settlementId);
        log.debug("Successfully retrieved settlement: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all settlements for a group.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(@PathVariable UUID groupId) {
        log.debug("Received request to get settlements for group: {}", groupId);
        
        List<SettlementResponse> response = settlementService.getGroupSettlements(groupId);
        
        log.debug("Retrieved {} settlements for group: {}", response.size(), groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets settlements for the current user.
     */
    @GetMapping("/my-settlements")
    public ResponseEntity<List<SettlementResponse>> getUserSettlements() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.debug("Received request to get settlements for current user");
        List<SettlementResponse> response = settlementService.getUserSettlements(userId);
        log.debug("Retrieved {} settlements for user: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets optimized settlement proposals for a group.
     */
    @GetMapping("/optimize/{groupId}")
    public ResponseEntity<List<SettlementProposal>> getOptimizedSettlements(@PathVariable UUID groupId) {
        
        log.info("Received request to optimize settlements for group: {}", groupId);
        
        List<SettlementProposal> response = settlementService.getOptimizedSettlements(groupId);
        
        log.info("Generated {} optimized settlement proposals for group {}", response.size(), groupId);
        return ResponseEntity.ok(response);
    }
}
