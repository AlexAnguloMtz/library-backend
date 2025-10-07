package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorOptions;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorSummaryResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetAuthorsRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.AuthorRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.service.book.AuthorService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public PaginationResponse<AuthorSummaryResponse> getAuthors(
            @Valid GetAuthorsRequest request,
            @Valid PaginationRequest pagination
    ) {
        return authorService.getAuthors(request, pagination);
    }

    @GetMapping("/options")
    public AuthorOptions getAuthorOptions() {
        return authorService.getAuthorOptions();
    }

    @PostMapping
    public AuthorSummaryResponse createAuthor(@Valid @RequestBody AuthorRequest request) {
        return authorService.createAuthor(request);
    }

    @PutMapping("/{id}")
    public AuthorSummaryResponse updateAuthor(
            @PathVariable String id,
            @Valid @RequestBody AuthorRequest request
    ) {
        return authorService.updateAuthor(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthorById(@PathVariable String id) {
        authorService.deleteAuthorById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExportResponse response = authorService.export(userDetails, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(response.mediaType())
                .body(response.fileBytes());
    }

}