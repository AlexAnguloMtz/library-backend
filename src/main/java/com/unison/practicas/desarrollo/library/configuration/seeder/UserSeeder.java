package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.factory.UserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
@Slf4j
public class UserSeeder {

    private final UserFactory userFactory;
    private final UserRepository userRepository;

    public UserSeeder(UserFactory userFactory, UserRepository userRepository) {
        this.userFactory = userFactory;
        this.userRepository = userRepository;
    }

    public void seed() {
        if (userRepository.count() > 0) {
            log.debug("user table not empty, will skip seeding of users");
            return;
        }
        int count = 500;

        log.debug("seeding {} users...", count);

        userRepository.saveAll(userFactory.createUsers(count));

        log.debug("seeded {} users", count);
    }

}