package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.BookCategoryPopularityResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportsService {

    private final GetBookCategoriesPopularity getBookCategoriesPopularity;

    public ReportsService(GetBookCategoriesPopularity getBookCategoriesPopularity) {
        this.getBookCategoriesPopularity = getBookCategoriesPopularity;
    }

    @PreAuthorize("hasAuthority('reports:read')")
    public List<BookCategoryPopularityResponse> getBookCategoriesPopularity() {
        return getBookCategoriesPopularity.get();
    }

}