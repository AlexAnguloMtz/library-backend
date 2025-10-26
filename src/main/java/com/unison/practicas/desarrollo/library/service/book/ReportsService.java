package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.PopularityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportsService {

    private final GetBookCategoriesPopularity getBookCategoriesPopularity;
    private final GetAuthorsPopularity getAuthorsPopularity;
    private final GetPublishersPopularity getPublishersPopularity;
    private final GetUsersAcquisition getUsersAcquisition;
    private final GetUsersDemography getUsersDemography;
    private final GetLoansDistribution getLoansDistribution;

    public ReportsService(GetBookCategoriesPopularity getBookCategoriesPopularity, GetAuthorsPopularity getAuthorsPopularity, GetPublishersPopularity getPublishersPopularity, GetUsersAcquisition getUsersAcquisition, GetUsersDemography getUsersDemography, GetLoansDistribution getLoansDistribution) {
        this.getBookCategoriesPopularity = getBookCategoriesPopularity;
        this.getAuthorsPopularity = getAuthorsPopularity;
        this.getPublishersPopularity = getPublishersPopularity;
        this.getUsersAcquisition = getUsersAcquisition;
        this.getUsersDemography = getUsersDemography;
        this.getLoansDistribution = getLoansDistribution;
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<BookCategoryPopularityResponse> getBookCategoriesPopularity(PopularityRequest request) {
        return getBookCategoriesPopularity.get(request);
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<AuthorPopularityResponse> getAuthorsPopularity(PopularityRequest request) {
        return getAuthorsPopularity.get(request);
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<PublisherPopularityResponse> getPublishersPopularity(PopularityRequest request) {
        return getPublishersPopularity.get(request);
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<UsersAcquisitionResponse> getUsersAcquisition() {
        return getUsersAcquisition.get();
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<UsersDemographyResponse> getUsersDemography() {
        return getUsersDemography.get();
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<LoansDistributionResponse> getLoansDistribution() {
        return getLoansDistribution.get();
    }

}