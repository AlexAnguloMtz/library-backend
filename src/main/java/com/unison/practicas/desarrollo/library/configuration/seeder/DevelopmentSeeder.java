package com.unison.practicas.desarrollo.library.configuration.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev"})
public class DevelopmentSeeder implements CommandLineRunner {

    private final UserSeeder userSeeder;
    private final DemoUsersSeeder demoUsersSeeder;

    protected DevelopmentSeeder(UserSeeder userSeeder, DemoUsersSeeder demoUsersSeeder) {
        this.userSeeder = userSeeder;
        this.demoUsersSeeder = demoUsersSeeder;
    }

    @Override
    public void run(String... args) throws Exception {
        userSeeder.seed();
        demoUsersSeeder.seed();
    }

}