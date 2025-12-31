package com.tranzo.tranzo_user_ms.trip.utility;

import com.tranzo.tranzo_user_ms.trip.NotEmptyIfPresentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyIfPresentValidator.class)
@Documented
public @interface NotEmptyIfPresent {
    String message() default "Map must not be empty if provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
