package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.configuration.security.UserDetailsImpl;
import com.unison.practicas.desarrollo.library.dto.*;
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
    public PaginationResponse<UserPreview> getUsersPreviews(
            UserPreviewsQuery query,
            PaginationRequest pagination
    ) {
        return userService.getUsersPreviews(query, pagination);
    }
    
    @GetMapping("/options")
    public UserOptionsResponse getUserOptions() {
        return userService.getUserOptions();
    }

    @GetMapping("/{id}")
    public FullUser getFullUserById(@PathVariable String id) {
        return userService.getFullUserById(id);
    }

    @PutMapping("/{id}/personal-data")
    public UserPersonalDataResponse updateUserPersonalData(
            String id,
            UserPersonalDataUpdateRequest request
    ) {
        return userService.updateUserPersonalData(id, request);
    }

    @PutMapping("/{id}/address")
    public UserAddressResponse updateUserAddress(
            String id,
            UserAddressUpdateRequest request
    ) {
        return userService.updateUserAddress(id, request);
    }

    @PutMapping("/{id}/account")
    public UserAccountResponse updateUserAccount(
            String id,
            UserAccountUpdateRequest request
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
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ExportResponse response = userService.export(userDetails.getUser(), request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(response.mediaType())
                .body(response.fileBytes());
    }

    @PostMapping
    public UserCreationResponse createUser(@Valid @RequestBody UserCreationRequest request) {
        return userService.createUser(request);
    }

}