package com.unison.practicas.desarrollo.library.util.factory;

import com.github.javafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.*;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import com.unison.practicas.desarrollo.library.util.TimeUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class UserFactory {

    private final Faker faker;
    private final RoleRepository roleRepository;
    private final GenderRepository genderRepository;
    private final UserAddressFactory userAddressFactory;

    public UserFactory(Faker faker, RoleRepository roleRepository, GenderRepository genderRepository, UserAddressFactory userAddressFactory) {
        this.faker = faker;
        this.roleRepository = roleRepository;
        this.genderRepository = genderRepository;
        this.userAddressFactory = userAddressFactory;
    }

    public Collection<User> createUsers(int count) {
        if (count < 0) {
            throw new RuntimeException("Count must be greater than 0, got %d".formatted(count));
        }

        Role adminRole = roleRepository.findBySlug(RoleName.ADMIN.name()).get();
        Role librarianRole = roleRepository.findBySlug(RoleName.LIBRARIAN.name()).get();
        Role userRole = roleRepository.findBySlug(RoleName.USER.name()).get();
        List<Role> roles = List.of(librarianRole, userRole);
        List<Gender> genders = genderRepository.findAll();

        var users = IntStream.range(1, count + 1)
                .mapToObj(i -> createUser(i, CollectionHelpers.randomItem(roles), CollectionHelpers.randomItem(genders)))
                .collect(Collectors.toList());

        User admin = createUser(Integer.MAX_VALUE, adminRole, CollectionHelpers.randomItem(genders));

        users.add(admin);

        return users;
    }

    private User createUser(int seed, Role role, Gender gender) {
        var user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setPhoneNumber(makePhoneNumber());
        user.setEmail(generateUniqueEmail(seed));
        user.setPasswordHash(faker.internet().password(8, 16));
        user.setRole(role);
        user.setRegistrationDate(TimeUtils.randomInstantBetween(Instant.parse("2020-01-24T00:00:00Z"), Instant.parse("2025-09-24T00:00:00Z")));
        user.setProfilePictureUrl(CollectionHelpers.randomItem(profilePictures()));
        user.setGender(gender);

        UserAddress userAddress = userAddressFactory.createUserAddresses(1).getFirst();

        userAddress.setUser(user);

        user.setAddress(userAddress);

        return user;
    }

    private String makePhoneNumber() {
        return IntStream.range(0, 10)
            .map(i -> faker.number().numberBetween(1, 10))
            .mapToObj(String::valueOf)
            .collect(Collectors.joining());
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
                "profile_4.jpg",
                "profile_5.jpg",
                "profile_6.jpg"
        );
    }

}