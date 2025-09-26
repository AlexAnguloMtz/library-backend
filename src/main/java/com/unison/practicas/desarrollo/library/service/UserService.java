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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
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
    private final ProfilePictureService profilePictureService;

    // Repositories
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final GenderRepository genderRepository;

    // Utils
    private final PasswordEncoder passwordEncoder;
    private final DateTimeFormatter dateTimeFormatter;

    public UserService(PasswordEncoder passwordEncoder, GetUsersPreviews getUsersPreviews, ExportUsers exportUsers, ProfilePictureService profilePictureService, UserRepository userRepository, RoleRepository roleRepository, StateRepository stateRepository, GenderRepository genderRepository) {
        this.passwordEncoder = passwordEncoder;
        this.getUsersPreviews = getUsersPreviews;
        this.exportUsers = exportUsers;
        this.profilePictureService = profilePictureService;
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

    @PreAuthorize("hasAuthority('users:read') || (hasAuthority('users:read:self') && #id == principal.id)")
    public FullUserResponse getFullUserById(@P("id") String id) {
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

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phone().trim());

        Gender gender = findGenderById(request.genderId());
        user.setGender(gender);

        User savedUser = userRepository.save(user);

        return toPersonalDataResponse(savedUser);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public UserAddressResponse updateUserAddress(String id, UserAddressRequest request) {
        User user = findUserById(id);
        State state = findStateById(request.stateId());

        UserAddress address = user.getAddress();

        address.setState(state);
        address.setCity(request.city().trim());
        address.setAddress(request.address().trim());
        address.setDistrict(request.district().trim());
        address.setZipCode(request.zipCode().trim());

        user.setAddress(address);

        User savedUser = userRepository.save(user);

        return toUserAddressResponse(savedUser.getAddress());
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public AccountResponse updateUserAccount(String id, UpdateAccountRequest request) {
        User userById = findUserById(id);

        Optional<User> userByEmail = userRepository.findByEmailIgnoreCase(request.email());

        boolean emailConflict = userByEmail.isPresent() && !userById.getId().equals(userByEmail.get().getId());

        if (emailConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is taken: %s".formatted(userByEmail.get().getEmail()));
        }

        userById.setEmail(request.email().trim());

        Role role = findRoleById(request.roleId());
        userById.setRole(role);

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

        String profilePictureKey = profilePictureService.saveProfilePicture(request.account().profilePicture());

        var user = new User();
        user.setFirstName(request.personalData().firstName().trim());
        user.setLastName(request.personalData().lastName().trim());
        user.setPhoneNumber(request.personalData().phone().trim());
        user.setGender(gender);
        user.setAddress(toAddressEntity(request.address()));
        user.setEmail(request.account().email().trim());
        user.setPasswordHash(passwordEncoder.encode(request.account().password().trim()));
        user.setRole(role);
        user.setProfilePictureUrl(profilePictureKey);
        user.setRegistrationDate(Instant.now());

        var savedUser = userRepository.save(user);

        return toCreationResponse(savedUser);
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public UpdateProfilePictureResponse updateProfilePicture(String userId, UpdateProfilePictureRequest request) {
        User user = findUserById(userId);

        String oldPictureKey = user.getProfilePictureUrl();

        String newPictureKey = profilePictureService.saveProfilePicture(request.profilePicture());

        user.setProfilePictureUrl(newPictureKey);

        User savedUser = userRepository.save(user);

        if (StringUtils.hasText(oldPictureKey)) {
            try {
                profilePictureService.deleteProfilePicture(oldPictureKey);
            } catch (Exception e) {
                // Don't stop the execution flow, we can delete
                // the orphan picture later with some worker thread
            }
        }

        return UpdateProfilePictureResponse.builder()
                .profilePictureUrl(profilePictureService.profilePictureUrl(savedUser.getProfilePictureUrl()))
                .build();
    }

    @PreAuthorize("hasAuthority('users:update')")
    @Transactional
    public void changePassword(String id, ChangePasswordRequest request) {
        User user = findUserById(id);

        if (!request.password().trim().equals(request.confirmedPassword().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords don't match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password().trim()));

        userRepository.save(user);
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
        address.setCity(request.city().trim());
        address.setDistrict(request.district().trim());
        address.setAddress(request.address().trim());
        address.setZipCode(request.zipCode().trim());
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
                .profilePictureUrl(profilePictureService.profilePictureUrl(user.getProfilePictureUrl()))
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
                .profilePictureUrl(profilePictureService.profilePictureUrl(user.getProfilePictureUrl()))
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