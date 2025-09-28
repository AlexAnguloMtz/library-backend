package com.unison.practicas.desarrollo.library.service.user;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import com.unison.practicas.desarrollo.library.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserAuthorization {

    private static final Map<RoleName, Map<RoleName, Set<String>>> TARGET_USER_PERMISSIONS = Map.of(
            RoleName.ADMIN, Map.of(
                    RoleName.ADMIN, Set.of("read"),
                    RoleName.LIBRARIAN, Set.of("read", "edit", "delete"),
                    RoleName.USER, Set.of("read", "edit", "delete")
            ),
            RoleName.LIBRARIAN, Map.of(
                    RoleName.USER, Set.of("read", "edit", "delete")
            ),
            RoleName.USER, Map.of()
    );

    private static final Map<RoleName, Set<String>> SELF_PERMISSIONS = Map.of(
            RoleName.ADMIN, Set.of("read", "edit"),
            RoleName.LIBRARIAN, Set.of("read"),
            RoleName.USER, Set.of("read")
    );

    private static final Map<RoleName, Set<RoleName>> ROLES_ASSIGNABLE_BY_ROLE = Map.of(
            RoleName.ADMIN, Set.of(RoleName.ADMIN, RoleName.LIBRARIAN, RoleName.USER),
            RoleName.LIBRARIAN, Set.of(RoleName.USER),
            RoleName.USER, Set.of()
    );

    public boolean canReadUser(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("read");
    }

    public boolean canEditUser(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("edit");
    }

    public boolean canDeleteUser(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("delete");
    }

    public Set<String> permissionsForUser(CustomUserDetails currentUser, User targetUser) {
        Optional<RoleName> roleNameOptional = RoleName.parse(targetUser.getRole().getSlug());
        if (roleNameOptional.isEmpty()) {
            return Set.of();
        }
        RoleName roleName = roleNameOptional.get();
        return permissionsFor(currentUser, targetUser.getId().toString(), roleName);
    }

    public Set<String> permissionsFor(CustomUserDetails currentUser, String targetUserId, RoleName targetRole) {
        Optional<RoleName> roleNameOptional = RoleName.parse(currentUser.getRole().getSlug());
        if (roleNameOptional.isEmpty()) {
            return Set.of();
        }
        if (currentUser.getId().equals(targetUserId)) {
            return selfPermissions(roleNameOptional.get());
        }
        return TARGET_USER_PERMISSIONS
                .getOrDefault(roleNameOptional.get(), Map.of())
                .getOrDefault(targetRole, Set.of());
    }

    public Set<RoleName> listableRoles(CustomUserDetails user) {
        return rolesWithPermission(user, "read");
    }

    public boolean canAssignRole(CustomUserDetails currentUser, String targetRole) {
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

    private Set<RoleName> rolesWithPermission(CustomUserDetails user, String permission) {
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

    private Set<String> selfPermissions(RoleName roleName) {
        return SELF_PERMISSIONS.getOrDefault(roleName, Set.of());
    }

}