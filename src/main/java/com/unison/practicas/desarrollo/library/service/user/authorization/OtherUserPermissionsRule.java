package com.unison.practicas.desarrollo.library.service.user.authorization;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import com.unison.practicas.desarrollo.library.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class OtherUserPermissionsRule {

    private static final Map<RoleName, Map<RoleName, Set<String>>> TARGET_USER_PERMISSIONS = Map.of(
            RoleName.ADMIN, Map.of(
                    RoleName.ADMIN, Set.of("read"),
                    RoleName.LIBRARIAN, Set.of("read", "edit", "delete", "change-individual-permissions"),
                    RoleName.USER, Set.of("read", "edit", "delete", "change-individual-permissions")
            ),
            RoleName.LIBRARIAN, Map.of(
                    RoleName.USER, Set.of("read", "edit", "delete", "change-individual-permissions")
            ),
            RoleName.USER, Map.of()
    );

    Set<String> permissionsForUser(CustomUserDetails currentUser, User targetUser) {
        Optional<RoleName> currentUserRoleNameOptional = RoleName.parse(currentUser.getRole().getSlug());
        if (currentUserRoleNameOptional.isEmpty()) {
            return Set.of();
        }
        Optional<RoleName> targetRoleNameOptional = RoleName.parse(targetUser.getRole().getSlug());
        if (targetRoleNameOptional.isEmpty()) {
            return Set.of();
        }
        RoleName currentUserRoleName = currentUserRoleNameOptional.get();
        RoleName targetRoleName = targetRoleNameOptional.get();
        return TARGET_USER_PERMISSIONS
                .getOrDefault(currentUserRoleName, Map.of())
                .getOrDefault(targetRoleName, Set.of());
    }

    Set<RoleName> actionableRoles(CustomUserDetails user, String permission) {
        Optional<RoleName> roleNameOptional = RoleName.parse(user.getRole().getSlug());
        if (roleNameOptional.isEmpty()) {
            return Set.of();
        }
        return TARGET_USER_PERMISSIONS
                .getOrDefault(roleNameOptional.get(), Map.of())
                .entrySet().stream()
                .filter(e -> e.getValue().contains(permission))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    Set<String> permissionsForRole(CustomUserDetails currentUser, RoleName roleName) {
        Optional<RoleName> currentUserRoleOptional = RoleName.parse(currentUser.getRole().getSlug());
        if (currentUserRoleOptional.isEmpty() || roleName == null) {
            return Set.of();
        }
        RoleName currentUserRole = currentUserRoleOptional.get();
        return TARGET_USER_PERMISSIONS
                .getOrDefault(currentUserRole, Map.of())
                .getOrDefault(roleName, Set.of());
    }

}