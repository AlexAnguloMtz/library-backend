package com.unison.practicas.desarrollo.library.configuration.beans;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class FakerConfiguration {

    @Bean
    public Faker faker() {
        return new Faker();
    }

}