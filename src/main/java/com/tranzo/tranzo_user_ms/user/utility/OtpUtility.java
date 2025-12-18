package com.tranzo.tranzo_user_ms.user.utility;

import com.tranzo.tranzo_user_ms.user.dto.IdentifierAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class OtpUtility {

    public String generateOtp()
    {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }

    public String resolveIdentifier(IdentifierAware otpDto)
    {
        if (otpDto.getEmailId() != null)
        {
            return otpDto.getEmailId().toLowerCase();
        }
        return otpDto.getCountryCode() + otpDto.getMobileNumber();
    }
}
