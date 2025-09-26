package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.user.request.UpdateProfilePictureRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.dto.user.request.*;
import com.unison.practicas.desarrollo.library.dto.user.response.*;
import com.unison.practicas.desarrollo.library.service.UserService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PaginationResponse<UserPreviewResponse> getUsersPreviews(
            UserPreviewsRequest query,
            PaginationRequest pagination,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return userService.getUsersPreviews(query, pagination, currentUser);
    }
    
    @GetMapping("/options")
    public UserOptionsResponse getUserOptions() {
        return userService.getUserOptions();
    }

    @GetMapping("/{id}")
    public FullUserResponse getFullUserById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.getFullUserById(id, userDetails);
    }

    @PutMapping("/{id}/personal-data")
    public PersonalDataResponse updateUserPersonalData(
            @PathVariable String id,
            @Valid @RequestBody PersonalDataRequest request
    ) {
        return userService.updateUserPersonalData(id, request);
    }

    @PutMapping("/{id}/address")
    public UserAddressResponse updateUserAddress(
            @PathVariable String id,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return userService.updateUserAddress(id, request);
    }

    @PutMapping("/{id}/account")
    public AccountResponse updateUserAccount(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        return userService.updateUserAccount(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExportResponse response = userService.export(userDetails.getUser(), request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(response.mediaType())
                .body(response.fileBytes());
    }

    @PostMapping(consumes = "multipart/form-data")
    public CreateUserResponse createUser(@Valid @ModelAttribute CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping(
            path = "/{id}/profile-picture",
            consumes = "multipart/form-data"
    )
    public UpdateProfilePictureResponse updateProfilePicture(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateProfilePictureRequest request
    ) {
        return userService.updateProfilePicture(id, request);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String id,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

}