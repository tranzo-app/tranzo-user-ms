package com.tranzo.tranzo_user_ms.utility;

import com.tranzo.tranzo_user_ms.dto.VerifyOtpDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VerifyEitherEmailOrMobileValidator implements ConstraintValidator<VerifyEitherEmailOrMobile, VerifyOtpDto> {
    @Override
    public boolean isValid(VerifyOtpDto verifyOtpDto, ConstraintValidatorContext context) {
        boolean result = false;
        if (verifyOtpDto.getCountryCode() != null && !verifyOtpDto.getCountryCode().isBlank() && verifyOtpDto.getMobileNumber() != null && !verifyOtpDto.getMobileNumber().isBlank())
        {
            result = verifyOtpDto.getEmailId() == null || verifyOtpDto.getEmailId().isBlank();
        }
        else if (verifyOtpDto.getEmailId() != null && !verifyOtpDto.getEmailId().isBlank())
        {
            result = (verifyOtpDto.getCountryCode() == null || verifyOtpDto.getCountryCode().isBlank()) && (verifyOtpDto.getMobileNumber() == null || verifyOtpDto.getMobileNumber().isBlank());
        }
        if (!result)
        {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(
                            "Either (country code and mobile number) or email id must be provided, not both"
                    )
                    .addConstraintViolation();
        }
        return result;
    }
}
