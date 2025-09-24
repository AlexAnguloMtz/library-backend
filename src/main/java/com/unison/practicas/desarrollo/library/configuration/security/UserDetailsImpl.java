package com.unison.practicas.desarrollo.library.configuration.security;

import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.entity.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Stream<GrantedAuthority> roleAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getSlug()));

        Stream<GrantedAuthority> permissionAuthorities = user.getPermissions().stream()
                .map(Permission::getName)
                .map(SimpleGrantedAuthority::new);

        return Stream.concat(roleAuthorities, permissionAuthorities)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

}