package com.unison.practicas.desarrollo.library.service;

import com.unison.practicas.desarrollo.library.dto.*;
import com.unison.practicas.desarrollo.library.entity.Role;
import com.unison.practicas.desarrollo.library.entity.User;
import com.unison.practicas.desarrollo.library.entity.UserAddress;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final GetUsersPreviews getUsersPreviews;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DateTimeFormatter dateTimeFormatter;

    public UserService(GetUsersPreviews getUsersPreviews, UserRepository userRepository, RoleRepository roleRepository) {
        this.getUsersPreviews = getUsersPreviews;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dateTimeFormatter = createDateTimeFormatter();
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

    @PreAuthorize("hasAuthority('users:read')")
    public FullUser getFullUserById(String id) {
        Optional<User> userOptional = userRepository.findById(Integer.parseInt(id));
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        return toFullUser(user);
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

    private FullUser toFullUser(User user) {
        return FullUser.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName("%s %s".formatted(user.getFirstName(), user.getLastName()))
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .roles(user.getRoles().stream().map(this::toRoleResponse).toList())
                .registrationDate(dateTimeFormatter.format(user.getRegistrationDate().atOffset(ZoneOffset.UTC)))
                .profilePictureUrl(user.getProfilePictureUrl())
                .address(toUserAddressResponse(user.getAddress()))
                .build();
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId().toString(),
                role.getName(),
                role.getSlug()
        );
    }

    private DateTimeFormatter createDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(
                "dd/MMM/yyyy",
                new Locale.Builder()
                        .setLanguage("es")
                        .setRegion("MX")
                        .build()
        );
    }

    private UserAddressResponse toUserAddressResponse(UserAddress userAddress) {
        return UserAddressResponse.builder()
                .state(userAddress.getState().getName())
                .city(userAddress.getCity())
                .address(userAddress.getAddress())
                .district(userAddress.getDistrict())
                .zipCode(userAddress.getZipCode())
                .build();
    }

}