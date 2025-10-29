package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.request.BookCategoryRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.MergeBookCategoriesRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBookCategoriesRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.MergeBookCategoriesResponse;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
import com.unison.practicas.desarrollo.library.util.event.BookCategoriesMerged;
import com.unison.practicas.desarrollo.library.util.event.BookCategoryCreated;
import com.unison.practicas.desarrollo.library.util.event.BookCategoryDeleted;
import com.unison.practicas.desarrollo.library.util.event.BookCategoryUpdated;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookCategoryService {

    private final GetBookCategories getBookCategories;
    private final ExportBookCategories exportBookCategories;
    private final BookCategoryRepository bookCategoryRepository;
    private final ApplicationEventPublisher publisher;

    public BookCategoryService(GetBookCategories getBookCategories, ExportBookCategories exportBookCategories, ApplicationEventPublisher publisher, BookCategoryRepository bookCategoryRepository) {
        this.getBookCategories = getBookCategories;
        this.exportBookCategories = exportBookCategories;
        this.publisher = publisher;
        this.bookCategoryRepository = bookCategoryRepository;
    }

    @PreAuthorize("hasAuthority('book-categories:create')")
    @Transactional
    public BookCategoryResponse createBookCategory(BookCategoryRequest request) {
        if (bookCategoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '%s' already exists".formatted(request.name().trim()));
        }

        BookCategory bookCategory = mapBookCategory(request, new BookCategory());

        BookCategory savedCategory = bookCategoryRepository.save(bookCategory);

        publisher.publishEvent(
                BookCategoryCreated.builder()
                        .categoryId(savedCategory.getId().toString())
                        .name(savedCategory.getName())
                        .build()
        );

        return toResponse(savedCategory);
    }

    @PreAuthorize("hasAuthority('book-categories:update')")
    @Transactional
    public BookCategoryResponse updateBookCategory(String id, BookCategoryRequest request) {
        BookCategory bookCategoryById = findBookCategoryById(id);

        BookCategoryUpdated.Fields oldValues = toUpdatedFields(bookCategoryById);

        Optional<BookCategory> bookCategoryByNameOptional = bookCategoryRepository.findByNameIgnoreCase(request.name().trim());

        boolean nameConflict = bookCategoryByNameOptional.isPresent() &&
                !bookCategoryByNameOptional.get().getId().equals(bookCategoryById.getId());

        if (nameConflict) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with name '%s' already exists".formatted(request.name().trim()));
        }

        BookCategory updatedBookCategory = mapBookCategory(request, bookCategoryById);

        BookCategory savedBookCategory = bookCategoryRepository.save(updatedBookCategory);

        BookCategoryUpdated.Fields newValues = toUpdatedFields(savedBookCategory);

        if (!newValues.equals(oldValues)) {
            publisher.publishEvent(
                    BookCategoryUpdated.builder()
                            .categoryId(savedBookCategory.getId().toString())
                            .oldValues(oldValues)
                            .newValues(newValues)
                            .build()
            );
        }

        return toResponse(savedBookCategory);
    }

    @PreAuthorize("hasAuthority('book-categories:delete')")
    @Transactional
    public void deleteBookCategoryById(String id) {
        BookCategory bookCategory = findBookCategoryById(id);
        if (!bookCategory.getBooks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes borrar una categoria que tiene libros asociados. " +
                    "Intenta combinarla con otra y será eliminada en el proceso.");
        }

        bookCategoryRepository.delete(bookCategory);

        publisher.publishEvent(
                BookCategoryDeleted.builder()
                        .categoryId(bookCategory.getId().toString())
                        .name(bookCategory.getName())
                        .build()
        );
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

    @PreAuthorize("hasAuthority('book-categories:update')")
    @Transactional
    public MergeBookCategoriesResponse merge(MergeBookCategoriesRequest request) {
        if (request.mergedCategoriesIds().contains(request.targetCategoryId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La categoría resultante no debe estar incluida en las categorías que serán eliminadas");
        }

        BookCategory targetCategory = findBookCategoryById(request.targetCategoryId());

        List<BookCategory> mergedCategories = findBookCategoriesByIds(request.mergedCategoriesIds());

        int booksMoved = mergedCategories.stream()
                .flatMap(cat -> cat.getBooks().stream())
                .peek(book -> book.setCategory(targetCategory))
                .toList()
                .size();

        mergedCategories.forEach(cat -> {
            targetCategory.getBooks().addAll(cat.getBooks());
            cat.getBooks().clear();
        });

        bookCategoryRepository.save(targetCategory);

        bookCategoryRepository.deleteAll(mergedCategories);

        publisher.publishEvent(
                BookCategoriesMerged.builder()
                        .booksMoved(booksMoved)
                        .targetCategory(toMergedCategory(targetCategory))
                        .mergedCategories(mergedCategories.stream().map(this::toMergedCategory).toList())
                        .build()
        );

        return MergeBookCategoriesResponse.builder()
                .targetCategory(toResponse(targetCategory))
                .deletedCategories(mergedCategories.size())
                .movedBooks(booksMoved)
                .build();
    }

    private BookCategoriesMerged.MergedBookCategory toMergedCategory(BookCategory category) {
        return BookCategoriesMerged.MergedBookCategory.builder()
                .categoryId(category.getId().toString())
                .name(category.getName())
                .build();
    }

    private List<BookCategory> findBookCategoriesByIds(Set<String> ids) {
        Set<Integer> intIds = ids.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        List<BookCategory> categories = bookCategoryRepository.findAllById(intIds);

        if (categories.size() != intIds.size()) {
            Set<Integer> foundIds = categories.stream()
                    .map(BookCategory::getId)
                    .collect(Collectors.toSet());

            intIds.removeAll(foundIds);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontraron las categorías con IDs: " + intIds
            );
        }

        return categories;
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

    private BookCategoryUpdated.Fields toUpdatedFields(BookCategory entity) {
        return BookCategoryUpdated.Fields.builder()
                .name(entity.getName())
                .build();
    }

}
