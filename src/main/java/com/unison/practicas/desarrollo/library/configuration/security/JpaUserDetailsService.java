package com.unison.practicas.desarrollo.library.configuration.security;

import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found: %s".formatted(username));
        }
        return new CustomUserDetails(userOptional.get());
    }

}