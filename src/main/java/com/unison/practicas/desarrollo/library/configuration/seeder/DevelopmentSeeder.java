package com.unison.practicas.desarrollo.library.configuration.seeder;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev"})
@Slf4j
class DevelopmentSeeder implements CommandLineRunner {

    private final UserSeeder userSeeder;
    private final DemoUsersSeeder demoUsersSeeder;
    private final AuthorSeeder authorSeeder;
    private final BookSeeder bookSeeder;

    DevelopmentSeeder(UserSeeder userSeeder, DemoUsersSeeder demoUsersSeeder, AuthorSeeder authorSeeder, BookSeeder bookSeeder) {
        this.userSeeder = userSeeder;
        this.demoUsersSeeder = demoUsersSeeder;
        this.authorSeeder = authorSeeder;
        this.bookSeeder = bookSeeder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            log.debug("seeding data for development...");
            seed();
            log.debug("seeding of data for development was successful");
        } catch (Exception e) {
            log.debug("could not seed data for development");
            log.debug(e.getMessage());
        }
    }

    private void seed() {
        userSeeder.seed(500);
        demoUsersSeeder.seed();
        authorSeeder.seed(300);
        bookSeeder.seed(600);
    }

}