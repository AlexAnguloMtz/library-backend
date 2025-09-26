package com.unison.practicas.desarrollo.library.service;

import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.dto.common.StateResponse;
import com.unison.practicas.desarrollo.library.dto.user.request.*;
import com.unison.practicas.desarrollo.library.dto.user.response.*;
import com.unison.practicas.desarrollo.library.entity.*;
import com.unison.practicas.desarrollo.library.repository.GenderRepository;
import com.unison.practicas.desarrollo.library.repository.RoleRepository;
import com.unison.practicas.desarrollo.library.repository.StateRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    // Services
    private final GetUsersPreviews getUsersPreviews;
    private final ExportUsers exportUsers;

    // Repositories
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final GenderRepository genderRepository;

    // Utils
    private final PasswordEncoder passwordEncoder;
    private final DateTimeFormatter dateTimeFormatter;

    public UserService(PasswordEncoder passwordEncoder, GetUsersPreviews getUsersPreviews, ExportUsers exportUsers, UserRepository userRepository, RoleRepository roleRepository, StateRepository stateRepository, GenderRepository genderRepository) {
        this.passwordEncoder = passwordEncoder;
        this.getUsersPreviews = getUsersPreviews;
        this.exportUsers = exportUsers;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.stateRepository = stateRepository;
        this.genderRepository = genderRepository;
        this.dateTimeFormatter = createDateTimeFormatter();
    }

    @PreAuthorize("hasAuthority('users:read')")
    public PaginationResponse<UserPreviewResponse> getUsersPreviews(UserPreviewsRequest query, PaginationRequest pagination) {
        return getUsersPreviews.handle(query, pagination);
    }

    @PreAuthorize("hasAuthority('users:read')")
    public UserOptionsResponse getUserOptions() {
        Iterable<OptionResponse> roles = roleRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        Iterable<OptionResponse> states = stateRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        Iterable<OptionResponse> genders = genderRepository.findAll().stream()
                .map(this::toOption)
                .sorted((a, b) -> a.label().compareToIgnoreCase(b.label()))
                .toList();

        return UserOptionsResponse.builder()
                .roles(roles)
                .states(states)
                .genders(genders)
                .build();
    }

    @PreAuthorize("hasAuthority('users:read')")
    public FullUserResponse getFullUserById(String id) {
        User user = findUserById(id);
        return toFullUser(user);
    }

    @PreAuthorize("hasAuthority('users:delete')")
    public void deleteUserById(String id) {
        userRepository.deleteById(Integer.parseInt(id));
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public PersonalDataResponse updateUserPersonalData(String id, PersonalDataRequest request) {
        User user = findUserById(id);

        if (StringUtils.hasText(request.firstName())) {
            user.setFirstName(request.firstName().trim());
        }

        if (StringUtils.hasText(request.lastName())) {
            user.setLastName(request.lastName().trim());
        }

        if (StringUtils.hasText(request.phone())) {
            user.setPhoneNumber(request.phone().trim());
        }

        if (StringUtils.hasText(request.genderId())) {
            Gender gender = findGenderById(request.genderId());
            user.setGender(gender);
        }

        User savedUser = userRepository.save(user);

        return toPersonalDataResponse(savedUser);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public UserAddressResponse updateUserAddress(String id, UserAddressRequest request) {
        User user = findUserById(id);
        UserAddress userAddress = user.getAddress();

        if (StringUtils.hasText(request.stateId())) {
            State state = findStateById(request.stateId());
            userAddress.setState(state);
        }

        if (StringUtils.hasText(request.city())) {
            userAddress.setCity(request.city().trim());
        }

        if (StringUtils.hasText(request.address())) {
            userAddress.setAddress(request.address().trim());
        }

        if (StringUtils.hasText(request.district())) {
            userAddress.setDistrict(request.district().trim());
        }

        if (StringUtils.hasText(request.zipCode())) {
            userAddress.setZipCode(request.zipCode().trim());
        }

        User savedUser = userRepository.save(user);

        return toUserAddressResponse(savedUser.getAddress());
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public AccountResponse updateUserAccount(String id, AccountRequest request) {
        User userById = findUserById(id);

        if (StringUtils.hasText(request.email())) {
            Optional<User> userByEmail = userRepository.findByEmailIgnoreCase(request.email());

            boolean emailConflict = userByEmail.isPresent() && !userById.getId().equals(userByEmail.get().getId());

            if (emailConflict) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is taken: %s".formatted(userByEmail.get().getEmail()));
            }

            userById.setEmail(request.email());
        }

        if (StringUtils.hasText(request.roleId())) {
            Role role = findRoleById(request.roleId());
            userById.setRole(role);
        }

        if (StringUtils.hasText(request.password())) {
            userById.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        User savedUser = userRepository.save(userById);

        return toAccountResponse(savedUser);
    }

    @PreAuthorize("hasAuthority('users:read')")
    public ExportResponse export(User currentUser, ExportRequest request) {
        return exportUsers.handle(currentUser, request);
    }

    @PreAuthorize("hasAuthority('users:create')")
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.account().email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email '%s' is already taken".formatted(request.account().email()));
        }

        Gender gender = findGenderById(request.personalData().genderId());
        Role role = findRoleById(request.account().roleId());

        var user = new User();
        user.setFirstName(request.personalData().firstName());
        user.setLastName(request.personalData().lastName());
        user.setPhoneNumber(request.personalData().phone());
        user.setGender(gender);
        user.setAddress(toAddressEntity(request.address()));
        user.setEmail(request.account().email());
        user.setPasswordHash(passwordEncoder.encode(request.account().password()));
        user.setRole(role);
        user.setProfilePictureUrl("http://localhost:8080/api/v1/users/profile-pictures/profile_1.jpg");
        user.setRegistrationDate(Instant.now());

        var savedUser = userRepository.save(user);

        return toCreationResponse(savedUser);
    }

    private CreateUserResponse toCreationResponse(User user) {
        return CreateUserResponse.builder()
                .id(String.valueOf(user.getId()))
                .personalData(toPersonalDataResponse(user))
                .address(toUserAddressResponse(user.getAddress()))
                .account(toAccountResponse(user))
                .build();
    }

    private UserAddress toAddressEntity(UserAddressRequest request) {
        State state = findStateById(request.stateId());

        var address = new UserAddress();
        address.setState(state);
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setAddress(request.address());
        address.setZipCode(request.zipCode());
        return address;
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

    private AccountResponse toAccountResponse(User user) {
        return AccountResponse.builder()
                .email(user.getEmail())
                .role(toRoleResponse(user.getRole()))
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    private OptionResponse toOption(Role role) {
        return OptionResponse.builder()
                .value(role.getId().toString())
                .label(role.getName())
                .build();
    }

    private OptionResponse toOption(State state) {
        return OptionResponse.builder()
                .value(state.getId().toString())
                .label(state.getName())
                .build();
    }

    private FullUserResponse toFullUser(User user) {
        return FullUserResponse.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .role(toRoleResponse(user.getRole()))
                .registrationDate(dateTimeFormatter.format(user.getRegistrationDate().atOffset(ZoneOffset.UTC)))
                .profilePictureUrl(user.getProfilePictureUrl())
                .address(toUserAddressResponse(user.getAddress()))
                .gender(toGenderResponse(user.getGender()))
                .build();
    }

    private GenderResponse toGenderResponse(Gender gender) {
        return GenderResponse.builder()
                .id(gender.getId().toString())
                .name(gender.getName())
                .slug(gender.getSlug())
                .build();
    }

    private OptionResponse toOption(Gender gender) {
        return OptionResponse.builder()
                .value(gender.getId().toString())
                .label(gender.getName())
                .build();
    }

    private Gender findGenderById(String id) {
        Optional<Gender> genderOptional = genderRepository.findById(Integer.parseInt(id));
        if (genderOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find gender with id: %s".formatted(id));
        }
        return genderOptional.get();
    }

}