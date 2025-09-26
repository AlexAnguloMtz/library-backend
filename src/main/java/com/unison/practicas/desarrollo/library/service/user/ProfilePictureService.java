package com.unison.practicas.desarrollo.library.service.user;

import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {

    String saveProfilePicture(MultipartFile imageFile);

    void deleteProfilePicture(String key);

    String profilePictureUrl(String key);

}