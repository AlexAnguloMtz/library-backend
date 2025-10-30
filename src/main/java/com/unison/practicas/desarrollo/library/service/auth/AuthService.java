package com.unison.practicas.desarrollo.library.service.auth;

import com.unison.practicas.desarrollo.library.dto.auth.LoginForm;
import com.unison.practicas.desarrollo.library.dto.auth.LoginResponse;
import com.unison.practicas.desarrollo.library.entity.user.Permission;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.service.user.ProfilePictureService;
import com.unison.practicas.desarrollo.library.util.JwtUtils;
import com.unison.practicas.desarrollo.library.util.event.UserLoggedIn;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ProfilePictureService profilePictureService;
    private final ApplicationEventPublisher publisher;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, ProfilePictureService profilePictureService, ApplicationEventPublisher publisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.profilePictureService = profilePictureService;
        this.publisher = publisher;
    }

    @Transactional
    public LoginResponse login(LoginForm loginForm) {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(loginForm.email());
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña inválidos");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginForm.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña inválidos");
        }

        if (!user.getCanLogin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Cuenta bloqueada. No tienes permitido iniciar sesión. Contacta a un administrador para más información.");
        }

        String accessToken = jwtUtils.accessTokenForUser(user);

        UserLoggedIn event = toLoginEvent(user);

        LoginResponse response = toLoginResponse(user, accessToken);

        publisher.publishEvent(event);

        return response;
    }

    private LoginResponse toLoginResponse(User user, String accessToken) {
        return LoginResponse.builder()
                .userId(user.getId().toString())
                .profilePictureUrl(profilePictureService.profilePictureUrl(user.getProfilePictureUrl().orElse(null)))
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .permissions(user.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()))
                .accessToken(accessToken)
                .build();
    }

    private UserLoggedIn toLoginEvent(User user) {
        return UserLoggedIn.builder()
                .userId(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

}