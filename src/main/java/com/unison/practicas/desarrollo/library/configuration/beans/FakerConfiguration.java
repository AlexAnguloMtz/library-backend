package com.unison.practicas.desarrollo.library.configuration.beans;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
class FakerConfiguration {

    @Bean
    Faker faker() {
        return new Faker();
    }

}