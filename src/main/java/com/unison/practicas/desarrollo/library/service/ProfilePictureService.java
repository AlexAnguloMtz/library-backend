package com.unison.practicas.desarrollo.library.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {

    String saveProfilePicture(MultipartFile imageFile);

    void deleteProfilePicture(String key);

    String profilePictureUrl(String key);

}