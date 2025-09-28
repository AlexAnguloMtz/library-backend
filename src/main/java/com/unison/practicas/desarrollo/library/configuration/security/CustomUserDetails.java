package com.unison.practicas.desarrollo.library.configuration.security;

import com.unison.practicas.desarrollo.library.entity.user.Role;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.entity.user.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Stream<GrantedAuthority> roleAuthorities =
                Stream.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

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

    public String getId() {
        return user.getId().toString();
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return user.getRole();
    }

}