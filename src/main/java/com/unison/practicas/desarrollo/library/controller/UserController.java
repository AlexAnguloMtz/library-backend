package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.UserPreview;
import com.unison.practicas.desarrollo.library.dto.UserPreviewsQuery;
import com.unison.practicas.desarrollo.library.service.UserService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public PaginationResponse<UserPreview> getUsersPreviews(
            UserPreviewsQuery query,
            PaginationRequest pagination
    ) {
        return userService.getUsersPreviews(query, pagination);
    }

}