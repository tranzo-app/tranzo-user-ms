package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.PublicProfileResponseDto;
import com.tranzo.tranzo_user_ms.user.service.PublicProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/public/profile")
@Slf4j
@RequiredArgsConstructor
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDto<PublicProfileResponseDto>> getPublicProfile(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PublicProfileResponseDto profile = publicProfileService.getPublicProfile(userId, page, size);
        return ResponseEntity.ok(ResponseDto.success(200, "Public profile fetched", profile));
    }
}
