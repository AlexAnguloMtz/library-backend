package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.configuration.security.CustomUserDetails;
import com.unison.practicas.desarrollo.library.dto.book.AuthorOptions;
import com.unison.practicas.desarrollo.library.dto.book.AuthorRequest;
import com.unison.practicas.desarrollo.library.dto.book.AuthorResponse;
import com.unison.practicas.desarrollo.library.dto.book.GetAuthorsRequest;
import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
import com.unison.practicas.desarrollo.library.dto.common.ExportRequest;
import com.unison.practicas.desarrollo.library.dto.common.ExportResponse;
import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.common.Country;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.repository.CountryRepository;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthorService {

    private final GetAuthors getAuthors;
    private final ExportAuthors exportAuthors;

    // Repositories
    private final AuthorRepository authorRepository;
    private final CountryRepository countryRepository;

    // Utils
    private final DateTimeFormatter dateTimeFormatter;

    public AuthorService(GetAuthors getAuthors, ExportAuthors exportAuthors, AuthorRepository authorRepository, CountryRepository countryRepository) {
        this.getAuthors = getAuthors;
        this.exportAuthors = exportAuthors;
        this.authorRepository = authorRepository;
        this.countryRepository = countryRepository;
        this.dateTimeFormatter = createDateTimeFormatter();
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

    @PreAuthorize("hasAuthority('authors:create')")
    public AuthorResponse createAuthor(AuthorRequest request) {
        Author author = setAuthorFields(request, new Author());
        return toResponse(authorRepository.save(author));
    }

    @PreAuthorize("hasAuthority('authors:edit')")
    public AuthorResponse updateAuthor(String id, AuthorRequest request) {
        Author author = setAuthorFields(request, findAuthorById(id));
        return toResponse(authorRepository.save(author));
    }

    @PreAuthorize("hasAuthority('authors:delete')")
    public void deleteAuthorById(String id) {
        authorRepository.delete(findAuthorById(id));
    }

    @PreAuthorize("hasAuthority('authors:read')")
    public ExportResponse export(CustomUserDetails userDetails, ExportRequest request) {
        return exportAuthors.handle(userDetails, request);
    }

    @PreAuthorize("hasAuthority('authors:read')")
    public PaginationResponse<AuthorResponse> getAuthors(
            GetAuthorsRequest request,
            PaginationRequest pagination
    ) {
        return getAuthors.handle(request, pagination);
    }

    private AuthorResponse toResponse(Author author) {
        return AuthorResponse.builder()
                .id(String.valueOf(author.getId()))
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .country(toResponse(author.getCountry()))
                .dateOfBirth(author.getDateOfBirth())
                .bookCount(author.getBooks().size())
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

    private DateTimeFormatter createDateTimeFormatter() {
        Locale spanish = Locale.forLanguageTag("es");
        return DateTimeFormatter.ofPattern("d/MMM/yyyy", spanish);
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

    private Author setAuthorFields(AuthorRequest request, Author author) {
        author.setFirstName(request.firstName().trim());
        author.setLastName(request.lastName().trim());
        author.setDateOfBirth(request.dateOfBirth());
        author.setCountry(findCountryById(request.countryId()));
        return author;
    }

}