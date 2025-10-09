package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.request.CreateBookRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBookAvailabilityRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.UpdateBookRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookAvailabilityDetailsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookDetailsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookOptionsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookPreviewResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.service.book.BookService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public PaginationResponse<BookPreviewResponse> getBooks(
            @Valid GetBooksRequest filters,
            @Valid PaginationRequest pagination
    ) {
        return bookService.getBooks(filters, pagination);
    }

    @GetMapping("/{id}")
    public BookDetailsResponse getBookDetailsById(@PathVariable String id) {
        return bookService.getBookDetailsById(id);
    }

    @GetMapping("/options")
    public BookOptionsResponse getBookOptions() {
        return bookService.getBookOptions();
    }

    @PostMapping(consumes = "multipart/form-data")
    public BookDetailsResponse createBook(@Valid @ModelAttribute CreateBookRequest request) {
        return bookService.createBook(request);
    }

    @PatchMapping(
            path = "/{id}",
            consumes = "multipart/form-data"
    )
    public BookDetailsResponse updateBook(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateBookRequest request
    ) {
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable String id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExportResponse response = bookService.export(userDetails, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.fileName())
                .contentType(response.mediaType())
                .body(response.fileBytes());
    }

    @GetMapping("/{id}/availability")
    public BookAvailabilityDetailsResponse availabilityById(
            @PathVariable String id,
            @Valid @ModelAttribute GetBookAvailabilityRequest request
    ) {
        return bookService.availabilityById(id, request);
    }

}