package com.unison.practicas.desarrollo.library.service.user.authorization;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
class RoleAssignmentRule {

    private static final Map<RoleName, Set<RoleName>> ROLES_ASSIGNABLE_BY_ROLE = Map.of(
            RoleName.ADMIN,
                Set.of(RoleName.ADMIN, RoleName.LIBRARIAN, RoleName.USER),

            RoleName.LIBRARIAN,
                Set.of(RoleName.USER),

            RoleName.USER,
                Set.of()
    );

    boolean canAssignRole(CustomUserDetails currentUser, String targetRole) {
        Optional<RoleName> currentUserRoleNameOpt = RoleName.parse(currentUser.getRole().getSlug());
        if (currentUserRoleNameOpt.isEmpty()) {
            return false;
        }

        Optional<RoleName> targetRoleOpt = RoleName.parse(targetRole);
        if (targetRoleOpt.isEmpty()) {
            return false;
        }

        return ROLES_ASSIGNABLE_BY_ROLE
                .getOrDefault(currentUserRoleNameOpt.get(), Set.of())
                .contains(targetRoleOpt.get());
    }

}