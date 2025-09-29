package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class UserDateOfBirthValidator implements ConstraintValidator<UserDateOfBirth, LocalDate> {

    private static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);

        return !value.isBefore(MIN_DATE) && !value.isAfter(yesterday);
    }
}

