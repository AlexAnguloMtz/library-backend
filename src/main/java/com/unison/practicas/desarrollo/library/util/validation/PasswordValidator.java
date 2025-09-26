package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int length = value.length();
        return length >= MIN_LENGTH && length <= MAX_LENGTH;
    }
}