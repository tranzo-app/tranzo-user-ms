package com.tranzo.tranzo_user_ms.dto;

import com.tranzo.tranzo_user_ms.enums.SocialHandle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialHandleDto {

    @NotBlank(message = "Platform is required")
    private SocialHandle platform;

    @NotBlank(message = "URL is required")
    @Size(max = 250, message = "URL cannot exceed 250 characters")
    private String url;
}
