package com.tranzo.tranzo_user_ms.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpData {
    private String plainOtp;
    private String otpHash;
    private int attempts;
    private long sentAt;
}
