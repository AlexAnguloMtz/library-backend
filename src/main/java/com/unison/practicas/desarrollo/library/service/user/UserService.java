package com.unison.practicas.desarrollo.library.service.user;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.dto.common.StateResponse;
import com.unison.practicas.desarrollo.library.dto.user.request.*;
import com.unison.practicas.desarrollo.library.dto.user.response.*;
import com.unison.practicas.desarrollo.library.entity.common.Gender;
import com.unison.practicas.desarrollo.library.entity.common.State;
import com.unison.practicas.desarrollo.library.entity.user.Role;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.entity.user.UserAddress;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.StateRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.service.user.authorization.UserAuthorization;
import com.unison.practicas.desarrollo.library.util.event.UserRegistered;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class UserService {

    // Services
    private final GetUsersPreviews getUsersPreviews;
    private final ExportUsers exportUsers;
    private final ProfilePictureService profilePictureService;
    private final UserAuthorization userAuthorization;
    private final GetUserOptions getUserOptions;
    private final ApplicationEventPublisher publisher;

    // Repositories
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final GenderRepository genderRepository;

    // Utils
    private final PasswordEncoder passwordEncoder;
    private final DateTimeFormatter dateTimeFormatter;

    public UserService(PasswordEncoder passwordEncoder, GetUsersPreviews getUsersPreviews, ExportUsers exportUsers, ProfilePictureService profilePictureService, UserAuthorization userAuthorization, GetUserOptions getUserOptions, ApplicationEventPublisher publisher, UserRepository userRepository, RoleRepository roleRepository, StateRepository stateRepository, GenderRepository genderRepository) {
        this.passwordEncoder = passwordEncoder;
        this.getUsersPreviews = getUsersPreviews;
        this.exportUsers = exportUsers;
        this.profilePictureService = profilePictureService;
        this.userAuthorization = userAuthorization;
        this.getUserOptions = getUserOptions;
        this.publisher = publisher;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.stateRepository = stateRepository;
        this.genderRepository = genderRepository;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    @PreAuthorize("hasAuthority('users:read')")
    public PaginationResponse<UserPreviewResponse> getUsersPreviews(
            UserPreviewsRequest query,
            PaginationRequest pagination,
            CustomUserDetails currentUser
    ) {
        return getUsersPreviews.handle(query, pagination, currentUser);
    }

    @PreAuthorize("hasAuthority('users:read')")
    public UserOptionsResponse getUserOptions() {
        return getUserOptions.get();
    }

    @PreAuthorize("hasAuthority('users:read') || (hasAuthority('users:read:self') && #id == principal.id)")
    public FullUserResponse getFullUserById(
            @P("id") String id,
            CustomUserDetails currentUser
    ) {
        User user = findUserById(id);

        if (!userAuthorization.canReadUser(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to read this user");
        }

        Set<String> permissions = permissionsForUser(currentUser, user);

        return toFullUser(user, permissions);
    }

    @PreAuthorize("hasAuthority('users:delete')")
    @Transactional
    public void deleteUserById(String id, CustomUserDetails currentUser) {
        User user = findUserById(id);

        if (!userAuthorization.canDeleteUser(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to delete this user");
        }

        userRepository.delete(user);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public PersonalDataResponse updateUserPersonalData(
            String id,
            PersonalDataRequest request,
            CustomUserDetails currentUser
    ) {
        User user = findUserById(id);

        if (!userAuthorization.canEditUser(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to edit this user");
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phone().trim());
        user.setDateOfBirth(request.dateOfBirth());

        Gender gender = findGenderById(request.genderId());
        user.setGender(gender);

        User savedUser = userRepository.save(user);

        return toPersonalDataResponse(savedUser);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public UserAddressResponse updateUserAddress(
            String id,
            UserAddressRequest request,
            CustomUserDetails currentUser
    ) {
        User user = findUserById(id);

        if (!userAuthorization.canEditUser(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to edit this user");
        }

        State state = findStateById(request.stateId());

        UserAddress address = user.getAddress().orElseGet(UserAddress::new);

        address.setState(state);
        address.setCity(request.city().trim());
        address.setAddress(request.address().trim());
        address.setDistrict(request.district().trim());
        address.setZipCode(request.zipCode().trim());

        user.setAddress(address);

        User savedUser = userRepository.save(user);

        // This Optional unboxing is 100% safe, we now know the address is present
        UserAddress savedAddress = savedUser.getAddress().get();

        return toUserAddressResponse(savedAddress);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public AccountResponse updateUserAccount(
            String id,
            UpdateAccountRequest request,
            CustomUserDetails currentUser
    ) {
        User userById = findUserById(id);
        if (!userAuthorization.canEditUser(currentUser, userById)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to edit this user");
        }

        Role role = findRoleById(request.roleId());
        if (!userAuthorization.canAssignRole(currentUser, role.getSlug())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permisos para asignar el rol '%s'".formatted(role.getName())
            );
        }

        Optional<User> userByEmail = userRepository.findByEmailIgnoreCase(request.email());

        boolean emailConflict = userByEmail.isPresent() && !userById.getId().equals(userByEmail.get().getId());

        if (emailConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está ocupado por otra cuenta".formatted(userByEmail.get().getEmail()));
        }

        userById.setEmail(request.email().trim());
        userById.setRole(role);

        User savedUser = userRepository.save(userById);

        Set<String> permissions = userAuthorization.permissionsForUser(currentUser, savedUser);

        return toAccountResponse(savedUser, permissions);
    }

    @PreAuthorize("hasAuthority('users:read')")
    public ExportResponse export(CustomUserDetails currentUser, ExportRequest request) {
        return exportUsers.handle(currentUser, request);
    }

    @PreAuthorize("hasAuthority('users:create')")
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request, CustomUserDetails currentUser) {
        if (userRepository.existsByEmailIgnoreCase(request.account().email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El correo ya está ocupado por otra cuenta: %s".formatted(request.account().email()));
        }

        Role role = findRoleById(request.account().roleId());
        if (!userAuthorization.canAssignRole(currentUser, role.getSlug())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos para asignar el rol '%s'".formatted(role.getName())
            );
        }

        User user = mapUser(request, role);

        User savedUser = userRepository.save(user);

        UserRegistered event = toRegistrationEvent(savedUser);

        publisher.publishEvent(event);

        Set<String> permissions = userAuthorization.permissionsForUser(currentUser, savedUser);

        return toCreationResponse(savedUser, permissions);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public UpdateProfilePictureResponse updateProfilePicture(
            String userId,
            UpdateProfilePictureRequest request,
            CustomUserDetails currentUser
    ) {
        User user = findUserById(userId);

        if (!userAuthorization.canEditUser(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to edit this user");
        }

        Optional<String> oldPictureKeyOptional = user.getProfilePictureUrl();

        String newPictureKey = profilePictureService.saveProfilePicture(request.profilePicture());

        user.setProfilePictureUrl(newPictureKey);

        User savedUser = userRepository.save(user);

        if (oldPictureKeyOptional.isPresent()) {
            try {
                // TODO
                // Commented for development purposes
                // profilePictureService.deleteProfilePicture(oldPictureKeyOptional.get());
            } catch (Exception e) {
                // Don't stop the execution flow, we can delete
                // the orphan picture later with some worker thread
            }
        }

        return UpdateProfilePictureResponse.builder()
                .profilePictureUrl(profilePictureService.profilePictureUrl(savedUser.getProfilePictureUrl().orElse(null)))
                .build();
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public void changePassword(String id, ChangePasswordRequest request, CustomUserDetails currentUser) {
        User user = findUserById(id);

        if (!userAuthorization.canEditUser(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to edit this user");
        }

        if (!request.password().trim().equals(request.confirmedPassword().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords don't match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password().trim()));

        userRepository.save(user);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public UserPermissionsResponse updateUserPermissions(String id, UpdateUserPermissionsRequest request, CustomUserDetails currentUser) {
        User user = findUserById(id);

        if (!userAuthorization.canChangeIndividualPermissions(currentUser, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permissions to change the individual permissions of this user");
        }

        if (request.login() != null) {
            user.setCanLogin(request.login());
        }

        User savedUser = userRepository.save(user);

        return UserPermissionsResponse.builder()
                .login(savedUser.getCanLogin())
                .build();
    }

    private CreateUserResponse toCreationResponse(User user, Set<String> permissions) {
        return CreateUserResponse.builder()
                .id(String.valueOf(user.getId()))
                .personalData(toPersonalDataResponse(user))
                .account(toAccountResponse(user, permissions))
                .build();
    }

    private Role findRoleById(String id) {
        Optional<Role> roleOptional = roleRepository.findById(Integer.parseInt(id));
        if (roleOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find role: %s".formatted(id));
        }
        return roleOptional.get();
    }

    private State findStateById(String state) {
        Optional<State> stateOptional = stateRepository.findById(Integer.parseInt(state));
        if (stateOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find state: %s".formatted(state));
        }
        return stateOptional.get();
    }

    private PersonalDataResponse toPersonalDataResponse(User user) {
        return PersonalDataResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhoneNumber())
                .gender(toGenderResponse(user.getGender()))
                .age(calculateAge(user.getDateOfBirth()))
                .build();
    }

    private User findUserById(String id) {
        Optional<User> userOptional = userRepository.findById(Integer.parseInt(id));
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find user with id: %s".formatted(id));
        }
        return userOptional.get();
    }


    private UserAddressResponse toUserAddressResponse(UserAddress userAddress) {
        return UserAddressResponse.builder()
                .state(toStateResponse(userAddress.getState()))
                .city(userAddress.getCity())
                .address(userAddress.getAddress())
                .district(userAddress.getDistrict())
                .zipCode(userAddress.getZipCode())
                .build();
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId().toString(),
                role.getName(),
                role.getSlug()
        );
    }

    private StateResponse toStateResponse(State state) {
        return StateResponse.builder()
                .id(state.getId().toString())
                .name(state.getName())
                .code(state.getCode())
                .build();
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

    private AccountResponse toAccountResponse(User user, Set<String> permissions) {
        return AccountResponse.builder()
                .email(user.getEmail())
                .role(toRoleResponse(user.getRole()))
                .profilePictureUrl(profilePictureService.profilePictureUrl(user.getProfilePictureUrl().orElse(null)))
                .permissions(permissions)
                .build();
    }

    private FullUserResponse toFullUser(User user, Set<String> permissions) {
        return FullUserResponse.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .role(toRoleResponse(user.getRole()))
                .registrationDate(dateTimeFormatter.format(user.getRegistrationDate().atOffset(ZoneOffset.UTC)))
                .profilePictureUrl(profilePictureService.profilePictureUrl(user.getProfilePictureUrl().orElse(null)))
                .address(user.getAddress().map(this::toUserAddressResponse).orElse(null))
                .gender(toGenderResponse(user.getGender()))
                .dateOfBirth(user.getDateOfBirth())
                .age(calculateAge(user.getDateOfBirth()))
                .canLogin(user.getCanLogin())
                .permissions(permissions)
                .build();
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private GenderResponse toGenderResponse(Gender gender) {
        return GenderResponse.builder()
                .id(gender.getId().toString())
                .name(gender.getName())
                .slug(gender.getSlug())
                .build();
    }

    private Gender findGenderById(String id) {
        Optional<Gender> genderOptional = genderRepository.findById(Integer.parseInt(id));
        if (genderOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find gender with id: %s".formatted(id));
        }
        return genderOptional.get();
    }

    private Set<String> permissionsForUser(CustomUserDetails currentUser, User someUser) {
        return userAuthorization.permissionsForUser(currentUser, someUser);
    }

    private User mapUser(CreateUserRequest request, Role role) {
        Gender gender = findGenderById(request.personalData().genderId());

        var user = new User();
        user.setFirstName(request.personalData().firstName().trim());
        user.setLastName(request.personalData().lastName().trim());
        user.setPhoneNumber(request.personalData().phone().trim());
        user.setGender(gender);
        user.setEmail(request.account().email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.account().password().trim()));
        user.setRole(role);
        user.setDateOfBirth(request.personalData().dateOfBirth());
        user.setRegistrationDate(Instant.now());
        user.setCanLogin(true);

        if (request.account().profilePicture() != null) {
            String profilePictureKey = profilePictureService.saveProfilePicture(request.account().profilePicture());
            user.setProfilePictureUrl(profilePictureKey);
        }

        return user;
    }

    private UserRegistered toRegistrationEvent(User user) {
        return UserRegistered.builder()
                .userId(user.getId().toString())
                .role(user.getRole().getName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender().getName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .build();
    }

}