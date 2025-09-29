package com.unison.practicas.desarrollo.library.configuration.beans;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfigurerImpl implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userProfilePicturesPath = Paths.get("data/users/profile-pictures").toAbsolutePath().toString();
        registry.addResourceHandler("/api/v1/users/profile-pictures/**")
                .addResourceLocations("file:" + userProfilePicturesPath + "/");

        String booksImagesPath = Paths.get("data/books/images").toAbsolutePath().toString();
        registry.addResourceHandler("/api/v1/books/images/**")
                .addResourceLocations("file:" + booksImagesPath + "/");
    }

}