package com.unison.practicas.desarrollo.library.configuration.seeder;

import net.datafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.common.Gender;
import com.unison.practicas.desarrollo.library.entity.user.Role;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import com.unison.practicas.desarrollo.library.util.TimeUtils;
import com.unison.practicas.desarrollo.library.util.factory.UserAddressFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
@Slf4j
public class DemoUsersSeeder {

    private final Faker faker;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAddressFactory userAddressFactory;
    private final GenderRepository genderRepository;

    public DemoUsersSeeder(
            Faker faker,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserAddressFactory userAddressFactory,
            GenderRepository genderRepository
    ) {
        this.faker = faker;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAddressFactory = userAddressFactory;
        this.genderRepository = genderRepository;
    }

    public void seed() {
        log.debug("seeding demo users...");

        List<Gender> genders = genderRepository.findAll();

        createDemoUserIfNotExists(
                "admin@email.com",
                "Admin99##",
                "Administrador",
                RoleName.ADMIN,
                "profile_5.jpg",
                genders
        );

        createDemoUserIfNotExists(
                "bibliotecario@email.com",
                "Bibliotecario99##",
                "Bibliotecario",
                RoleName.LIBRARIAN,
                "profile_1.jpg",
                genders
        );

        createDemoUserIfNotExists(
                "usuario@email.com",
                "Usuario99##",
                "Usuario",
                RoleName.USER,
                "profile_4.jpg",
                genders
        );
    }

    private void createDemoUserIfNotExists(
            String email,
            String password,
            String firstName,
            RoleName roleName,
            String profilePicture,
            List<Gender> genders
    ) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            log.debug("user with email {} already exists, will skip seeding of this user", email);
            return;
        }

        Role role = roleRepository.findBySlug(roleName.name()).orElseThrow();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName("Demo");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhoneNumber(makePhoneNumber());
        user.setRegistrationDate(Instant.now());
        user.setRole(role);
        user.setGender(CollectionHelpers.randomItem(genders));
        user.setCanLogin(true);
        user.setDateOfBirth(randomDateOfBirth());
        user.setProfilePictureUrl(profilePicture);
        user.setAddress(userAddressFactory.createUserAddresses(1).getFirst());

        userRepository.save(user);

        log.debug("seeded demo user with email {} and role {}", email, roleName.name());
    }

    private LocalDate randomDateOfBirth() {
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
