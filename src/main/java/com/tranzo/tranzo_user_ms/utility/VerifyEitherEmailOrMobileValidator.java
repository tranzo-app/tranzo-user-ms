package com.tranzo.tranzo_user_ms.utility;

import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VerifyEitherEmailOrMobileValidator implements ConstraintValidator<VerifyEitherEmailOrMobile, VerifyOtpDto> {
    @Override
    public boolean isValid(VerifyOtpDto verifyOtpDto, ConstraintValidatorContext constraintValidatorContext) {
        boolean hasMobile = verifyOtpDto.getMobileNumber() != null && !verifyOtpDto.getMobileNumber().isBlank();
        boolean hasEmail = verifyOtpDto.getEmailId() != null && !verifyOtpDto.getEmailId().isBlank();
        return hasMobile ^ hasEmail;
    }
}
