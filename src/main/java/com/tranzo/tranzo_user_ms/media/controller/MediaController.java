package com.tranzo.tranzo_user_ms.media.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.media.dto.PresignedUrlResponseDto;
import com.tranzo.tranzo_user_ms.media.dto.UploadResponseDto;
import com.tranzo.tranzo_user_ms.media.exception.S3MediaNotConfiguredException;
import com.tranzo.tranzo_user_ms.media.service.S3MediaService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final S3MediaService s3MediaService;

    /**
     * Upload a file. Authenticated. File is stored under uploads/media/{userId}/{uuid}.{ext}
     */
    @PostMapping("/upload")
    public ResponseEntity<ResponseDto<UploadResponseDto>> upload(@RequestParam("file") MultipartFile file) throws AuthException, IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.failure(400, "No file provided"));
        }
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/media/upload | method=POST | userId={} | fileName={}", userId, file.getOriginalFilename());
        
        try {
            UploadResponseDto result = s3MediaService.upload(file, userId.toString());
            log.info("File uploaded | userId={} | s3Key={} | status=SUCCESS", userId, result.getKey());
            return ResponseEntity.ok(ResponseDto.success(200, "File uploaded successfully", result));
        } catch (S3MediaNotConfiguredException e) {
            log.warn("Upload failed | operation=upload | userId={} | reason=S3_NOT_CONFIGURED", userId);
            return ResponseEntity.status(503)
                    .body(ResponseDto.failure(503, "Media storage is not configured"));
        }
    }

    /**
     * Get a presigned URL for an existing object key. Authenticated.
     * Query param: key (required). Optional: expiryMinutes (default from config).
     */
    @GetMapping("/url")
    public ResponseEntity<ResponseDto<PresignedUrlResponseDto>> getPresignedUrl(
            @RequestParam("key") String key,
            @RequestParam(value = "expiryMinutes", required = false) Integer expiryMinutes) throws AuthException {
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.failure(400, "Query param 'key' is required"));
        }
        UUID userId = SecurityUtils.getCurrentUserUuid();
        log.info("Incoming request | API=/media/url | method=GET | userId={} | key={}", userId, key);
        
        try {
            PresignedUrlResponseDto result = s3MediaService.getPresignedUrl(key, expiryMinutes);
            log.info("Presigned URL generated | userId={} | key={} | status=SUCCESS", userId, key);
            return ResponseEntity.ok(ResponseDto.success(200, "Presigned URL generated", result));
        } catch (S3MediaNotConfiguredException e) {
            log.warn("Presigned URL failed | operation=getPresignedUrl | userId={} | reason=S3_NOT_CONFIGURED", userId);
            return ResponseEntity.status(503)
                    .body(ResponseDto.failure(503, "Media storage is not configured"));
        }
    }
}
