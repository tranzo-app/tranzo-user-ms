package com.tranzo.tranzo_user_ms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class VerifyOtpResponseDto {
    private boolean userExists;
}
