package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.CreateBookRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.UpdateBookRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.*;
import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.entity.common.Country;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
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
    public PaginationResponse<BookPreviewResponse> getBooks(GetBooksRequest filters, PaginationRequest pagination) {
        return getBooks.handle(filters, pagination);
    }

    @PreAuthorize("hasAuthority('books:read')")
    public BookDetailsResponse getBookDetailsById(String id) {
        return toBookDetailsResponse(findBookById(id));
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
    public BookDetailsResponse createBook(CreateBookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN already exists: %s".formatted(request.isbn()));
        }
        Book book = toBook(request);
        Book savedBook = bookRepository.save(book);
        return toBookDetailsResponse(savedBook);
    }

    @PreAuthorize("hasAuthority('books:update')")
    @Transactional
    public BookDetailsResponse updateBook(String bookId, UpdateBookRequest request) {
        Book book = findBookById(bookId);
        Optional<Book> byIsbn = bookRepository.findByIsbn(request.isbn());
        boolean isbnConflict = byIsbn.isPresent() && !byIsbn.get().getId().equals(book.getId());
        if (isbnConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ISBN already exists: %s".formatted(request.isbn()));
        }
        Book updatedBook = updatedBook(book, request);
        Book savedBook = bookRepository.save(updatedBook);
        return toBookDetailsResponse(savedBook);
    }

    @PreAuthorize("hasAuthority('books:delete')")
    @Transactional
    public void deleteBookById(String id) {
        // TODO
        // Don't delete book if it has active
        // loans or copies on inventory.
        // The loan system and inventory are not implemented yet.
        Book book = findBookById(id);
        bookRepository.delete(book);
    }

    private Book updatedBook(Book book, UpdateBookRequest request) {
        if (StringUtils.hasText(request.title())) {
            book.setTitle(request.title());
        }
        if (StringUtils.hasText(request.isbn())) {
            book.setIsbn(request.isbn());
        }
        if (request.year() != null) {
            book.setYear(request.year());
        }
        if (!CollectionUtils.isEmpty(request.authorIds())) {
            List<Author> authors = findAuthorsByIds(request.authorIds());
            book.setAuthors(authors);
        }
        if (StringUtils.hasText(request.categoryId())) {
            BookCategory category = findCategoryById(request.categoryId());
            book.setCategory(category);
        }
        if (request.bookPicture() != null) {
            updateBookImage(book, request.bookPicture());
        }
        return book;
    }

    private void updateBookImage(Book book, MultipartFile newPictureFile) {
        String oldPictureKey = book.getImage();
        String pictureKey = bookImageService.saveBookImage(newPictureFile);
        book.setImage(pictureKey);
        if (StringUtils.hasText(oldPictureKey)) {
            try {
                // TODO
                // Commented for development purposes
                // bookImageService.deleteImage(oldPictureKey);
            } catch (Exception e) {
                // Don't stop the execution flow, we can delete
                // the orphan picture later with some worker thread
            }
        }
    }

    private Book findBookById(String id) {
        Optional<Book> bookOptional = bookRepository.findById(Integer.parseInt(id));
        if (bookOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find book with id: %s".formatted(id));
        }
        return bookOptional.get();
    }

    private Book toBook(CreateBookRequest request) {
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

    private BookDetailsResponse toBookDetailsResponse(Book book) {
        return BookDetailsResponse.builder()
                .id(String.valueOf(book.getId()))
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .year(book.getYear())
                .category(toCategoryMinimalResponse(book.getCategory()))
                .authors(book.getAuthors().stream().map(this::toAuthorResponse).toList())
                .pictureUrl(bookImageService.bookImageUrl(book.getImage()))
                .build();
    }

    private BookCategoryMinimalResponse toCategoryMinimalResponse(BookCategory category) {
        return BookCategoryMinimalResponse.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .build();
    }

    private AuthorSummaryResponse toAuthorResponse(Author author) {
        return AuthorSummaryResponse.builder()
                .id(author.getId().toString())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .dateOfBirth(author.getDateOfBirth())
                .country(toCountryResponse(author.getCountry()))
                .bookCount(author.getBooks().size())
                .build();
    }

    private CountryResponse toCountryResponse(Country country) {
        return CountryResponse.builder()
                .id(country.getId().toString())
                .name(country.getNicename())
                .build();
    }

}