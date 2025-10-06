package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.book.request.BookRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.CreateBookResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookOptionsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookPreview;
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
    public PaginationResponse<BookPreview> getBooks(
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
    public CreateBookResponse createBook(@Valid @ModelAttribute BookRequest request) {
        return bookService.createBook(request);
    }

}
