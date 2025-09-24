package com.unison.practicas.desarrollo.library.service;

import com.unison.practicas.desarrollo.library.dto.LoginForm;
import com.unison.practicas.desarrollo.library.dto.LoginResponse;
import com.unison.practicas.desarrollo.library.entity.Permission;
import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.JwtUtils;
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

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginForm loginForm) {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(loginForm.email());
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginForm.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtils.accessTokenForUser(user);

        return toLoginResponse(user, accessToken);
    }

    private LoginResponse toLoginResponse(User user, String accessToken) {
        return LoginResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getSlug).collect(Collectors.toSet()))
                .permissions(user.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()))
                .accessToken(accessToken)
                .build();
    }

}
