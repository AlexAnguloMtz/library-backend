package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ZipCodeValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ZipCode {

    String message() default "El código postal debe tener exactamente 5 dígitos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}