package com.unison.practicas.desarrollo.library.dto.user.request;

import com.unison.practicas.desarrollo.library.util.validation.Password;
import com.unison.practicas.desarrollo.library.util.validation.ProfilePicture;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record CreateAccountRequest(

        @NotBlank
        @Size(max = 100)
        @Email
        String email,

        @NotBlank
        @Size(max = 40)
        String roleId,

        @NotBlank
        @Password
        String password,

        @ProfilePicture
        MultipartFile profilePicture

) {
}