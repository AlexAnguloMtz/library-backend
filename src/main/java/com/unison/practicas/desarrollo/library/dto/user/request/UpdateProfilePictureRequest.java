package com.unison.practicas.desarrollo.library.dto.user.request;

import com.unison.practicas.desarrollo.library.util.validation.ProfilePicture;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record UpdateProfilePictureRequest(

        @NotNull
        @ProfilePicture
        MultipartFile profilePicture

) { }