package com.unison.practicas.desarrollo.library.service;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.RoleName;
import com.unison.practicas.desarrollo.library.entity.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserAuthorization {

    public Set<String> permissionsForUser(CustomUserDetails currentUser, User someUser) {
        Optional<RoleName> roleNameOptional = RoleName.parse(someUser.getRole().getSlug());
        if (roleNameOptional.isEmpty()) {
            return Set.of();
        }
        RoleName roleName = roleNameOptional.get();
        return permissionsForRole(currentUser, roleName);
    }

    public Set<String> permissionsForRole(CustomUserDetails currentUser, RoleName roleName) {
        if (currentUser.hasRole(RoleName.LIBRARIAN) && RoleName.LIBRARIAN.equals(roleName)) {
            return Set.of("read");
        }
        if (currentUser.hasRole(RoleName.LIBRARIAN) && RoleName.USER.equals(roleName)) {
            return Set.of("read", "edit", "delete");
        }
        // Regular users won't have any special permissions right now.
        // Just return an empty collection by default.
        return Set.of();
    }

}
