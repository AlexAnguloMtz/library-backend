package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.github.javafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.common.Gender;
import com.unison.practicas.desarrollo.library.entity.user.Role;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.entity.user.UserAddress;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import com.unison.practicas.desarrollo.library.util.TimeUtils;
import com.unison.practicas.desarrollo.library.util.factory.UserAddressFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class DemoUsersSeeder {

    private final Faker faker;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAddressFactory userAddressFactory;
    private final GenderRepository genderRepository;

    public DemoUsersSeeder(Faker faker, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserAddressFactory userAddressFactory, GenderRepository genderRepository) {
        this.faker = faker;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAddressFactory = userAddressFactory;
        this.genderRepository = genderRepository;
    }

    public void seed() {
        var adminEmail = "admin@email.com";
        var adminPassword = "Admin99##";

        var librarianEmail = "bibliotecario@email.com";
        var librarianPassword = "Bibliotecario99##";

        var userEmail = "usuario@email.com";
        var userPassword = "Usuario99##";

        List<Gender> genders = genderRepository.findAll();

        if (userRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findBySlug(RoleName.ADMIN.name()).get();

            var adminUser = new User();
            adminUser.setFirstName("Administrador");
            adminUser.setLastName("Demo");
            adminUser.setEmail(adminEmail);
            adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
            adminUser.setPhoneNumber(makePhoneNumber());
            adminUser.setRegistrationDate(Instant.now());
            adminUser.setRole(adminRole);
            adminUser.setGender(CollectionHelpers.randomItem(genders));
            adminUser.setDateOfBirth(dateOfBirth());

            adminUser.setProfilePictureUrl("profile_5.jpg");

            UserAddress userAddress = userAddressFactory.createUserAddresses(1).getFirst();

            adminUser.setAddress(userAddress);

            userRepository.save(adminUser);
        }

        if (userRepository.findByEmailIgnoreCase(librarianEmail).isEmpty()) {
            Role librarianRole = roleRepository.findBySlug(RoleName.LIBRARIAN.name()).get();

            var librarianUser = new User();
            librarianUser.setFirstName("Bibliotecario");
            librarianUser.setLastName("Demo");
            librarianUser.setEmail(librarianEmail);
            librarianUser.setPasswordHash(passwordEncoder.encode(librarianPassword));
            librarianUser.setPhoneNumber(makePhoneNumber());
            librarianUser.setRegistrationDate(Instant.now());
            librarianUser.setRole(librarianRole);
            librarianUser.setGender(CollectionHelpers.randomItem(genders));
            librarianUser.setDateOfBirth(dateOfBirth());

            librarianUser.setProfilePictureUrl("profile_1.jpg");

            UserAddress userAddress = userAddressFactory.createUserAddresses(1).getFirst();

            librarianUser.setAddress(userAddress);

            userRepository.save(librarianUser);
        }

        if (userRepository.findByEmailIgnoreCase(userEmail).isEmpty()) {
            Role userRole = roleRepository.findBySlug(RoleName.USER.name()).get();

            var regularUser = new User();
            regularUser.setFirstName("Usuario");
            regularUser.setLastName("Demo");
            regularUser.setEmail(userEmail);
            regularUser.setPasswordHash(passwordEncoder.encode(userPassword));
            regularUser.setPhoneNumber(makePhoneNumber());
            regularUser.setRegistrationDate(Instant.now());
            regularUser.setRole(userRole);
            regularUser.setGender(CollectionHelpers.randomItem(genders));
            regularUser.setDateOfBirth(dateOfBirth());

            regularUser.setProfilePictureUrl("profile_4.jpg");

            UserAddress userAddress = userAddressFactory.createUserAddresses(1).getFirst();

            regularUser.setAddress(userAddress);

            userRepository.save(regularUser);
        }
    }

    private LocalDate dateOfBirth() {
        return TimeUtils.randomLocalDateBetween(
                LocalDate.of(1950, 1, 1),
                LocalDate.of(2000, 1, 1)
        );
    }

    private String makePhoneNumber() {
        return IntStream.range(0, 10)
                .map(i -> faker.number().numberBetween(1, 10))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

}