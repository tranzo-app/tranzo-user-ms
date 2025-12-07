package com.tranzo.tranzo_user_ms.utility;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VerifyEitherEmailOrMobileValidator.class)
public @interface VerifyEitherEmailOrMobile {
    String message() default "Either email id or mobile number must be provided, but not both";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
