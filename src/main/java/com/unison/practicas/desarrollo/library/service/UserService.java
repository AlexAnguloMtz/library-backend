package com.unison.practicas.desarrollo.library.service;

import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.dto.OptionResponse;
import com.unison.practicas.desarrollo.library.dto.UserFiltersResponse;
import com.unison.practicas.desarrollo.library.dto.UserPreview;
import com.unison.practicas.desarrollo.library.dto.UserPreviewsQuery;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final GetUsersPreviews getUsersPreviews;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(GetUsersPreviews getUsersPreviews, UserRepository userRepository, RoleRepository roleRepository) {
        this.getUsersPreviews = getUsersPreviews;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("hasAuthority('users:read')")
    public PaginationResponse<UserPreview> getUsersPreviews(UserPreviewsQuery query, PaginationRequest pagination) {
        return getUsersPreviews.handle(query, pagination);
    }

    @PreAuthorize("hasAuthority('users:read')")
    public UserFiltersResponse getUserFilters() {
        Iterable<OptionResponse> roles = roleRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        return UserFiltersResponse.builder()
                .roles(roles)
                .build();
    }

    @PreAuthorize("hasAuthority('users:delete')")
    public void deleteUserById(String id) {
        userRepository.deleteById(Integer.parseInt(id));
    }

    private OptionResponse toOption(Role role) {
        return OptionResponse.builder()
                .value(role.getSlug())
                .label(role.getName())
                .build();
    }

}