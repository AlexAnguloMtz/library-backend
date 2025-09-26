package com.unison.practicas.desarrollo.library.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginForm(
        @NotBlank
        @Length(min = 1, max = 100)
        @Email
        String email,

        @NotBlank
        @Length(min = 8, max = 100)
        String password
) {
}