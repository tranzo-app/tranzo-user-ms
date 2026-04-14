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
        log.info("Incoming request | API=/api/splitwise/settlements | method=POST | userId={} | paidById={} | paidToId={} | amount={}", 
                 userId, request.getPaidById(), request.getPaidToId(), request.getAmount());
        
        try {
            SettlementResponse response = settlementService.createSettlement(request, userId);
            
            log.info("Settlement created | userId={} | settlementId={} | status=SUCCESS", userId, response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/settlements | method=POST | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets a settlement by ID.
     */
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable UUID settlementId) {
        log.info("Incoming request | API=/api/splitwise/settlements/{} | method=GET", settlementId);
        
        try {
            SettlementResponse response = settlementService.getSettlement(settlementId);
            
            log.info("Settlement retrieved | settlementId={} | status=SUCCESS", settlementId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/settlements/{} | method=GET | reason={}", settlementId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets all settlements for a group.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(@PathVariable UUID groupId) {
        log.info("Incoming request | API=/api/splitwise/settlements/group/{} | method=GET", groupId);
        
        try {
            List<SettlementResponse> response = settlementService.getGroupSettlements(groupId);
            
            log.info("Group settlements retrieved | groupId={} | settlementsCount={} | status=SUCCESS", groupId, response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/settlements/group/{} | method=GET | reason={}", groupId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets settlements for the current user.
     */
    @GetMapping("/my-settlements")
    public ResponseEntity<List<SettlementResponse>> getUserSettlements() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/api/splitwise/settlements/my-settlements | method=GET | userId={}", userId);
        
        try {
            List<SettlementResponse> response = settlementService.getUserSettlements(userId);
            
            log.info("User settlements retrieved | userId={} | settlementsCount={} | status=SUCCESS", userId, response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/settlements/my-settlements | method=GET | userId={} | reason={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets optimized settlement proposals for a group.
     */
    @GetMapping("/optimize/{groupId}")
    public ResponseEntity<List<SettlementProposal>> getOptimizedSettlements(@PathVariable UUID groupId) {
        log.info("Incoming request | API=/api/splitwise/settlements/optimize/{} | method=GET", groupId);
        
        try {
            List<SettlementProposal> response = settlementService.getOptimizedSettlements(groupId);
            
            log.info("Optimized settlements generated | groupId={} | proposalsCount={} | status=SUCCESS", groupId, response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Request failed | API=/api/splitwise/settlements/optimize/{} | method=GET | reason={}", groupId, e.getMessage(), e);
            throw e;
        }
    }
}
