package com.unison.practicas.desarrollo.library.util.factory;

import net.datafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.common.Gender;
import com.unison.practicas.desarrollo.library.entity.user.Role;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.entity.user.UserAddress;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import com.unison.practicas.desarrollo.library.util.TimeUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class UserFactory {

    private final Faker faker;
    private final RoleRepository roleRepository;
    private final GenderRepository genderRepository;
    private final UserAddressFactory userAddressFactory;
    private final PasswordEncoder passwordEncoder;

    public UserFactory(Faker faker, RoleRepository roleRepository, GenderRepository genderRepository, UserAddressFactory userAddressFactory, PasswordEncoder passwordEncoder) {
        this.faker = faker;
        this.roleRepository = roleRepository;
        this.genderRepository = genderRepository;
        this.userAddressFactory = userAddressFactory;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> createUsers(int count) {
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
        user.setPasswordHash(passwordEncoder.encode(generatePassword()));
        user.setRole(role);
        user.setRegistrationDate(TimeUtils.randomInstantBetween(Instant.parse("2020-01-24T00:00:00Z"), Instant.parse("2025-09-24T00:00:00Z")));
        user.setProfilePictureUrl(CollectionHelpers.randomItem(profilePictures()));
        user.setGender(gender);
        user.setCanLogin(true);
        user.setDateOfBirth(TimeUtils.randomLocalDateBetween(LocalDate.of(1935, 3, 15), LocalDate.of(2015, 3, 15)));

        UserAddress userAddress = userAddressFactory.createUserAddresses(1).getFirst();

        userAddress.setUser(user);

        user.setAddress(userAddress);

        return user;
    }

    private String generatePassword() {
        Faker faker = new Faker();

        var lower = "abcdefghijklmnopqrstuvwxyz";
        var upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var digits = "0123456789";
        var symbols = "!@#$%^&*?";

        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(lower.charAt(faker.random().nextInt(lower.length())));
        passwordChars.add(upper.charAt(faker.random().nextInt(upper.length())));
        passwordChars.add(digits.charAt(faker.random().nextInt(digits.length())));
        passwordChars.add(symbols.charAt(faker.random().nextInt(symbols.length())));

        String all = lower + upper + digits + symbols;
        for (int i = 0; i < 6; i++) {
            passwordChars.add(all.charAt(faker.random().nextInt(all.length())));
        }

        Collections.shuffle(passwordChars, new Random());

        StringBuilder sb = new StringBuilder();
        for (char c : passwordChars) {
            sb.append(c);
        }
        return sb.toString();
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
                "profile_3.jpg",
                "profile_4.jpg",
                "profile_5.jpg",
                "profile_6.jpg",
                "profile_7.jpg"
        );
    }

}