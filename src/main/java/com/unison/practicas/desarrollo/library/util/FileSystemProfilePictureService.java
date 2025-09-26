package com.unison.practicas.desarrollo.library.util;

import com.unison.practicas.desarrollo.library.service.user.ProfilePictureService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileSystemProfilePictureService implements ProfilePictureService {

    private final FileSystemImageService imageService;
    private final Path rootFolder;

    public FileSystemProfilePictureService(FileSystemImageService imageService) {
        this.imageService = imageService;
        this.rootFolder = Paths.get("data/users/profile-pictures").toAbsolutePath();
    }

    @Override
    public String saveProfilePicture(MultipartFile imageFile) {
        return imageService.saveImage(imageFile, rootFolder);
    }

    @Override
    public String profilePictureUrl(String key) {
        String publicUrlPrefix = "http://localhost:8080/api/v1/users/profile-pictures/";
        return publicUrlPrefix + key;
    }

    @Override
    public void deleteProfilePicture(String key) {
        imageService.deleteImage(key, rootFolder);
    }

}
