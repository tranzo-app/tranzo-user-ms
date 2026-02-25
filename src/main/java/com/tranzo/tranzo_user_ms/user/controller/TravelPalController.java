package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.user.service.TravelPalService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/travel-pal")
@RequiredArgsConstructor
public class TravelPalController {
    private final TravelPalService service;

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendRequest(@PathVariable UUID receiverId) throws AuthException {
        UUID requesterId = SecurityUtils.getCurrentUserUuid();
        service.sendRequest(requesterId, receiverId);
        return ResponseEntity.ok("Request sent");
    }

    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<?> accept(@PathVariable UUID requesterId) throws AuthException {
        UUID receiverId = SecurityUtils.getCurrentUserUuid();
        service.acceptRequest(receiverId, requesterId);
        return ResponseEntity.ok("Request accepted");
    }

    @PostMapping("/reject/{requesterId}")
    public ResponseEntity<?> reject(@PathVariable UUID requesterId) throws AuthException {
        UUID receiverId = SecurityUtils.getCurrentUserUuid();
        service.rejectRequest(receiverId, requesterId);
        return ResponseEntity.ok("Request rejected");
    }

    @DeleteMapping("/{otherUserId}")
    public ResponseEntity<?> remove(@PathVariable UUID otherUserId) throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        service.removeTravelPal(userId, otherUserId);
        return ResponseEntity.ok("Removed");
    }

    @GetMapping("/my")
    public ResponseEntity<List<UUID>> myPals() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(service.getMyTravelPals(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending() throws AuthException {
        UUID userId = SecurityUtils.getCurrentUserUuid();
        return ResponseEntity.ok(service.getIncomingPendingRequests(userId));
    }
}
