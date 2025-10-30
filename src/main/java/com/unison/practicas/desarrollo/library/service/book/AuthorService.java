package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorOptions;
import com.unison.practicas.desarrollo.library.dto.book.request.AuthorRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorSummaryResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetAuthorsRequest;
import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.common.Country;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.repository.CountryRepository;
import com.unison.practicas.desarrollo.library.util.event.AuthorCreated;
import com.unison.practicas.desarrollo.library.util.event.AuthorDeleted;
import com.unison.practicas.desarrollo.library.util.event.AuthorUpdated;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    private final GetAuthors getAuthors;
    private final ExportAuthors exportAuthors;
    private final AuthorRepository authorRepository;
    private final CountryRepository countryRepository;
    private final ApplicationEventPublisher publisher;

    public AuthorService(GetAuthors getAuthors, ExportAuthors exportAuthors, AuthorRepository authorRepository, CountryRepository countryRepository, ApplicationEventPublisher publisher) {
        this.getAuthors = getAuthors;
        this.exportAuthors = exportAuthors;
        this.authorRepository = authorRepository;
        this.countryRepository = countryRepository;
        this.publisher = publisher;
    }

    @PreAuthorize("hasAuthority('authors:read')")
    public AuthorOptions getAuthorOptions() {
        List<OptionResponse> countries = countryRepository.findAll().stream()
                .map(this::toOptionResponse)
                .toList();

        return AuthorOptions.builder()
                .countries(countries)
                .build();
    }

    @Transactional
    @PreAuthorize("hasAuthority('authors:create')")
    public AuthorSummaryResponse createAuthor(AuthorRequest request) {
        Author author = mapAuthor(request, new Author());
        Author saved = authorRepository.save(author);

        publisher.publishEvent(toCreationEvent(author));

        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('authors:edit')")
    public AuthorSummaryResponse updateAuthor(String id, AuthorRequest request) {
        Author author = findAuthorById(id);

        AuthorUpdated.Fields oldValues = toUpdatedFields(author);

        Author updated = mapAuthor(request, author);

        Author saved = authorRepository.save(updated);

        AuthorUpdated.Fields newValues = toUpdatedFields(saved);

        if (!newValues.equals(oldValues)) {
            publisher.publishEvent(
                    AuthorUpdated.builder()
                            .authorId(saved.getId().toString())
                            .oldValues(oldValues)
                            .newValues(newValues)
                            .build()
            );
        }

        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('authors:delete')")
    public void deleteAuthorById(String id) {
        Author author = findAuthorById(id);

        AuthorDeleted event = toDeletionEvent(author);

        author.getBooks().forEach(book -> book.getAuthors().remove(author));
        author.getBooks().clear();

        publisher.publishEvent(event);

        authorRepository.delete(author);
    }

    @PreAuthorize("hasAuthority('authors:read')")
    public ExportResponse export(CustomUserDetails userDetails, ExportRequest request) {
        return exportAuthors.handle(userDetails, request);
    }

    @PreAuthorize("hasAuthority('authors:read')")
    public PaginationResponse<AuthorSummaryResponse> getAuthors(
            GetAuthorsRequest request,
            PaginationRequest pagination
    ) {
        return getAuthors.handle(request, pagination);
    }

    private AuthorSummaryResponse toResponse(Author author) {
        return AuthorSummaryResponse.builder()
                .id(String.valueOf(author.getId()))
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .country(toResponse(author.getCountry()))
                .dateOfBirth(author.getDateOfBirth())
                .bookCount(author.getBooks() != null ? author.getBooks().size() : 0)
                .build();
    }

    private CountryResponse toResponse(Country country) {
        return CountryResponse.builder()
                .id(String.valueOf(country.getId()))
                .name(country.getNicename())
                .build();
    }

    private OptionResponse toOptionResponse(Country country) {
        return OptionResponse.builder()
                .value(String.valueOf(country.getId()))
                .label(country.getNicename())
                .build();
    }

    private Author findAuthorById(String id) {
        Optional<Author> authorOptional = authorRepository.findById(Integer.parseInt(id));
        if (authorOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find author with id: %s".formatted(id));
        }
        return authorOptional.get();
    }

    private Country findCountryById(String id) {
        Optional<Country> countryOptional = countryRepository.findById(Integer.parseInt(id));
        if (countryOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find country with id: %s".formatted(id));
        }
        return countryOptional.get();
    }

    private Author mapAuthor(AuthorRequest request, Author author) {
        author.setFirstName(request.firstName().trim());
        author.setLastName(request.lastName().trim());
        author.setDateOfBirth(request.dateOfBirth());
        author.setCountry(findCountryById(request.countryId()));
        return author;
    }

    private AuthorCreated toCreationEvent(Author author) {
        return AuthorCreated.builder()
                .authorId(author.getId().toString())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .dateOfBirth(author.getDateOfBirth())
                .nationality(author.getCountry().getName())
                .build();
    }

    private AuthorDeleted toDeletionEvent(Author author) {
        return AuthorDeleted.builder()
                .authorId(author.getId().toString())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .dateOfBirth(author.getDateOfBirth())
                .nationality(author.getCountry().getName())
                .booksCount(author.getBooks().size())
                .build();
    }

    private AuthorUpdated.Fields toUpdatedFields(Author author) {
        return AuthorUpdated.Fields.builder()
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .nationality(author.getCountry().getName())
                .dateOfBirth(author.getDateOfBirth())
                .build();
    }

}