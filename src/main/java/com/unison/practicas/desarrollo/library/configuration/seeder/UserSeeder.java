package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.factory.UserFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
class UserSeeder extends BaseSeeder<User> {

    private final UserFactory userFactory;
    private final UserRepository userRepository;

    public UserSeeder(UserFactory userFactory, UserRepository userRepository) {
        this.userFactory = userFactory;
        this.userRepository = userRepository;
    }

    @Override
    long countExisting() {
        return userRepository.count();
    }

    @Override
    String resourceName() {
        return "users";
    }

    @Override
    void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }

    @Override
    List<User> makeItems(int count) {
        return userFactory.createUsers(count);
    }

}