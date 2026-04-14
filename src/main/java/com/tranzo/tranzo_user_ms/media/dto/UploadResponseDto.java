package com.tranzo.tranzo_user_ms.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponseDto {
    /** S3 object key (use this to get presigned URL or delete). */
    private String key;
    /** Optional presigned GET URL for immediate access (expires after configured duration). */
    private String presignedUrl;
}
