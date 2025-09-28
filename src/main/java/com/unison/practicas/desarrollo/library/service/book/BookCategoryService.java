package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.request.BookCategoryRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBookCategoriesRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class BookCategoryService {

    // Services
    private final GetBookCategories getBookCategories;
    private final ExportBookCategories exportBookCategories;

    // Repositories
    private final BookCategoryRepository bookCategoryRepository;

    public BookCategoryService(GetBookCategories getBookCategories, ExportBookCategories exportBookCategories, BookCategoryRepository bookCategoryRepository) {
        this.getBookCategories = getBookCategories;
        this.exportBookCategories = exportBookCategories;
        this.bookCategoryRepository = bookCategoryRepository;
    }


    @PreAuthorize("hasAuthority('book-categories:create')")
    public BookCategoryResponse createBookCategory(BookCategoryRequest request) {
        if (bookCategoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '%s' already exists".formatted(request.name().trim()));
        }
        BookCategory bookCategory = mapBookCategory(request, new BookCategory());
        return toResponse(bookCategoryRepository.save(bookCategory));
    }

    @PreAuthorize("hasAuthority('book-categories:update')")
    public BookCategoryResponse updateBookCategory(String id, BookCategoryRequest request) {
        BookCategory bookCategoryById = findBookCategoryById(id);

        Optional<BookCategory> bookCategoryByNameOptional = bookCategoryRepository.findByNameIgnoreCase(request.name().trim());

        boolean nameConflict = bookCategoryByNameOptional.isPresent() &&
                !bookCategoryByNameOptional.get().getId().equals(bookCategoryById.getId());

        if (nameConflict) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '%s' already exists".formatted(request.name().trim()));
        }

        BookCategory bookCategory = mapBookCategory(request, findBookCategoryById(id));

        return toResponse(bookCategoryRepository.save(bookCategory));
    }

    @PreAuthorize("hasAuthority('book-categories:delete')")
    public void deleteBookCategoryById(String id) {
        BookCategory bookCategory = findBookCategoryById(id);
        bookCategoryRepository.delete(bookCategory);
    }

    @PreAuthorize("hasAuthority('book-categories:read')")
    public ExportResponse export(CustomUserDetails userDetails, ExportRequest request) {
        return exportBookCategories.handle(userDetails, request);
    }

    @PreAuthorize("hasAuthority('book-categories:read')")
    public PaginationResponse<BookCategoryResponse> getBookCategories(
            GetBookCategoriesRequest request,
            PaginationRequest pagination
    ) {
        return getBookCategories.handle(request, pagination);
    }

    private BookCategoryResponse toResponse(BookCategory bookCategory) {
        return BookCategoryResponse.builder()
                .id(String.valueOf(bookCategory.getId()))
                .name(bookCategory.getName())
                .bookCount(bookCategory.getBooks() != null ? bookCategory.getBooks().size() : 0)
                .build();
    }

    private BookCategory findBookCategoryById(String id) {
        Optional<BookCategory> bookCategoryOptional = bookCategoryRepository.findById(Integer.parseInt(id));
        if (bookCategoryOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find book category with id: %s".formatted(id));
        }
        return bookCategoryOptional.get();
    }

    private BookCategory mapBookCategory(BookCategoryRequest request, BookCategory bookCategory) {
        bookCategory.setName(request.name().trim());
        return bookCategory;
    }
}
