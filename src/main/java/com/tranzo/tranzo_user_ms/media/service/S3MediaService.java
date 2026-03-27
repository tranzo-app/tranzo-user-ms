package com.tranzo.tranzo_user_ms.media.service;

import com.tranzo.tranzo_user_ms.media.dto.PresignedUrlResponseDto;
import com.tranzo.tranzo_user_ms.media.dto.UploadResponseDto;
import com.tranzo.tranzo_user_ms.media.exception.S3MediaNotConfiguredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3MediaService {

    private final S3Client s3Client;

    @Value("${app.s3.media-bucket:}")
    private String bucket;

    @Value("${app.s3.region:ap-south-1}")
    private String region;

    @Value("${app.s3.presigned-url-expiry-minutes:15}")
    private int presignedUrlExpiryMinutes;

    private static final String UPLOAD_PREFIX = "uploads/media/";

    private void ensureConfigured() {
        log.debug("Processing started | operation=ensureConfigured");
        
        if (bucket == null || bucket.isBlank()) {
            log.error("Configuration check failed | operation=ensureConfigured | reason=S3_BUCKET_NOT_CONFIGURED");
            throw new S3MediaNotConfiguredException("S3 media bucket is not configured (set AWS_S3_MEDIA_BUCKET)");
        }
        
        log.debug("Processing completed | operation=ensureConfigured | status=SUCCESS");
    }

    /**
     * Upload a file to S3. Key format: uploads/media/{userId}/{uuid}_{originalFilename}.
     */
    public UploadResponseDto upload(MultipartFile file, String userId) throws IOException {
        log.info("Processing started | operation=upload | userId={} | fileName={}", userId, file.getOriginalFilename());
        
        try {
            ensureConfigured();
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "file";
            }
            String ext = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0) {
                ext = originalFilename.substring(lastDot);
            }
            String key = UPLOAD_PREFIX + userId + "/" + UUID.randomUUID() + ext;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .contentLength(file.getSize())
                    .build();

            log.info("Calling external service | service=S3 | operation=putObject | bucket={} | key={}", bucket, key);
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File uploaded to S3 | userId={} | key={} | status=SUCCESS", userId, key);

            String presignedUrl = buildPresignedUrl(key, presignedUrlExpiryMinutes);
            log.info("Processing completed | operation=upload | userId={} | key={} | status=SUCCESS", userId, key);
            return UploadResponseDto.builder()
                    .key(key)
                    .presignedUrl(presignedUrl)
                    .build();
        } catch (IOException e) {
            log.error("Operation failed | operation=upload | userId={} | reason=IO_ERROR | error={}", userId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Operation failed | operation=upload | userId={} | reason=UNEXPECTED_ERROR | error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Generate a presigned GET URL for an existing object key.
     */
    public PresignedUrlResponseDto getPresignedUrl(String key, Integer expiryMinutes) {
        log.info("Processing started | operation=getPresignedUrl | key={}", key);
        
        try {
            ensureConfigured();
            int expiry = expiryMinutes != null && expiryMinutes > 0 ? expiryMinutes : presignedUrlExpiryMinutes;
            
            log.info("Calling external service | service=S3 | operation=presignUrl | key={} | expiryMinutes={}", key, expiry);
            String url = buildPresignedUrl(key, expiry);
            
            log.info("Processing completed | operation=getPresignedUrl | key={} | status=SUCCESS", key);
            return PresignedUrlResponseDto.builder()
                    .url(url)
                    .expiryMinutes(expiry)
                    .build();
        } catch (Exception e) {
            log.error("Operation failed | operation=getPresignedUrl | key={} | reason={}", key, e.getMessage(), e);
            throw e;
        }
    }

    private String buildPresignedUrl(String key, int expiryMinutes) {
        log.debug("Processing started | operation=buildPresignedUrl | key={} | expiryMinutes={}", key, expiryMinutes);
        
        try (S3Presigner presigner = S3Presigner.builder().region(Region.of(region)).build()) {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(expiryMinutes))
                            .getObjectRequest(getRequest)
                            .build()
            );
            String url = presignedRequest.url().toString();
            
            log.debug("Processing completed | operation=buildPresignedUrl | key={} | status=SUCCESS", key);
            return url;
        } catch (Exception e) {
            log.error("Operation failed | operation=buildPresignedUrl | key={} | reason={}", key, e.getMessage(), e);
            throw e;
        }
    }
}
