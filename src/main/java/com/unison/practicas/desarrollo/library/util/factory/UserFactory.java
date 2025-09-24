package com.unison.practicas.desarrollo.library.util.factory;

import com.github.javafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import com.unison.practicas.desarrollo.library.util.TimeUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class UserFactory {

    private final Faker faker;
    private final RoleRepository roleRepository;

    public UserFactory(Faker faker, RoleRepository roleRepository) {
        this.faker = faker;
        this.roleRepository = roleRepository;
    }

    public Collection<User> createUsers(int count) {
        if (count < 0) {
            throw new RuntimeException("Count must be greater than 0, got %d".formatted(count));
        }

        List<Role> roles = roleRepository.findAll();

        return IntStream.range(1, count + 1)
                .mapToObj(i -> createUser(i, CollectionHelpers.randomItem(roles)))
                .toList();
    }

    private User createUser(int seed, Role role) {
        var user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setPhoneNumber(faker.phoneNumber().cellPhone());
        user.setEmail(generateUniqueEmail(seed));
        user.setPasswordHash(faker.internet().password(8, 16));
        user.setRoles(Set.of(role));
        user.setRegistrationDate(TimeUtils.randomInstantBetween(Instant.parse("2020-01-24T00:00:00Z"), Instant.parse("2025-09-24T00:00:00Z")));
        user.setProfilePictureUrl("http://localhost:8080/api/v1/users/profile-pictures/%s".formatted(CollectionHelpers.randomItem(profilePictures())));

        return user;
    }

    private String generateUniqueEmail(int seed) {
        String email = faker.internet().emailAddress();
        String[] parts = email.split("@");
        return parts[0] + "_" + seed + "@" + parts[1];
    }

    private List<String> profilePictures() {
        return List.of(
                "profile_1.jpg",
                "profile_2.jpg",
                "profile_3.jpg",
                "profile_4.jpg",
                "profile_5.jpg",
                "profile_6.jpg"
        );
    }

}