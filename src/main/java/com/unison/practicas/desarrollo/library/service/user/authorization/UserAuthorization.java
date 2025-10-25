package com.unison.practicas.desarrollo.library.service.user.authorization;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.entity.user.RoleName;
import com.unison.practicas.desarrollo.library.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserAuthorization {

    private final SelfPermissionsRule selfPermissionsRule;
    private final OtherUserPermissionsRule otherUserPermissionsRule;
    private final RoleAssignmentRule roleAssignmentRule;

    UserAuthorization(SelfPermissionsRule selfPermissionsRule, OtherUserPermissionsRule otherUserPermissionsRule, RoleAssignmentRule roleAssignmentRule) {
        this.selfPermissionsRule = selfPermissionsRule;
        this.otherUserPermissionsRule = otherUserPermissionsRule;
        this.roleAssignmentRule = roleAssignmentRule;
    }

    public boolean canReadUser(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("read");
    }

    public boolean canEditUser(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("edit");
    }

    public boolean canDeleteUser(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("delete");
    }

    public boolean canChangeIndividualPermissions(CustomUserDetails currentUser, User targetUser) {
        return permissionsForUser(currentUser, targetUser).contains("change-individual-permissions");
    }

    public Set<String> permissionsForUser(CustomUserDetails currentUser, User targetUser) {
        boolean isSelf = currentUser.getId().equals(String.valueOf(targetUser.getId()));
        if (isSelf) {
            return selfPermissionsRule.selfPermissions(currentUser);
        }
        return otherUserPermissionsRule.permissionsForUser(currentUser, targetUser);
    }

    public Set<String> permissionsFor(CustomUserDetails currentUser, String targetUserId, RoleName roleName) {
        if (currentUser.getId().equals(targetUserId)) {
            return selfPermissionsRule.selfPermissions(currentUser);
        }
        return otherUserPermissionsRule.permissionsForRole(currentUser, roleName);
    }

    public Set<RoleName> listableRoles(CustomUserDetails currentUser) {
        return otherUserPermissionsRule.actionableRoles(currentUser, "read");
    }

    public boolean canAssignRole(CustomUserDetails currentUser, String targetRole) {
        return roleAssignmentRule.canAssignRole(currentUser, targetRole);
    }

}