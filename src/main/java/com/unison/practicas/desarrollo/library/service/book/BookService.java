package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.BookRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.CreateBookResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookOptionsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookPreview;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final GetBooks getBooks;
    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final AuthorRepository authorRepository;
    private final BookImageService bookImageService;

    public BookService(GetBooks getBooks, BookRepository bookRepository, BookCategoryRepository bookCategoryRepository, AuthorRepository authorRepository, BookImageService bookImageService) {
        this.getBooks = getBooks;
        this.bookRepository = bookRepository;
        this.bookCategoryRepository = bookCategoryRepository;
        this.authorRepository = authorRepository;
        this.bookImageService = bookImageService;
    }

    @PreAuthorize("hasAuthority('books:read')")
    public PaginationResponse<BookPreview> getBooks(GetBooksRequest filters, PaginationRequest pagination) {
        return getBooks.handle(filters, pagination);
    }

    @PreAuthorize("hasAuthority('books:read')")
    public BookOptionsResponse getBookOptions() {
        Iterable<OptionResponse> categories = bookCategoryRepository.findAll().stream()
                .map(this::toOptionResponse)
                .toList();

        return BookOptionsResponse.builder()
                .categories(categories)
                .build();
    }

    @PreAuthorize("hasAuthority('books:create')")
    @Transactional
    public CreateBookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN already exists: %s".formatted(request.isbn()));
        }

        Book book = toBook(request);

        Book savedBook = bookRepository.save(book);

        return toCreationResponse(savedBook);
    }

    private Book toBook(BookRequest request) {
        BookCategory category = findCategoryById(request.categoryId());
        List<Author> authors = findAuthorsByIds(request.authorIds());
        String pictureKey = bookImageService.saveBookImage(request.bookPicture());

        var book = new Book();
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setYear(request.year());
        book.setCategory(category);
        book.setAuthors(authors);
        book.setImage(pictureKey);

        return book;
    }

    private List<Author> findAuthorsByIds(List<String> ids) {
        List<Integer> parsedIds = ids.stream()
                .map(Integer::parseInt)
                .toList();

        List<Author> authors = authorRepository.findAllById(parsedIds);

        Set<Integer> foundIds = authors.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());

        List<Integer> missingIds = parsedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Authors not found for IDs: " + missingIds
            );
        }

        return authors;
    }

    private BookCategory findCategoryById(String id) {
        Optional<BookCategory> categoryOptional = bookCategoryRepository.findById(Integer.parseInt(id));
        if (categoryOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find category with id: %s".formatted(id));
        }
        return categoryOptional.get();
    }

    private OptionResponse toOptionResponse(BookCategory category) {
        return OptionResponse.builder()
                .value(String.valueOf(category.getId()))
                .label(category.getName())
                .build();
    }

    private CreateBookResponse toCreationResponse(Book book) {
        return CreateBookResponse.builder()
                .id(String.valueOf(book.getId()))
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .year(book.getYear())
                .category(book.getCategory().getName())
                .authors(book.getAuthors().stream().map(Author::getFullNameReversed).toList())
                .pictureUrl(bookImageService.bookImageUrl(book.getImage()))
                .build();
    }

}