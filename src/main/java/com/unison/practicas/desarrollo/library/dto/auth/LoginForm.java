package com.unison.practicas.desarrollo.library.dto.auth;

import com.unison.practicas.desarrollo.library.util.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginForm(
        @NotBlank
        @Length(min = 1, max = 100)
        @Email
        String email,

        @NotBlank
        @Password
        String password
) {
}