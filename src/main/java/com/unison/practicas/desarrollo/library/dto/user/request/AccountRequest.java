package com.unison.practicas.desarrollo.library.dto.user.request;

import com.unison.practicas.desarrollo.library.util.validation.ProfilePicture;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record AccountRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 40)
        String roleId,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @NotNull
        @ProfilePicture
        MultipartFile profilePicture

) {
}