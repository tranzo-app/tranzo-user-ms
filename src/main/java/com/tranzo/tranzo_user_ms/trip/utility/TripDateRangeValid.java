package com.tranzo.tranzo_user_ms.trip.utility;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TripDateRangeValidator.class)
@Documented
public @interface TripDateRangeValid {
    String message() default "Trip end date must be after start date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
