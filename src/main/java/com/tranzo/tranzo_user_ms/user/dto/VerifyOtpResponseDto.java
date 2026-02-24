package com.tranzo.tranzo_user_ms.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpResponseDto {
    private boolean userExists;
    private String registrationToken;
}
