package com.tranzo.tranzo_user_ms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlDto {
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String url;
}
