package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.book.response.BookOptionsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.service.book.BookService;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public PaginationResponse<BookResponse> getBooks(
            @Valid GetBooksRequest filters,
            @Valid PaginationRequest pagination
    ) {
        return bookService.getBooks(filters, pagination);
    }

    @GetMapping("/options")
    public BookOptionsResponse getBookOptions() {
        return bookService.getBookOptions();
    }

}
