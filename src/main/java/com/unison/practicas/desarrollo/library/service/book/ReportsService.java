package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.AuthorsPopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.BookCategoriesPopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorPopularityResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryPopularityResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportsService {

    private final GetBookCategoriesPopularity getBookCategoriesPopularity;
    private final GetAuthorsPopularity getAuthorsPopularity;

    public ReportsService(GetBookCategoriesPopularity getBookCategoriesPopularity, GetAuthorsPopularity getAuthorsPopularity) {
        this.getBookCategoriesPopularity = getBookCategoriesPopularity;
        this.getAuthorsPopularity = getAuthorsPopularity;
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<BookCategoryPopularityResponse> getBookCategoriesPopularity(BookCategoriesPopularityRequest request) {
        return getBookCategoriesPopularity.get(request);
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<AuthorPopularityResponse> getAuthorsPopularity(AuthorsPopularityRequest request) {
        return getAuthorsPopularity.get(request);
    }

}