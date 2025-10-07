package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.book.request.CreateBookRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.UpdateBookRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookDetailsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookOptionsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookPreviewResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.service.book.BookService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
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

    @GetMapping("/options")
    public BookOptionsResponse getBookOptions() {
        return bookService.getBookOptions();
    }

    @PostMapping(consumes = "multipart/form-data")
    public BookDetailsResponse createBook(@Valid @ModelAttribute CreateBookRequest request) {
        return bookService.createBook(request);
    }

    @PatchMapping(consumes = "multipart/form-data")
    public BookDetailsResponse updateBook(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateBookRequest request
    ) {
        return bookService.updateBook(id, request);
    }

}