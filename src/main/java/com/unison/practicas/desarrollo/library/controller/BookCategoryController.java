package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.request.BookCategoryRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.MergeBookCategoriesRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBookCategoriesRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.MergeBookCategoriesResponse;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.service.book.BookCategoryService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/book-categories")
public class BookCategoryController {

    private final BookCategoryService bookCategoryService;

    public BookCategoryController(BookCategoryService bookCategoryService) {
        this.bookCategoryService = bookCategoryService;
    }

    @GetMapping
    public PaginationResponse<BookCategoryResponse> getBookCategories(
            @Valid GetBookCategoriesRequest request,
            @Valid PaginationRequest pagination
    ) {
        return bookCategoryService.getBookCategories(request, pagination);
    }

    @PostMapping
    public BookCategoryResponse createBookCategory(@Valid @RequestBody BookCategoryRequest request) {
        return bookCategoryService.createBookCategory(request);
    }

    @PutMapping("/{id}")
    public BookCategoryResponse updateBookCategory(
            @PathVariable String id,
            @Valid @RequestBody BookCategoryRequest request
    ) {
        return bookCategoryService.updateBookCategory(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookCategoryById(@PathVariable String id) {
        bookCategoryService.deleteBookCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExportResponse response = bookCategoryService.export(userDetails, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(response.mediaType())
                .body(response.fileBytes());
    }

    @PostMapping("/merge")
    public MergeBookCategoriesResponse merge(@Valid @RequestBody MergeBookCategoriesRequest request) {
        return bookCategoryService.merge(request);
    }

}