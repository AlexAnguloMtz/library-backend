package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.AuthorsPopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.request.BookCategoriesPopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AuthorPopularityResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryPopularityResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.UsersAcquisitionResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportsService {

    private final GetBookCategoriesPopularity getBookCategoriesPopularity;
    private final GetAuthorsPopularity getAuthorsPopularity;
    private final GetUsersAcquisition getUsersAcquisition;

    public ReportsService(GetBookCategoriesPopularity getBookCategoriesPopularity, GetAuthorsPopularity getAuthorsPopularity, GetUsersAcquisition getUsersAcquisition) {
        this.getBookCategoriesPopularity = getBookCategoriesPopularity;
        this.getAuthorsPopularity = getAuthorsPopularity;
        this.getUsersAcquisition = getUsersAcquisition;
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<BookCategoryPopularityResponse> getBookCategoriesPopularity(BookCategoriesPopularityRequest request) {
        return getBookCategoriesPopularity.get(request);
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<AuthorPopularityResponse> getAuthorsPopularity(AuthorsPopularityRequest request) {
        return getAuthorsPopularity.get(request);
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<UsersAcquisitionResponse> getUsersAcquisition() {
        return getUsersAcquisition.get();
    }

}