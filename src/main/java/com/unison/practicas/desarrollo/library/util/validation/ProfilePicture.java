package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ProfilePictureValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ProfilePicture {

    String message() default "Imagen inválida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxSize() default 2_000_000; // 2MB por defecto
}
