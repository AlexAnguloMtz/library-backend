package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.FullUser;
import com.unison.practicas.desarrollo.library.dto.UserFiltersResponse;
import com.unison.practicas.desarrollo.library.dto.UserPreview;
import com.unison.practicas.desarrollo.library.dto.UserPreviewsQuery;
import com.unison.practicas.desarrollo.library.service.UserService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.http.ResponseEntity;
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
    
    @GetMapping("/filters")
    public UserFiltersResponse getUserFilters() {
        return userService.getUserFilters();
    }

    @GetMapping("/{id}")
    public FullUser getFullUserById(@PathVariable String id) {
        return userService.getFullUserById(id);
    }

    @DeleteMapping("/{id}")
    public Void deleteUserById(@PathVariable String id) {
        userService.deleteUserById(id);
        return null;
    }

}