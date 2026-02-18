package com.tranzo.tranzo_user_ms.trip.utility;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.tranzo.tranzo_user_ms.trip.utility.NotEmptyIfPresent;

import java.util.Map;

public class NotEmptyIfPresentValidator
        implements ConstraintValidator<NotEmptyIfPresent, Map<?, ?>> {

    @Override
    public boolean isValid(Map<?, ?> value, ConstraintValidatorContext context) {
        return value == null || !value.isEmpty();
    }
}
