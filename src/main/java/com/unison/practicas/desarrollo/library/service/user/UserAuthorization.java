package com.unison.practicas.desarrollo.library.service.user;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.RoleName;
import com.unison.practicas.desarrollo.library.entity.User;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserAuthorization {

    public boolean canEditUser(CustomUserDetails currentUser, User user) {
        return permissionsForUser(currentUser, user).contains("edit");
    }

    public boolean canDeleteUser(CustomUserDetails currentUser, User user) {
        return permissionsForUser(currentUser, user).contains("delete");
    }

    public Set<String> permissionsForUser(CustomUserDetails currentUser, User someUser) {
        Optional<RoleName> roleNameOptional = RoleName.parse(someUser.getRole().getSlug());
        if (roleNameOptional.isEmpty()) {
            return Set.of();
        }
        RoleName roleName = roleNameOptional.get();
        return permissionsFor(currentUser, someUser.getId().toString(), roleName);
    }

    public Set<String> permissionsFor(CustomUserDetails currentUser, String otherUserId, RoleName roleName) {
        if (currentUser.hasRole(RoleName.ADMIN) && RoleName.ADMIN.equals(roleName)) {
            return Set.of("read");
        }
        if (currentUser.hasRole(RoleName.ADMIN)) {
            return Set.of("read", "edit", "delete");
        }
        if (currentUser.hasRole(RoleName.LIBRARIAN) && RoleName.ADMIN.equals(roleName)) {
            return Set.of("read");
        }
        if (currentUser.hasRole(RoleName.LIBRARIAN) && RoleName.LIBRARIAN.equals(roleName)) {
            return Set.of("read");
        }
        if (currentUser.hasRole(RoleName.LIBRARIAN) && RoleName.USER.equals(roleName)) {
            return Set.of("read", "edit", "delete");
        }
        if (currentUser.hasRole(RoleName.USER) && currentUser.getId().equals(otherUserId)) {
            return Set.of("read");
        }
        return Set.of();
    }

}
