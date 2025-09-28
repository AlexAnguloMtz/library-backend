package com.unison.practicas.desarrollo.library.service.user.authorization;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
class SelfPermissionsRule {

    private static final Map<RoleName, Set<String>> SELF_PERMISSIONS = Map.of(
            RoleName.ADMIN,
                Set.of("read", "edit"),

            RoleName.LIBRARIAN,
                Set.of("read"),

            RoleName.USER,
                Set.of("read")
    );

    Set<String> selfPermissions(CustomUserDetails currentUser) {
        Optional<RoleName> roleNameOptional = RoleName.parse(currentUser.getRole().getSlug());
        if (roleNameOptional.isEmpty()) {
            return Set.of();
        }
        RoleName roleName = roleNameOptional.get();
        return SELF_PERMISSIONS.getOrDefault(roleName, Set.of());
    }

}