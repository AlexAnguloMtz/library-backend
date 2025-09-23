package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.factory.UserFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class UserSeeder {

    private final UserFactory userFactory;
    private final UserRepository userRepository;

    public UserSeeder(UserFactory userFactory, UserRepository userRepository) {
        this.userFactory = userFactory;
        this.userRepository = userRepository;
    }

    public void seed() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.saveAll(userFactory.createUsers(500));
    }

}