package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserDateOfBirthValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UserDateOfBirth {
    String message() default "La fecha de nacimiento debe estar entre 1900-01-01 y hasta el día de ayer";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

