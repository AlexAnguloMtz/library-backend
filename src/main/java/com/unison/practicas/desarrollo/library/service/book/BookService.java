package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.BookOptionsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    public final GetBooks getBooks;
    private final BookCategoryRepository bookCategoryRepository;

    public BookService(GetBooks getBooks, BookCategoryRepository bookCategoryRepository) {
        this.getBooks = getBooks;
        this.bookCategoryRepository = bookCategoryRepository;
    }

    @PreAuthorize("hasAuthority('books:read')")
    public PaginationResponse<BookResponse> getBooks(GetBooksRequest filters, PaginationRequest pagination) {
        return getBooks.handle(filters, pagination);
    }

    @PreAuthorize("hasAuthority('books:read')")
    public BookOptionsResponse getBookOptions() {
        Iterable<OptionResponse> categories = bookCategoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();

        return BookOptionsResponse.builder()
                .categories(categories)
                .build();
    }

    private OptionResponse toResponse(BookCategory category) {
        return OptionResponse.builder()
                .value(String.valueOf(category.getId()))
                .label(category.getName())
                .build();
    }

}