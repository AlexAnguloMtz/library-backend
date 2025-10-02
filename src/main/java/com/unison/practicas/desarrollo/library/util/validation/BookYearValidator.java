package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class BookYearValidator implements ConstraintValidator<BookYear, Integer> {

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true;
        }
        int currentYear = LocalDate.now().getYear();
        return year >= 1 && year <= currentYear;
    }

}