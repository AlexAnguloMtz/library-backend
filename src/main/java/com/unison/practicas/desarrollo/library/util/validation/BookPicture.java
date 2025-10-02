package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = BookPictureValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface BookPicture {

    String message() default "Imagen inválida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxSize() default 3_000_000;
}

