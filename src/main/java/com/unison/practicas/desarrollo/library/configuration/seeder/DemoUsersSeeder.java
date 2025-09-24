package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;

@Component
@Profile({"dev", "test"})
public class DemoUsersSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUsersSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seed() {
        var librarianEmail = "bibliotecario@email.com";
        var librarianPassword = "bibliotecario99##";

        var userEmail = "usuario@email.com";
        var userPassword = "usuario99##";

        if (userRepository.findByEmailIgnoreCase(librarianEmail).isEmpty()) {
            Role librarianRole = roleRepository.findBySlug("LIBRARIAN").get();

            var librarianUser = new User();
            librarianUser.setFirstName("Bibliotecario");
            librarianUser.setLastName("Demo");
            librarianUser.setEmail(librarianEmail);
            librarianUser.setPasswordHash(passwordEncoder.encode(librarianPassword));
            librarianUser.setPhoneNumber("6622118899");
            librarianUser.setRegistrationDate(Instant.now());
            librarianUser.setRoles(new HashSet<>());

            librarianUser.getRoles().add(librarianRole);

            userRepository.save(librarianUser);
        }

        if (userRepository.findByEmailIgnoreCase(userEmail).isEmpty()) {
            Role userRole = roleRepository.findBySlug("USER").get();

            var librarianUser = new User();
            librarianUser.setFirstName("Usuario");
            librarianUser.setLastName("Demo");
            librarianUser.setEmail(userEmail);
            librarianUser.setPasswordHash(passwordEncoder.encode(userPassword));
            librarianUser.setPhoneNumber("7755449933");
            librarianUser.setRegistrationDate(Instant.now());
            librarianUser.setRoles(new HashSet<>());

            librarianUser.getRoles().add(userRole);

            userRepository.save(librarianUser);
        }
    }

}