package com.unison.practicas.desarrollo.library.configuration.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev"})
public class DevelopmentSeeder implements CommandLineRunner {

    private final UserSeeder userSeeder;

    protected DevelopmentSeeder(UserSeeder userSeeder) {
        this.userSeeder = userSeeder;
    }

    @Override
    public void run(String... args) throws Exception {
        userSeeder.seed();
    }

}